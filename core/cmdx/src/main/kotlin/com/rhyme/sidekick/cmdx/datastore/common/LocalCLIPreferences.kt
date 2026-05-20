package com.rhyme.sidekick.cmdx.datastore.common

import androidx.compose.runtime.*
import com.rhyme.sidekick.cmdx.framework.CLIPreferences
import kotlin.reflect.KClass

/**
 * Preferences 组合隐式数据
 * 支持 CLIPreferences 的具体子类
 */
object LocalCLIPreferences {
    private val LocalCLIPreferences: ProvidableCompositionLocal<CLIPreferences> =
        staticCompositionLocalOf {
            error("No CLIPreferences Provided. Wrap content with CLIRoot!")
        }

    /**
     * 获取当前的偏好设置实例（基类类型）
     */
    val current: CLIPreferences
        @Composable
        get() = LocalCLIPreferences.current

    /**
     * 获取当前偏好设置实例并转换为具体子类类型
     * 使用示例: val prefs = LocalCLIPreferences.currentAs(MyPreferences::class)
     */
    @Composable
    @Suppress("UNCHECKED_CAST")
    fun <T : CLIPreferences> currentAs(clazz: KClass<T>): T {
        val current = LocalCLIPreferences.current
        return checkNotNull(current as? T) {
            "Expected CLIPreferences of type ${clazz.simpleName}, but got ${current::class.simpleName}"
        }
    }

    /**
     * Associates a [LocalCLIPreferences] key to a value in a call to
     * [CompositionLocalProvider].
     * 支持传入 CLIPreferences 的任何子类实例
     */
    infix fun provides(preferences: CLIPreferences):
            ProvidedValue<CLIPreferences> {
        return LocalCLIPreferences.provides(preferences)
    }
}