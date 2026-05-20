package com.rhyme.sidekick.quickgrok

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.rhyme.sidekick.cmdx.viewmodel.common.CLIViewModelStoreOwner
import com.rhyme.sidekick.cmdx.viewmodel.common.LocalCLIViewModelStoreOwner
import com.rhyme.sidekick.cmdx.viewmodel.common.cliViewModel
import com.rhyme.sidekick.cmdx.viewmodel.core.ViewModelStore
import com.rhyme.sidekick.quickgrok.presentation.QuickGrokUiEvent
import com.rhyme.sidekick.quickgrok.presentation.QuickGrokUiState
import com.rhyme.sidekick.quickgrok.presentation.QuickGrokViewModel
import com.rhyme.sidekick.quickgrok.ui.components.ProjectList
import com.rhyme.sidekick.quickgrok.ui.components.ResultsSection
import com.rhyme.sidekick.quickgrok.ui.components.SearchSection

/**
 * 主函数 - 启动QuickGrok应用
 */
fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "QuickGrok - Code Search",
        state = rememberWindowState(
            placement = WindowPlacement.Maximized
        )
    ) {
        MaterialTheme(
            colorScheme = lightColorScheme(
                primary = Color(0xFF2a52be),
                secondary = Color(0xFF4f6ae5),
                background = Color(0xFFf0f2f5),
                surface = Color(0xFFf5f7fa)
            )
        ) {
            QuickGrokRoot {
                QuickGrokApp()
            }
        }
    }
}

@Composable
fun QuickGrokRoot(content: @Composable () -> Unit) {
    val owner = remember { CLIViewModelStoreOwner() }

    CompositionLocalProvider(
        LocalCLIViewModelStoreOwner provides owner,
        content = content
    )
}

@Composable
fun QuickGrokApp() {
    // 使用 cmdx 的 cliViewModel 获取 ViewModel 实例
    val viewModel: QuickGrokViewModel = cliViewModel("quickgrok") { ViewModelStore() }
    val uiState by viewModel.uiState.collectAsState()

    // 初始化时加载项目列表
    LaunchedEffect(Unit) {
        viewModel.onEvent(QuickGrokUiEvent.LoadProjects)
    }

    Row(
        modifier = Modifier.fillMaxSize()
    ) {
        // 左侧边栏 - 项目列表
        SidebarPanel(
            uiState = uiState,
            onEvent = viewModel::onEvent,
            modifier = Modifier.width(320.dp)
        )

        // 主内容区
        MainContentArea(
            uiState = uiState,
            onEvent = viewModel::onEvent,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun SidebarPanel(
    uiState: QuickGrokUiState,
    onEvent: (QuickGrokUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxHeight(),
        color = Color(0xFFffffff).copy(alpha = 0.92f),
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Logo
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(Color(0xFF2a52be), Color(0xFF4f6ae5))
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .rotate(45f)
                )
                Text(
                    text = "QUICKGROK",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 项目列表标题
            Text(
                text = "SOURCE REPOSITORIES",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF6b6b70),
                fontWeight = FontWeight.Bold
            )

            // 项目列表
            when (uiState) {
                is QuickGrokUiState.Success -> {
                    ProjectList(
                        projects = uiState.projects,
                        selectedProjects = uiState.selectedProjects,
                        onToggleProject = { onEvent(QuickGrokUiEvent.ToggleProject(it)) }
                    )
                }

                else -> {
                    Text(
                        text = "Loading projects...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF6b6b70)
                    )
                }
            }
        }
    }
}

@Composable
fun MainContentArea(
    uiState: QuickGrokUiState,
    onEvent: (QuickGrokUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = Color(0xFFf0f2f5)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp, vertical = 24.dp)
        ) {
            // 搜索区域 - 固定大小
            SearchSection(
                uiState = uiState,
                onEvent = onEvent,
                modifier = Modifier.wrapContentHeight()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 搜索结果 - 占据剩余空间
            ResultsSection(
                uiState = uiState,
                modifier = Modifier.weight(1f).fillMaxWidth()
            )
        }
    }
}
