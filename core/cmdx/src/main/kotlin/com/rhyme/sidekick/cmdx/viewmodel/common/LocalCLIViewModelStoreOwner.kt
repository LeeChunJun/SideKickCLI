package com.rhyme.sidekick.cmdx.viewmodel.common

import androidx.compose.runtime.*

/**
 * ViewModelStoreOwer 组合隐式数据
 */
object LocalCLIViewModelStoreOwner {
    private val LocalCLIViewModelStoreOwner: ProvidableCompositionLocal<CLIViewModelStoreOwner> =
        staticCompositionLocalOf {
            error("No CLIViewModelStoreOwner Provided. Warp content with CLIRoot!")
        }

    val current: CLIViewModelStoreOwner
        @Composable
        get() = LocalCLIViewModelStoreOwner.current

    /**
     * Associates a [LocalCLIViewModelStoreOwner] key to a value in a call to
     * [CompositionLocalProvider].
     */
    infix fun provides(viewModelStoreOwner: CLIViewModelStoreOwner):
            ProvidedValue<CLIViewModelStoreOwner> {
        return LocalCLIViewModelStoreOwner.provides(viewModelStoreOwner)
    }
}