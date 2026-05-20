package com.rhyme.sidekick.andropilot.ui.screens.docli

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.jakewharton.mosaic.LocalTerminalState
import com.jakewharton.mosaic.layout.background
import com.jakewharton.mosaic.layout.fillMaxWidth
import com.jakewharton.mosaic.layout.padding
import com.jakewharton.mosaic.layout.width
import com.jakewharton.mosaic.modifier.Modifier
import com.jakewharton.mosaic.text.SpanStyle
import com.jakewharton.mosaic.text.buildAnnotatedString
import com.jakewharton.mosaic.text.withStyle
import com.jakewharton.mosaic.ui.*
import com.rhyme.sidekick.andropilot.ui.theme.CLITheme
import com.rhyme.sidekick.cmdx.lifecycle.common.LocalCLILifecycleOwer
import com.rhyme.sidekick.cmdx.navigation.simple.LocalCLINavigator
import com.rhyme.sidekick.cmdx.viewmodel.common.cliViewModel
import com.rhyme.sidekick.cmdx.viewmodel.core.ViewModelStore
import com.rhyme.sidesick.ui.*
import kotlin.system.exitProcess

/**
 * 项目名：SideKickCLI
 * 包名：com.rhyme.sidekick.andropilot.ui.screens.docli
 *
 * @author 李春俊-Rhyme
 * @since 11:52 2026/5/10 星期日
 * @version 1.0
 */

/**
 * Docli 搜索界面
 * 模仿 wiki-tui 的布局风格：顶部搜索框 + 中间搜索结果列表/详情页 + 底部状态栏
 */
@Composable
fun DocliScreen() {
    val lifecycleOwner = LocalCLILifecycleOwer.current
    val navigator = LocalCLINavigator.current
    val terminalState = LocalTerminalState.current

    // 获取当前命令行尺寸
    val terminalSize = terminalState.size
    val terminalColumns = terminalSize.columns // 获取宽度

    // 计算内容区域的宽度（确保最小宽度）
    val contentFixWidth = maxOf(40, (terminalColumns * 0.65).toInt())  // 最小 40 字符，保证基本可读性

    // 在 Compose 中使用 cliViewModel 获取 ViewModel
    val docliVm: DocliViewModel = cliViewModel("docli") { ViewModelStore() }

    // 收集 UI State - 克服粘性，只响应最新状态
    val uiState by docliVm.uiState.collectAsState()
    val searchBoxState by docliVm.searchBoxState.collectAsState()
    val resultListState by docliVm.resultListState.collectAsState()
    val detailPageState by docliVm.detailPageState.collectAsState()

    // 初始化：显示空列表，等待用户输入搜索
    LaunchedEffect(Unit) {
        // 初始化为空列表
        docliVm.sendAction(DocliUiAction.Initialize(emptyList()))
    }

    DocliActionBox(modifier = Modifier, uiStatus = uiState.status) { actionFlow ->
        // 收集 DocliActionBox 发出的用户操作并转发给 ViewModel
        LaunchedEffect(actionFlow) {
            actionFlow.collect { action ->
                docliVm.sendAction(action)
            }
        }


        Column(
            verticalArrangement = Arrangement.Top, horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ========== 顶部：搜索输入框 ==========
            BorderedInputBox(
                modifier = Modifier.fillMaxWidth(),
                value = searchBoxState.searchQuery,
                focus = searchBoxState.focus,
                cursorPosition = searchBoxState.cursorPosition,
                placeholder = "Search Documents...",
                minWidth = (terminalColumns * 0.45).toInt(),
                maxWidth = (terminalColumns * 0.95).toInt()
            )

            // ========== 中间：根据状态显示列表、详情或加载中 ==========
            when (uiState.status) {
                DocliUiStatus.Loading -> {
                    // 显示加载状态
                    Box(
                        modifier = Modifier
                            .width(contentFixWidth)
                            .border(color = CLITheme.colorScheme.divider),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            value = "Loading...",
                            color = CLITheme.colorScheme.primary,
                            textStyle = TextStyle.Bold
                        )
                    }
                }

                else -> {
                    // 查看模式或输入模式
                    if (detailPageState.focus) {
                        // 显示详情页
                        renderDetailPage(detailPageState, contentWidth = contentFixWidth)
                    } else {
                        // 显示结果列表
                        renderResultList(resultListState, contentWidth = contentFixWidth)
                    }
                }
            }

            // ========== 底部：状态栏 ==========
            renderStatusBar(uiState, resultListState, detailPageState)
        }

    }

    // 处理副作用（导航、退出等）
    LaunchedEffect(uiState) {
        // 处理退出
        if (uiState.pendingExit) {
            lifecycleOwner.toTerminated()
            exitProcess(0)
        }

        // 处理确认导航
        if (uiState.pendingNavigation) {
            val selectedDoc = detailPageState.docDetail
            if (selectedDoc.title.isNotEmpty()) {
                // TODO: 导航到文档详情页面
                println("Navigating to document: ${selectedDoc.title}")
                // navigator.navigate("doc_detail_${selectedDoc.title}")
            }
            // 清除导航标志
            docliVm.sendAction(DocliUiAction.ClearPendingActions)
        }

        // 处理帮助显示
        if (uiState.showingHelp) {
            println("Showing help...")
            // TODO: 显示帮助信息
            docliVm.sendAction(DocliUiAction.ClearPendingActions)
        }
    }
}

