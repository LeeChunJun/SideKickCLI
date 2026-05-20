package com.rhyme.sidekick.cmdx.navigation.simple

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * 导航组合隐式数据
 */
object LocalCLINavigator {
    private val LocalCLINavigator: ProvidableCompositionLocal<CLINavigator> =
        staticCompositionLocalOf {
            error("No CLINavigator Provided. Warp content with CLIRoot!")
        }

    val current: CLINavigator
        @Composable
        get() = LocalCLINavigator.current

    /**
     * Associates a [LocalCLINavigator] key to a value in a call to
     * [CompositionLocalProvider].
     */
    infix fun provides(cliNavigator: CLINavigator): ProvidedValue<CLINavigator> {
        return LocalCLINavigator.provides(cliNavigator)
    }
}