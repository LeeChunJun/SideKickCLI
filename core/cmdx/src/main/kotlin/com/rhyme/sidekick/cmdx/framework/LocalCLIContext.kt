package com.rhyme.sidekick.cmdx.framework

import androidx.compose.runtime.*
import kotlin.reflect.KClass

/**
 * CLIContext 组合隐式数据
 * 支持 CLIContext 的具体子类
 */
object LocalCLIContext {
    private val LocalCLIContext: ProvidableCompositionLocal<CLIContext> =
        staticCompositionLocalOf {
            error("No CLIContext Provided. Warp content with CLIRoot!")
        }

    /**
     * 获取当前的上下文实例（基类类型）
     */
    val current: CLIContext
        @Composable
        get() = LocalCLIContext.current

    /**
     * 获取当前上下文实例并转换为具体子类类型
     * 使用示例: val context = LocalCLIContext.currentAs(MyContext::class)
     */
    @Composable
    @Suppress("UNCHECKED_CAST")
    fun <T : CLIContext> currentAs(clazz: KClass<T>): T {
        val current = LocalCLIContext.current
        return checkNotNull(current as? T) {
            "Expected CLIContext of type ${clazz.simpleName}, but got ${current::class.simpleName}"
        }
    }

    /**
     * Associates a [LocalCLIContext] key to a value in a call to
     * [CompositionLocalProvider].
     * 支持传入 CLIContext 的任何子类实例
     */
    infix fun provides(context: CLIContext): ProvidedValue<CLIContext> {
        return LocalCLIContext.provides(context)
    }
}