package com.rhyme.sidekick.cmdx.framework

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * 服务管理器组合隐式数据
 */
object LocalCLIServiceManager {
    private val LocalCLIServiceManager: ProvidableCompositionLocal<CLIServiceManager> =
        staticCompositionLocalOf {
            error("No CLIServiceManager Provided. Wrap content with appropriate provider!")
        }

    val current: CLIServiceManager
        @Composable
        get() = LocalCLIServiceManager.current

    /**
     * Associates a [LocalCLIServiceManager] key to a value in a call to
     * [CompositionLocalProvider].
     */
    infix fun provides(cliServiceManager: CLIServiceManager): ProvidedValue<CLIServiceManager> {
        return LocalCLIServiceManager.provides(cliServiceManager)
    }
}