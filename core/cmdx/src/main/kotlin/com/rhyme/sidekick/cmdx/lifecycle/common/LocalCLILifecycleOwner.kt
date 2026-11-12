package com.rhyme.sidekick.cmdx.lifecycle.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * 生命周期组合隐式数据
 */
object LocalCLILifecycleOwner {
    private val LocalOwner: ProvidableCompositionLocal<CLILifecycleOwner> =
        staticCompositionLocalOf {
            error("No CLILifecycleOwner Provided. Wrap content with CLIAndropilotRoot!")
        }

    val current: CLILifecycleOwner
        @Composable
        get() = LocalOwner.current

    /**
     * Associates a [LocalCLILifecycleOwner] key to a value in a call to
     * [androidx.compose.runtime.CompositionLocalProvider].
     */
    infix fun provides(lifecycleOwner: CLILifecycleOwner): ProvidedValue<CLILifecycleOwner> {
        return LocalOwner.provides(lifecycleOwner)
    }
}
