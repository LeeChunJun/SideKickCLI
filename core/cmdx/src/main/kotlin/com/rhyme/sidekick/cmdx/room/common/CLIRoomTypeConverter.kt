package com.rhyme.sidekick.cmdx.room.common

import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.functions
import kotlin.reflect.full.valueParameters

/**
 * 类型转换器注解
 */
@Target(AnnotationTarget.FUNCTION)
annotation class TypeConverter

/**
 * 转换器基类：支持自动扫描注解方法
 */
abstract class CLIRoomTypeConverter {
    // 存入数据库：Kotlin类型 -> (对象 -> 数据库格式)
    val encoders = mutableMapOf<KClass<*>, (Any?) -> Any?>()
    // 从数据库读取：目标Kotlin类型 -> (数据库格式 -> 对象)
    val decoders = mutableMapOf<KClass<*>, (Any?) -> Any?>()

    init {
        // 自动扫描子类中所有带 @TypeConverter 的函数
        this::class.functions.forEach { func ->
            if (func.findAnnotation<TypeConverter>() != null) {
                registerConverter(func)
            }
        }
    }

    private fun registerConverter(func: KFunction<*>) {
        val params = func.valueParameters
        if (params.size != 1) return // 转换函数必须只有一个参数

        val inputType = params[0].type.classifier as? KClass<*> ?: return
        val returnType = func.returnType.classifier as? KClass<*> ?: return

        // 判断逻辑：
        // 如果返回类型是数据库基础类型（String, Long, Int, Double, Float），它通常是 Encoder
        val dbBaseTypes = listOf(String::class, Long::class, Int::class, Double::class, Float::class, ByteArray::class)

        if (dbBaseTypes.contains(returnType)) {
            // 这是从 Kotlin 到 数据库 的转换
            encoders[inputType] = { obj -> func.call(this, obj) }
        } else {
            // 这是从 数据库 到 Kotlin 的转换
            decoders[returnType] = { dbObj -> func.call(this, dbObj) }
        }
    }
}