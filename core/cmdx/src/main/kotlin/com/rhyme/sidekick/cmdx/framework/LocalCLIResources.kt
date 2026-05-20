package com.rhyme.sidekick.cmdx.framework

import androidx.compose.runtime.*
import kotlin.reflect.KClass

/**
 * CLIResources 组合隐式数据
 * 支持 CLIResources 的具体子类
 */
object LocalCLIResources {
    private val LocalCLIResources: ProvidableCompositionLocal<CLIResources> =
        staticCompositionLocalOf {
            error("No CLIResources Provided. Warp content with CLIRoot!")
        }

    /**
     * 获取当前的资源实例（基类类型）
     */
    val current: CLIResources
        @Composable
        get() = LocalCLIResources.current

    /**
     * 获取当前资源实例并转换为具体子类类型
     * 使用示例: val resources = LocalCLIResources.currentAs(MyResources::class)
     */
    @Composable
    @Suppress("UNCHECKED_CAST")
    fun <T : CLIResources> currentAs(clazz: KClass<T>): T {
        val current = LocalCLIResources.current
        return checkNotNull(current as? T) {
            "Expected CLIResources of type ${clazz.simpleName}, but got ${current::class.simpleName}"
        }
    }

    /**
     * Associates a [LocalCLIResources] key to a value in a call to
     * [CompositionLocalProvider].
     * 支持传入 CLIResources 的任何子类实例
     */
    infix fun provides(resources: CLIResources): ProvidedValue<CLIResources> {
        return LocalCLIResources.provides(resources)
    }
}