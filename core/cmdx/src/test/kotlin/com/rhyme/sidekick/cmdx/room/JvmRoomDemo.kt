package com.rhyme.sidekick.cmdx.room

import com.rhyme.sidekick.cmdx.room.common.CLIRoomMigration
import com.rhyme.sidekick.cmdx.room.common.CLIRoomTypeConverter
import com.rhyme.sidekick.cmdx.room.common.TypeConverter
import com.rhyme.sidekick.cmdx.room.runtime.Dao
import com.rhyme.sidekick.cmdx.room.runtime.Entity
import com.rhyme.sidekick.cmdx.room.runtime.Insert
import com.rhyme.sidekick.cmdx.room.runtime.CLIRoomDatabase
import com.rhyme.sidekick.cmdx.room.runtime.PrimaryKey
import com.rhyme.sidekick.cmdx.room.runtime.Query
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import java.io.File
import java.nio.file.Paths
import java.util.Date

// 0. 定义转换器类
class AppTypeConverters : CLIRoomTypeConverter() {

    @TypeConverter
    fun fromLocation(loc: Location): String {
        return "${loc.lat},${loc.lng}"
    }

    @TypeConverter
    fun toLocation(data: String): Location {
        val parts = data.split(",")
        return Location(parts[0].toDouble(), parts[1].toDouble())
    }

    // 你还可以继续在这里写 Date 转 String，或者 List 转 JSON...
}

// 1. 定义实体
@Entity(tableName = "users")
data class User(
    @PrimaryKey val id: Int,
    val name: String,
    val birthday: Date, // 测试类型转换
    val location: Location, // 测试转换器
    val age: Int = 0 // 新增字段
)

data class Location(val lat: Double, val lng: Double)

// 2. 定义 DAO
@Dao
interface UserDao {
    @Insert
    fun insert(user: User): Long

    @Query("DELETE FROM users WHERE name = :name AND id = :id")
    fun deleteSpecific(name: String, id: Int)

    @Query("SELECT * FROM users")
    fun getAll(): List<User>

    @Query("SELECT * FROM users")
    fun getAllFlow(): Flow<List<User>>

    // 支持 Flow 单个对象 (增强解析)
    @Query("SELECT * FROM users WHERE id = :id")
    fun observeUserById(id: Int): Flow<User?>

    @Query("DELETE FROM users WHERE id = :id")
    fun deleteById(id: Int)

    @Query("INSERT OR REPLACE INTO users (id, name, birthday, location, age) VALUES (:id, :name, :birthday, :location, :age)")
    fun insertManual(id: Int, name: String, birthday: Date, location: Location, age: Int): Long
}

// 3. 定义数据库
class AppDatabase : CLIRoomDatabase() {
    val userDao: UserDao by lazy { getDao<UserDao>() }
}

fun main() = runBlocking {
    File("test.db").delete()

    val db = AppDatabase()

    // 1. 定义从版本 1 到 2 的迁移逻辑
    val MIGRATION_1_2 = CLIRoomMigration(1, 1) { conn ->
        // 执行 SQL 增加列
        conn.createStatement().execute("ALTER TABLE users ADD COLUMN age INTEGER NOT NULL DEFAULT 0")
        println("迁移脚本执行完毕：已增加 age 字段")
    }

    // 注册整个转换器类
    db.addConverter(AppTypeConverters())
    // 迁移数据库
    db.addMigrations(MIGRATION_1_2)

    db.init(Paths.get("test.db"), listOf(User::class), version = 2)
    val dao = db.userDao

    // 测试 Flow 观察
    val job = launch {
        println("观察者: 等待数据中...")
        dao.getAllFlow().collect { users ->
            println("观察者: 数据库已更新，当前总数: ${users.size}")
            users.forEach { println("  - $it") }
        }
    }

    delay(1000)

    // 4. 执行增删改查
    println("\n操作: 插入用户...")
    dao.insert(User(301, "Alice", Date(), Location(39.9042, 116.4074), age = 12)) // 北京坐标
    delay(500)

    println("\n操作: 插入另一个用户...")
    dao.insert(User(302, "Bob", Date(), Location(31.2304, 121.4737), age = 22)) // 上海坐标
    delay(500)

    println("\n操作: 插入第三个用户...")
    val rhyme = User(303, "Rhyme", Date(), Location(22.5431, 114.0579), age = 23) // 深圳坐标
    dao.insertManual(rhyme.id, rhyme.name, rhyme.birthday, rhyme.location, rhyme.age)
    delay(500)

    println("\n操作: 删除用户301...")
    dao.deleteById(301)
    delay(500)

    println("\n操作: 删除用户303...")
    dao.deleteSpecific("Rhyme", 303)
    delay(500)

    println("\n操作: 同步查询所有...")
    val list = dao.getAll()
    println("同步结果: $list")

    delay(1000)
    job.cancel()
    println("Demo 结束")
}