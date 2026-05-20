package com.rhyme.sidekick.cmdx.room.runtime

import com.rhyme.sidekick.cmdx.room.common.CLIRoomMigration
import com.rhyme.sidekick.cmdx.room.common.CLIRoomTypeConverter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.runBlocking
import java.lang.reflect.Proxy
import java.nio.file.Files
import java.nio.file.Path
import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Statement
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.functions
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor

/**
 * 核心引擎与代理
 * 这是整个库的心脏，负责反射、SQL 构建和 Flow 包装。
 */
abstract class CLIRoomDatabase {
    private lateinit var connection: Connection
    // 存储所有转换器实例
    private val allConverters = mutableListOf<CLIRoomTypeConverter>()

    // 版本管理与迁移
    private var databaseVersion: Int = 1
    private val migrations = mutableListOf<CLIRoomMigration>()

    fun addConverter(converter: CLIRoomTypeConverter) {
        allConverters.add(converter)
    }

    fun addMigrations(vararg migration: CLIRoomMigration) {
        migrations.addAll(migration)
    }

    fun init(dbPath: Path, entities: List<KClass<*>>, version: Int = 1) {
        // 1. 自动创建不存在的父目录
        val parentDir = dbPath.parent
        if (parentDir != null && Files.notExists(parentDir)) {
            Files.createDirectories(parentDir)
        }

        // 2. 获取绝对路径并转换为 SQLite JDBC 格式
        // SQLite JDBC 要求格式为 jdbc:sqlite:/path/to/db
        val absolutePath = dbPath.toAbsolutePath().toString()

        // 3. 打印路径，方便调试
        println("[JvmRoom] 数据库文件位置: $absolutePath")

        // 4. 建立连接
        connection = DriverManager.getConnection("jdbc:sqlite:$absolutePath")

        // 5. 执行迁移逻辑
        databaseVersion = version
        handleMigration()

        // 初始化表
        entities.forEach { createTable(it) }
    }

    fun close() {
        if (::connection.isInitialized) {
            connection.close()
        }
    }

    /**
     * 数据库迁移核心逻辑
     */
    private fun handleMigration() {
        val rs = connection.createStatement().executeQuery("PRAGMA user_version")
        val currentVersion = if (rs.next()) rs.getInt(1) else 0

        if (currentVersion < databaseVersion) {
            println("[JvmRoom] 检测到版本升级: $currentVersion -> $databaseVersion")

            // 按照版本顺序寻找并执行迁移
            var ver = currentVersion
            while (ver < databaseVersion) {
                val migration = migrations.find { it.startVersion == ver && it.endVersion == ver + 1 }
                if (migration != null) {
                    println("[JvmRoom] 执行迁移: ${migration.startVersion} 到 ${migration.endVersion}")
                    migration.migrate(connection)
                    ver++
                } else {
                    // 如果没找到对应的迁移路径，通常 Room 会报错，这里我们简单处理
                    println("[JvmRoom] 警告: 未找到从 $ver 到 ${ver + 1} 的迁移路径")
                    break
                }
            }
            // 更新数据库内的版本号
            connection.createStatement().execute("PRAGMA user_version = $databaseVersion")
        }
    }

    private fun createTable(entityClass: KClass<*>) {
        val tableName = getTableName(entityClass)
        val fields = entityClass.memberProperties.joinToString { prop ->
            val pkAnno = prop.findAnnotation<PrimaryKey>()
            val isPk = pkAnno != null
            val isAuto = pkAnno?.autoGenerate ?: false

            val type = when (prop.returnType.classifier) {
                Int::class, Long::class -> "INTEGER"
                Float::class, Double::class -> "REAL"
                else -> "TEXT"
            }
            // 注意：SQLite 中自增必须是 INTEGER PRIMARY KEY AUTOINCREMENT
            var desc = "$type ${if (isPk) "PRIMARY KEY" else ""}"
            if (isPk && isAuto) desc += " AUTOINCREMENT"
            "${prop.name} $desc"
        }
        val sql = "CREATE TABLE IF NOT EXISTS $tableName ($fields)"
        connection.createStatement().execute(sql)
    }

