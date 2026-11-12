package com.rhyme.sidekick.cmdx.lifecycle.common

import androidx.compose.runtime.*

/**
 * Lifecycle 组合隐式数据
 */
object LocalCLILifecycleOwer {
    private val LocalCLILifecycleOwer: ProvidableCompositionLocal<CLILifecycleOwner> =
        staticCompositionLocalOf {
            error("No CLILifecycleOwner Provided. Warp content with CLIRoot!")
        }

    val current: CLILifecycleOwner
        @Composable
        get() = LocalCLILifecycleOwer.current

    /**
     * Associates a [LocalCLILifecycleOwer] key to a value in a call to
     * [CompositionLocalProvider].
     */
    infix fun provides(lifecycleOwner: CLILifecycleOwner):
            ProvidedValue<CLILifecycleOwner> {
        return LocalCLILifecycleOwer.provides(lifecycleOwner)
    }
}