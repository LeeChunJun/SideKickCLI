package com.rhyme.sidekick.andropilot

import com.jakewharton.mosaic.NonInteractivePolicy
import com.jakewharton.mosaic.runMosaicBlocking
import com.jakewharton.mosaic.runMosaicMain
import com.rhyme.sidekick.andropilot.config.CLIConfigImpl
import com.rhyme.sidekick.andropilot.config.CLIResourcesImpl
import com.rhyme.sidekick.andropilot.data.local.database.AndropilotDatabase
import com.rhyme.sidekick.andropilot.data.local.preferences.CLIPreferencesImpl
import com.rhyme.sidekick.andropilot.ui.navigation.CLIAndropilotRoot
import com.rhyme.sidekick.andropilot.ui.navigation.CLIAndropilotRouter
import com.rhyme.sidekick.andropilot.ui.theme.CLITheme
import com.rhyme.sidekick.cmdx.framework.CLIContextImpl
import com.rhyme.sidekick.cmdx.framework.CLIServiceManager
import com.rhyme.sidekick.cmdx.lifecycle.common.CLILifecycleOwner
import com.rhyme.sidekick.cmdx.navigation.simple.CLINavigator
import com.rhyme.sidekick.cmdx.viewmodel.common.CLIViewModelStoreOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

/**
 * 项目名：SideKickCLI
 * 包名：com.rhyme.sidekick.andropilot
 * 描述：Your Copilot for Android Command Line
 *
 * @author 李春俊-Rhyme
 * @since 18:11 2026/4/22 星期三
 * @version 1.0
 */
class Andropilot {

    /**
     * 创建程序上下文实现
     */
    private fun createContext(): CLIContextImpl {
        val config = CLIConfigImpl.load()
        return CLIContextImpl(
            config = config,
            resources = CLIResourcesImpl(config = config),
            preferences = CLIPreferencesImpl(),
            database = AndropilotDatabase(),
            navigator = CLINavigator("docli"), // 初始加载菜单页面
            lifecycleOwner = CLILifecycleOwner(),
            viewModelStoreOwner = CLIViewModelStoreOwner(),
            serviceManager = CLIServiceManager(),
            coroutineScope = CoroutineScope(Dispatchers.Default)
        )
    }

    /**
     * 程序主入口
     */
    fun main() {
        val context = createContext()
        context.lifecycleOwner.toInitialized() // 程序初始化
        try {
            runMosaicBlocking(onNonInteractive = NonInteractivePolicy.Throw) {
                CLIAndropilotRoot(context) {
                    CLITheme(darkTheme = true) {
                        CLIAndropilotRouter()
                    }
                }
            }
        } finally {
            // 释放各种资源和内存
            context.viewModelStoreOwner.clear()
            context.database.close()
            context.lifecycleOwner.toTerminated()
        }
    }

}