    inline fun <reified T : Any> getDao(): T {
        val daoClass = T::class.java
        return Proxy.newProxyInstance(daoClass.classLoader, arrayOf(daoClass)) { _, method, args ->
            val kFunc = T::class.functions.find { it.name == method.name }!!
            try {
                handleDaoMethod(kFunc, args?.toList() ?: emptyList())
            } catch (e: Exception) {
                // 打印真实的数据库错误原因，而不是包装后的错误
                val cause = e.cause ?: e
                println("[JvmRoom Error] 数据库操作失败: ${cause.message}")
                throw cause
            }
        } as T
    }

    fun handleDaoMethod(func: KFunction<*>, args: List<Any?>): Any? {
        val queryAnno = func.findAnnotation<Query>()
        if (queryAnno != null) {
            val sql = queryAnno.value.trim()

            // 扩展识别范围：INSERT, UPDATE, DELETE, REPLACE 统统视为写操作
            val writeKeywords = listOf("INSERT", "UPDATE", "DELETE", "REPLACE")
            val isWrite = writeKeywords.any { sql.startsWith(it, ignoreCase = true) }

            return if (isWrite) {
                executeRawWrite(func, sql, args)
            } else {
                executeRead(func, sql, args)
            }
        }

        // 处理 @Insert, @Update, @Delete 注解的方法...
        return when {
            func.findAnnotation<Insert>() != null -> executeWrite(func, args, "INSERT")
            func.findAnnotation<Update>() != null -> executeWrite(func, args, "UPDATE")
            func.findAnnotation<Delete>() != null -> executeWrite(func, args, "DELETE")
            else -> null
        }
    }

    /**
     * 智能参数绑定 (解决参数顺序问题)
     */
    private fun bindParameters(stmt: PreparedStatement, sql: String, func: KFunction<*>, args: List<Any?>) {
        val paramNames = Regex(":(\\w+)").findAll(sql).map { it.groupValues[1] }.toList()
        if (paramNames.isEmpty()) return

        // 建立函数参数名到值的映射
        // 注意：反射获取参数名需要编译器开启 -java-parameters，或者在 Kotlin 中通过参数索引匹配
        val argMap = func.parameters.filter { it.kind == kotlin.reflect.KParameter.Kind.VALUE }
            .zip(args)
            .associate { it.first.name to it.second }

        paramNames.forEachIndexed { index, name ->
            val value = argMap[name]
            stmt.setObject(index + 1, mapValueForDb(value))
        }
    }

    /**
     * 增强的返回类型解析 (支持 Flow<T> 和 Flow<List<T>>)
     */
    private fun getEntityClassFromType(type: kotlin.reflect.KType): KClass<Any> {
        val classifier = type.classifier as KClass<*>
        return when {
            classifier == Flow::class || classifier == List::class -> {
                getEntityClassFromType(type.arguments.first().type!!)
            }
            else -> classifier as KClass<Any>
        }
    }

    /**
     * 处理 @Query 里的 SELECT 语句
     */
    private fun executeRead(func: KFunction<*>, sql: String, args: List<Any?>): Any? {
        val returnType = func.returnType
        val isFlow = returnType.classifier == Flow::class
        val entityClass = getEntityClassFromType(returnType)
        val tableName = getTableName(entityClass)

        val queryBlock = {
            val preparedSql = sql.replace(Regex(":\\w+"), "?")
            val stmt = connection.prepareStatement(preparedSql)
            bindParameters(stmt, sql, func, args) // 使用绑定逻辑
            val rs = stmt.executeQuery()
            mapResultSet(rs, entityClass)
        }

        return if (isFlow) {
            flow {
                InvalidationTracker.tableChanges
                    .filter { it == tableName }
                    .onStart { emit(tableName) }
                    .collect {
                        val result = queryBlock()
                        // 如果 Flow 只要单个对象而不是 List
                        if (returnType.arguments.first().type?.classifier != List::class) {
                            emit(result.firstOrNull())
                        } else {
                            emit(result)
                        }
                    }
            }
        } else {
            val list = queryBlock()
            if (returnType.classifier == List::class) list else list.firstOrNull()
        }
    }

