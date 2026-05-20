package com.rhyme.sidekick.cmdx.room.common

import androidx.compose.runtime.*
import com.rhyme.sidekick.cmdx.room.runtime.CLIRoomDatabase
import kotlin.reflect.KClass

/**
 * Database 组合隐式数据
 * 支持 JvmRoomDatabase 的具体子类
 */
object LocalCLIRoomDatabase {
    private val LocalCLIRoomDatabase: ProvidableCompositionLocal<CLIRoomDatabase> =
        staticCompositionLocalOf {
            error("No JvmRoomDatabase Provided. Warp content with CLIRoot!")
        }

    /**
     * 获取当前的数据库实例（基类类型）
     */
    val current: CLIRoomDatabase
        @Composable
        get() = LocalCLIRoomDatabase.current

    /**
     * 获取当前数据库实例并转换为具体子类类型
     * 使用示例: val db = LocalJvmRoomDatabase.currentAs(MyDatabase::class)
     */
    @Composable
    @Suppress("UNCHECKED_CAST")
    fun <T : CLIRoomDatabase> currentAs(clazz: KClass<T>): T {
        val current = LocalCLIRoomDatabase.current
        return checkNotNull(current as? T) {
            "Expected JvmRoomDatabase of type ${clazz.simpleName}, but got ${current::class.simpleName}"
        }
    }

    /**
     * Associates a [LocalCLIRoomDatabase] key to a value in a call to
     * [CompositionLocalProvider].
     * 支持传入 JvmRoomDatabase 的任何子类实例
     */
    infix fun provides(database: CLIRoomDatabase):
            ProvidedValue<CLIRoomDatabase> {
        return LocalCLIRoomDatabase.provides(database)
    }
}
