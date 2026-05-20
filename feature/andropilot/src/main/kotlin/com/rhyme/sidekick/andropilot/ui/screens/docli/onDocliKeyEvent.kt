package com.rhyme.sidekick.andropilot.ui.screens.docli

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.rememberCoroutineScope
import com.jakewharton.mosaic.layout.KeyEvent
import com.jakewharton.mosaic.layout.onKeyEvent
import com.jakewharton.mosaic.modifier.Modifier
import com.jakewharton.mosaic.ui.Box
import com.rhyme.sidekick.andropilot.ui.components.Direction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

/**
 * 项目名：SideKickCLI
 * 包名：com.rhyme.sidekick.andropilot.ui.screens.docli
 *
 * @author 李春俊-Rhyme
 * @since 11:54 2026/5/10 星期日
 * @version 1.0
 */

/**
 * 支持的按键事件常量
 */
private val ArrowUp = KeyEvent("ArrowUp")
private val ArrowDown = KeyEvent("ArrowDown")
private val ArrowLeft = KeyEvent("ArrowLeft")
private val ArrowRight = KeyEvent("ArrowRight")
private val Enter = KeyEvent("Enter")
private val Escape = KeyEvent("Escape")
private val Backspace = KeyEvent("Backspace")
private val Delete = KeyEvent("Delete")
private val Home = KeyEvent("Home")
private val End = KeyEvent("End")
private val ShowHelp = KeyEvent("?")
private val ExitApp = KeyEvent("q", ctrl = true)
private val FunctionKeyByI = KeyEvent("i")
private val FunctionKeyByS = KeyEvent("s")
private val FunctionKeyByP = KeyEvent("p")

/**
 * 判断按键是否为可打印字符
 */
private fun KeyEvent.isPrintable(): Boolean {
    // 可打印字符通常是单个字符且不是控制键
    return key.length == 1 && key[0] in ' '..'~'
}

/**
 * 为组件添加键盘事件处理修饰符
 */
private fun Modifier.onDocliKeyEvent(
    uiStatus: DocliUiStatus,
    doAction: (DocliUiAction) -> Unit
): Modifier {
    return this.onKeyEvent {
        when (uiStatus) {
            DocliUiStatus.View -> {
                // 查看模式时
                when (it) {
                    ArrowUp -> doAction(DocliUiAction.NavigateUp)
                    ArrowDown -> doAction(DocliUiAction.NavigateDown)
                    ArrowLeft -> doAction(DocliUiAction.MoveCursor(Direction.LEFT))
                    ArrowRight -> doAction(DocliUiAction.MoveCursor(Direction.RIGHT))
                    Enter -> doAction(DocliUiAction.Confirm)
                    ExitApp -> doAction(DocliUiAction.Exit)
                    ShowHelp -> doAction(DocliUiAction.ShowHelp)
                    FunctionKeyByI -> doAction(DocliUiAction.FocusSearchBox)
                    FunctionKeyByS -> doAction(DocliUiAction.SwitchSearchList)
                    FunctionKeyByP -> doAction(DocliUiAction.SwitchDetailPage)
                    else -> return@onKeyEvent false
                }
            }

            DocliUiStatus.Input -> {
                // 输入模式时
                when {
                    it == Enter -> doAction(DocliUiAction.FinishInput)
                    it == Escape -> doAction(DocliUiAction.ExitInput)
                    it == Backspace -> doAction(DocliUiAction.Backspace)
                    it == Delete -> doAction(DocliUiAction.Delete)
                    it == Home -> doAction(DocliUiAction.MoveToLineStart)
                    it == End -> doAction(DocliUiAction.MoveToLineEnd)
                    it == ArrowLeft -> doAction(DocliUiAction.MoveCursorLeft)
                    it == ArrowRight -> doAction(DocliUiAction.MoveCursorRight)
                    it.isPrintable() -> doAction(DocliUiAction.UpdateInput(content = it.key))
                    else -> return@onKeyEvent false
                }
            }

            DocliUiStatus.Loading -> {
                // 加载中模式
                when (it) {
                    Escape -> doAction(DocliUiAction.FinishLoading)
                    else -> return@onKeyEvent false
                }
            }
        }
        true
    }
}

/**
 * 定义界面行为
 **/
@Stable
sealed interface DocliUiAction {
    /// 查看模式
    data object NavigateUp : DocliUiAction // 向上导航（列表移动或详情滚动）
    data object NavigateDown : DocliUiAction // 向下导航（列表移动或详情滚动）
    data class MoveCursor(val direction: Direction) : DocliUiAction //光标移动（保留用于左右方向）
    data object Confirm : DocliUiAction // 确认选中
    data object Exit : DocliUiAction // 退出
    data object ShowHelp : DocliUiAction // 显示帮助
    data object FocusSearchBox : DocliUiAction // 聚焦搜索框
    data object SwitchSearchList : DocliUiAction // 切换至文档搜索列表
    data object SwitchDetailPage : DocliUiAction // 切换至文档详情页面

    /// 输入模式
    data object FinishInput : DocliUiAction //  结束输入内容
    data object ExitInput : DocliUiAction //  退出输入模式
    data class UpdateInput(val content: String) : DocliUiAction // 更新输入内容
    data object Backspace : DocliUiAction // 退格删除
    data object Delete : DocliUiAction // 删除字符
    data object MoveToLineStart : DocliUiAction // 移动到行首
    data object MoveToLineEnd : DocliUiAction // 移动到行尾
    data object MoveCursorLeft : DocliUiAction // 光标左移
    data object MoveCursorRight : DocliUiAction // 光标右移

    /// 加载模式
    data object FinishLoading : DocliUiAction // 结束加载

    /// 通用
    data class Initialize(val searchResults: List<DocBrief>) : DocliUiAction
    data object ClearPendingActions : DocliUiAction //
}

/**
 * Docli操作框组件
 *
 * 该组件用于处理Docli界面的键盘事件，并将按键事件转换为相应的UI操作。
 * 它创建一个共享流来传递用户操作，并通过修饰符监听键盘事件。
 *
 * @param modifier 应用于Box组件的修饰符
 * @param uiStatus 当前页面的模式状态
 * @param content 可组合内容函数，接收一个Flow<DocliUiAction>作为参数，用于订阅用户操作
 */
@Composable
fun DocliActionBox(
    modifier: Modifier,
    uiStatus: DocliUiStatus,
    content: @Composable (Flow<DocliUiAction>) -> Unit
) {
    val eventFlow = MutableSharedFlow<DocliUiAction>(replay = 0, extraBufferCapacity = 1)

    Box(modifier.onDocliKeyEvent(uiStatus = uiStatus, doAction = { action ->
        // 将 KeyEvent 转换为 DocliUiAction 并发射到事件流中
        // 使用 tryEmit 避免在快速按键时创建过多协程
        eventFlow.tryEmit(action)
    })) {
        content(eventFlow)
    }
}