    private fun extractTableName(sql: String): String {
        val pattern = Regex("""(?i)(?:FROM|INTO|UPDATE|JOIN)\s+[`"\[]?(\w+)""", RegexOption.IGNORE_CASE)
        return pattern.find(sql)?.groupValues?.get(1) ?: ""
    }

    /**
     * 处理 @Query 里的 INSERT/UPDATE/DELETE 语句
     */
    private fun executeRawWrite(func: KFunction<*>, sql: String, args: List<Any?>): Long {
        val preparedSql = sql.replace(Regex(":\\w+"), "?")

        // 使用新逻辑精准提取表名
        val tableName = extractTableName(sql)
        println("[Debug] 检测到写操作，涉及表: $tableName") // 添加调试日志

        val stmt = connection.prepareStatement(preparedSql, Statement.RETURN_GENERATED_KEYS)
        bindParameters(stmt, sql, func, args) // 使用绑定逻辑

        val affectedRows = stmt.executeUpdate()

        val result = if (sql.trim().startsWith("INSERT", ignoreCase = true)) {
            val keys = stmt.generatedKeys
            if (keys.next()) keys.getLong(1) else affectedRows.toLong()
        } else {
            affectedRows.toLong()
        }

        // 发送通知
        if (tableName.isNotEmpty()) {
            runBlocking { InvalidationTracker.notifyChange(tableName) }
        }

        return result
    }

    private fun executeWrite(func: KFunction<*>, args: List<Any?>, type: String): Any {
        val entity = args[0]!!
        val entityClass = entity::class
        val tableName = getTableName(entityClass)
        val props = entityClass.memberProperties

        // 获取主键信息
        val primaryKeyProp = props.find { it.findAnnotation<PrimaryKey>() != null }
        val isAutoGenerate = primaryKeyProp?.findAnnotation<PrimaryKey>()?.autoGenerate ?: false

        val sql = when (type) {
            "INSERT" -> {
                // 如果是自增主键，过滤掉主键字段，不参与 INSERT
                val filteredProps = if (isAutoGenerate) {
                    props.filter { it != primaryKeyProp }
                } else {
                    props
                }
                val names = filteredProps.joinToString { it.name }
                val placeholders = filteredProps.joinToString { "?" }
                "INSERT INTO $tableName ($names) VALUES ($placeholders)"
            }

            "UPDATE" -> {
                val setClause = props.joinToString { "${it.name} = ?" }
                "UPDATE $tableName SET $setClause WHERE ${primaryKeyProp?.name} = ?"
            }

            "DELETE" -> "DELETE FROM $tableName WHERE ${primaryKeyProp?.name} = ?"
            else -> ""
        }

        // 执行 SQL
        val stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)

        if (type == "INSERT") {
            val filteredProps = if (isAutoGenerate) props.filter { it != primaryKeyProp } else props
            filteredProps.forEachIndexed { i, prop ->
                stmt.setObject(i + 1, mapValueForDb(prop.getter.call(entity)))
            }
        } else if (type == "UPDATE") {
            props.forEachIndexed { i, prop ->
                stmt.setObject(i + 1, mapValueForDb(prop.getter.call(entity)))
            }
            stmt.setObject(props.size + 1, mapValueForDb(primaryKeyProp?.getter?.call(entity)))
        } else if (type == "DELETE") {
            val idValue = if (entityClass.findAnnotation<Entity>() != null) {
                primaryKeyProp?.getter?.call(entity)
            } else {
                entity
            }
            stmt.setObject(1, mapValueForDb(idValue))
        }

        stmt.executeUpdate()

        // 如果是自增，返回生成的新 ID
        val generatedId = if (type == "INSERT" && isAutoGenerate) {
            val rs = stmt.generatedKeys
            if (rs.next()) rs.getLong(1) else 0L
        } else {
            1L
        }

