package com.rhyme.sidekick.cmdx.framework

import androidx.compose.runtime.*
import kotlin.reflect.KClass

/**
 * CLIConfig 组合隐式数据
 * 支持 CLIConfig 的具体子类
 */
object LocalCLIConfig {
    private val LocalCLIConfig: ProvidableCompositionLocal<CLIConfig> =
        staticCompositionLocalOf {
            error("No CLIConfig Provided. Warp content with CLIRoot!")
        }

    /**
     * 获取当前的配置实例（基类类型）
     */
    val current: CLIConfig
        @Composable
        get() = LocalCLIConfig.current

    /**
     * 获取当前配置实例并转换为具体子类类型
     * 使用示例: val config = LocalCLIConfig.currentAs(MyConfig::class)
     */
    @Composable
    @Suppress("UNCHECKED_CAST")
    fun <T : CLIConfig> currentAs(clazz: KClass<T>): T {
        val current = LocalCLIConfig.current
        return checkNotNull(current as? T) {
            "Expected CLIConfig of type ${clazz.simpleName}, but got ${current::class.simpleName}"
        }
    }

    /**
     * Associates a [LocalCLIConfig] key to a value in a call to
     * [CompositionLocalProvider].
     * 支持传入 CLIConfig 的任何子类实例
     */
    infix fun provides(config: CLIConfig): ProvidedValue<CLIConfig> {
        return LocalCLIConfig.provides(config)
    }
}