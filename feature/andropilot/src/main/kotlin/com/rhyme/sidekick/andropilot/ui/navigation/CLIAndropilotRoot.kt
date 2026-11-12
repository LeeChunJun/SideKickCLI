package com.rhyme.sidekick.andropilot.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.rhyme.sidekick.andropilot.ui.screens.ErrorScreen
import com.rhyme.sidekick.andropilot.ui.screens.docli.DocliScreen
import com.rhyme.sidekick.cmdx.datastore.common.LocalCLIPreferences
import com.rhyme.sidekick.cmdx.framework.CLIContext
import com.rhyme.sidekick.cmdx.framework.LocalCLIConfig
import com.rhyme.sidekick.cmdx.framework.LocalCLIResources
import com.rhyme.sidekick.cmdx.framework.LocalCLIServiceManager
import com.rhyme.sidekick.cmdx.lifecycle.common.LocalCLILifecycleOwner
import com.rhyme.sidekick.cmdx.navigation.simple.LocalCLINavigator
import com.rhyme.sidekick.cmdx.room.common.LocalCLIRoomDatabase
import com.rhyme.sidekick.cmdx.viewmodel.common.LocalCLIViewModelStoreOwner

/**
 * 项目名：SideKickCLI
 * 包名：com.rhyme.sidekick.andropilot.ui.navigation
 *
 * @author 李春俊-Rhyme
 * @since 20:26 2026/5/7 星期四
 * @version 1.0
 */
@Composable
fun CLIAndropilotRoot(
    context: CLIContext,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalCLIConfig provides context.config,
        LocalCLIResources provides context.resources,
        LocalCLIPreferences provides context.preferences,
        LocalCLIRoomDatabase provides context.database,
        LocalCLINavigator provides context.navigator,
        LocalCLILifecycleOwner provides context.lifecycleOwner,
        LocalCLIViewModelStoreOwner provides context.viewModelStoreOwner,
        LocalCLIServiceManager provides context.serviceManager,
        content = content
    )
}

@Composable
fun CLIAndropilotRouter() {
    val cliNavigator = LocalCLINavigator.current
    val backStack by cliNavigator.backStack.collectAsState()
    val currentRoute = backStack.last().route

    when {
        currentRoute == "docli" -> DocliScreen()
        else -> ErrorScreen()
    }
}