/**
 * 渲染结果列表
 */
@Composable
private fun renderResultList(resultListState: DocliUiResultListState, contentWidth: Int) {
    BorderedTitledBox(
        modifier = Modifier.width(contentWidth),
        title = "Results",
        titleColor = CLITheme.colorScheme.primary,
        borderColor = CLITheme.colorScheme.divider
    ) {
        if (resultListState.resultList.isEmpty()) {
            // 空列表提示
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(2),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    value = "No results. Press 'i' to search.",
                    color = CLITheme.color.BrightBlack
                )
            }
        } else {
            Column {
                resultListState.resultList.forEachIndexed { index, doc ->
                    if (index == resultListState.selectedIndex && resultListState.focus) {
                        // 当前选中的项：使用背景高亮
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(CLITheme.colorScheme.primary)
                                .padding(horizontal = 1)
                        ) {
                            Column {
                                WrappingText(
                                    value = " ${doc.title}",
                                    maxWidth = contentWidth - 4,
                                    color = CLITheme.colorScheme.surface,
                                    textStyle = TextStyle.Bold
                                )
                                WrappingText(
                                    value = "  ${doc.description.take(150)}...",
                                    maxWidth = contentWidth - 6,
                                    color = CLITheme.colorScheme.surface
                                )
                            }
                        }
                    } else {
                        // 普通项：标题红色高亮
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 1)
                        ) {
                            WrappingText(
                                value = "  ${doc.title}",
                                maxWidth = contentWidth - 4,
                                color = CLITheme.color.BrightRed,
                                textStyle = TextStyle.Bold
                            )
                            WrappingText(
                                value = "  ${doc.description.take(150)}...",
                                maxWidth = contentWidth - 6,
                                color = CLITheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 渲染详情页
 */
@Composable
private fun renderDetailPage(detailPageState: DocliUiDetailPageState, contentWidth: Int) {
    Box {
        Column(modifier = Modifier.padding(horizontal = 1)) {
            // 文档标题（使用WrappingText）
            WrappingText(
                value = "Title: ${detailPageState.docDetail.title}",
                maxWidth = contentWidth - 2,
                color = CLITheme.color.BrightGreen,
                textStyle = TextStyle.Bold
            )
            // 空行
            Text(value = "")

            // 文档描述（使用WrappingText）
            Text(
                value = "Description:",
                color = CLITheme.colorScheme.primary,
                textStyle = TextStyle.Bold
            )
            WrappingText(
                value = "  ${detailPageState.docDetail.description}",
                maxWidth = contentWidth - 2,
                color = CLITheme.colorScheme.onSurface
            )
            // 空行
            Text(value = "")

            // 详细内容
            Text(
                value = "Content:",
                color = CLITheme.colorScheme.primary,
                textStyle = TextStyle.Bold
            )

            val terminalState = LocalTerminalState.current
            val terminalRows = terminalState.size.rows

            // 调整：增大安全边距，防止动态高度导致的边框溢出
            val reservedLines = 12
            val availableHeight = maxOf(5, terminalRows - reservedLines)

            LongTextList(
                text = detailPageState.docDetail.rawContent,
                maxWidth = contentWidth - 2,
                scrollPercentage = detailPageState.scrollPercentage,
                visibleLines = availableHeight,  // 动态传入可见行数
                lineColor = CLITheme.colorScheme.onSurface
            )
        }
    }
}

/**
 * 渲染状态栏
 */
@Composable
private fun renderStatusBar(
    uiState: DocliUiState, resultListState: DocliUiResultListState, detailPageState: DocliUiDetailPageState
) {
    Text(
        value = buildAnnotatedString {
            withStyle(SpanStyle(CLITheme.colorScheme.primary)) {
                append("docli-tui")
            }
            append(" | ")

            // 根据当前焦点显示不同信息
            if (detailPageState.focus) {
                // 详情页模式
                withStyle(SpanStyle(CLITheme.colorScheme.primary)) {
                    append("View: '")
                }
                withStyle(SpanStyle(CLITheme.color.BrightCyan)) {
                    append("Detail")
                }
                withStyle(SpanStyle(CLITheme.colorScheme.primary)) {
                    append("'")
                }
            } else {
                // 列表页模式
                withStyle(SpanStyle(CLITheme.colorScheme.primary)) {
                    append("Results: '")
                }
                withStyle(SpanStyle(CLITheme.color.BrightGreen)) {
                    append("${resultListState.resultList.size}'")
                }
                withStyle(SpanStyle(CLITheme.colorScheme.primary)) {
                    append(" | Selected: '")
                }
                withStyle(SpanStyle(CLITheme.color.BrightCyan)) {
                    append("${resultListState.selectedIndex + 1}/${resultListState.resultList.size}")
                }
                withStyle(SpanStyle(CLITheme.colorScheme.primary)) {
                    append("'")
                }
            }

            append(" | ")
            withStyle(SpanStyle(CLITheme.colorScheme.primary)) {
                append("[s]earch [p]age [i]nput [Ctrl+q]exit")
            }
        },
        color = CLITheme.colorScheme.onSurface,
        background = CLITheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    )
}