        runBlocking { InvalidationTracker.notifyChange(tableName) }
        return generatedId
    }

    private fun executeQuery(func: KFunction<*>, args: List<Any?>): Any? {
        val queryAnno = func.findAnnotation<Query>()!!
        val sql = queryAnno.value
        val returnType = func.returnType
        val isFlow = returnType.classifier == Flow::class

        val entityClass = if (isFlow) {
            returnType.arguments.first().type!!.arguments.first().type!!.classifier as KClass<Any>
        } else if (returnType.classifier == List::class) {
            returnType.arguments.first().type!!.classifier as KClass<Any>
        } else {
            returnType.classifier as KClass<Any>
        }

        val tableName = getTableName(entityClass)

        val queryLogic = {
            val stmt = connection.prepareStatement(sql.replace(Regex(":\\w+"), "?"))
            // 简单的参数映射逻辑（实际应按名称匹配）
            bindParameters(stmt, sql, func, args) // 使用绑定逻辑
            val rs = stmt.executeQuery()
            mapResultSet(rs, entityClass)
        }

        return if (isFlow) {
            flow {
                InvalidationTracker.tableChanges
                    .filter { it == tableName }
                    .onStart { emit(tableName) }
                    .collect { emit(queryLogic()) }
            }
        } else {
            val list = queryLogic()
            if (returnType.classifier == List::class) list else list.firstOrNull()
        }
    }

    // 辅助：结果集转对象
    private fun <T : Any> mapResultSet(rs: ResultSet, kClass: KClass<T>): List<T> {
        val list = mutableListOf<T>()
        val constructor = kClass.primaryConstructor!!
        while (rs.next()) {
            val args = constructor.parameters.associateWith { param ->
                val raw = rs.getObject(param.name)
                mapValueFromDb(raw, param.type.classifier as KClass<*>)
            }
            list.add(constructor.callBy(args))
        }
        return list
    }

    fun getTableName(kClass: KClass<*>) = kClass.findAnnotation<Entity>()?.tableName.let {
        if (it.isNullOrEmpty()) kClass.simpleName!! else it
    }

    /**
     * 将 Kotlin 对象的值转换为数据库能接受的类型 (写入过程)
     */
    private fun mapValueForDb(value: Any?): Any? {
        if (value == null) return null

        // 1. 优先查找自定义转换器
        for (cvt in allConverters) {
            cvt.encoders[value::class]?.let { return it(value) }
        }

        // 2. 默认处理
        return when (value) {
            is Date -> value.time
            is Boolean -> if (value) 1 else 0
            is Enum<*> -> value.name
            // Float, Double, Int, Long 等数字类型 JDBC 驱动通常能直接处理
            else -> value
        }
    }

    /**
     * 将数据库返回的原始值转换为 Kotlin 属性需要的精确类型 (读取过程)
     */
    private fun mapValueFromDb(value: Any?, target: KClass<*>): Any? {
        if (value == null) return null

        // 优先查找自定义转换器
        for (cvt in allConverters) {
            cvt.decoders[target]?.let { return it(value) }
        }

        // 1. 处理日期
        if (target == Date::class) {
            return when (value) {
                is Number -> Date(value.toLong())
                is String -> Date(value.toLong())
                else -> value
            }
        }

        // 2. 处理数值和基础类型 (核心修复点)
        return when (target) {
            Float::class -> when (value) {
                is Number -> value.toFloat()
                is String -> value.toFloatOrNull() ?: 0.0f
                else -> value
            }

            Double::class -> when (value) {
                is Number -> value.toDouble()
                is String -> value.toDoubleOrNull() ?: 0.0
                else -> value
            }

            Int::class -> when (value) {
                is Number -> value.toInt()
                is String -> value.toIntOrNull() ?: 0
                else -> value
            }

            Long::class -> when (value) {
                is Number -> value.toLong()
                is String -> value.toLongOrNull() ?: 0L
                else -> value
            }

            Boolean::class -> when (value) {
                is Number -> value.toInt() != 0
                is String -> value == "1" || value.equals("true", ignoreCase = true)
                else -> value
            }

            String::class -> value.toString()
            else -> value
        }
    }
}