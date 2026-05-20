package com.rhyme.sidekick.cmdx.viewmodel

import androidx.compose.runtime.*
import com.jakewharton.mosaic.ui.Text
import com.rhyme.sidekick.cmdx.viewmodel.common.CLIViewModelStoreOwner
import com.rhyme.sidekick.cmdx.viewmodel.common.LocalCLIViewModelStoreOwner
import com.rhyme.sidekick.cmdx.viewmodel.common.cliViewModel
import com.rhyme.sidekick.cmdx.viewmodel.core.ViewModelStore

/**
 * Compose 示例：如何在 Compose 层提供并使用 CLIViewModelStoreOwner + cliViewModel
 *
 * 说明：示例使用了 UserViewModel（位于同一 tdd 包），并将其 state 转为 Compose 可观察的状态。
 */

@Composable
fun CLIRoot(content: @Composable () -> Unit) {
    // root owner，记得在合适的生命周期点 clear()（这里为示例演示）
    val owner = remember { CLIViewModelStoreOwner() }

    CompositionLocalProvider(
        LocalCLIViewModelStoreOwner provides owner,
        content = content
    )
}

@Composable
fun UserScreen() {
    // 在 Compose 中使用 cliViewModel 获取 ViewModel（会在内部使用提供的 ViewModelStore）
    val userVm: UserViewModel = cliViewModel("user") { ViewModelStore() }

    // 将 StateFlow 转为 Compose 状态
    val state by userVm.state.collectAsState(initial = "Loading...")

    // 简单展示
    Text(value = state)
}

// Usage example in an application entry (pseudo code):
@Composable
fun App() {
    CLIRoot {
        UserScreen()
    }
}
