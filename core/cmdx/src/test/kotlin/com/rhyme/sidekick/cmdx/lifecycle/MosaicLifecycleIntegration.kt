package com.rhyme.sidekick.cmdx.lifecycle

import com.rhyme.sidekick.cmdx.lifecycle.common.CLILifecycleOwner
import com.rhyme.sidekick.cmdx.lifecycle.common.CLILifecycleState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.runBlocking

/**
 * Mosaic UI 中的生命周期集成示例
 *
 * 展示如何在实际的 Mosaic UI 应用中使用新的生命周期系统
 */
object MosaicLifecycleIntegration {

    /**
     * 创建支持生命周期的应用上下文
     */
    class LifecycleEnabledAppContext(
        val lifecycleOwner: CLILifecycleOwner = CLILifecycleOwner(),
        val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Default)
    ) {
        val stateFlow: StateFlow<CLILifecycleState> = lifecycleOwner.stateFlow

        fun getCurrentState(): CLILifecycleState = lifecycleOwner.getCurrentState()

        fun isMenuSelection(): Boolean = getCurrentState() is CLILifecycleState.MenuSelection
        fun isCommandList(): Boolean = getCurrentState() is CLILifecycleState.CommandList
        fun isParameterInput(): Boolean = getCurrentState() is CLILifecycleState.ParameterInput
        fun isCommandExecution(): Boolean = getCurrentState() is CLILifecycleState.CommandExecution
        fun isResultDisplay(): Boolean = getCurrentState() is CLILifecycleState.ResultDisplay
        fun isErrorDisplay(): Boolean = getCurrentState() is CLILifecycleState.ErrorDisplay
        fun isTimeoutState(): Boolean = getCurrentState() is CLILifecycleState.TimeoutState
        fun isActionMenu(): Boolean = getCurrentState() is CLILifecycleState.ActionMenu
    }

    /**
     * UI 状态管理器，根据生命周期状态驱动 UI 更新
     */
    class UIStateManager(private val appContext: LifecycleEnabledAppContext) {

        /**
         * 根据当前状态渲染 UI
         */
        fun renderUI(): String = buildString {
            val state = appContext.getCurrentState()

            append("╔════════════════════════════════════╗\n")
            append("║  AndroMosaic CLI - ${state::class.simpleName}  ║\n")
            append("╚════════════════════════════════════╝\n\n")

            when (state) {
                is CLILifecycleState.Initialized -> {
                    append("应用正在初始化...\n")
                }
                is CLILifecycleState.Ready -> {
                    append("应用已准备就绪，按任意键继续\n")
                }
                is CLILifecycleState.MenuSelection -> {
                    append("请选择命令模块:\n")
                    append("1. create   - 项目创建\n")
                    append("2. describe - 项目描述\n")
                    append("3. emulator - 虚拟设备\n")
                    append("4. sdk      - SDK 包管理\n")
                    append("5. exit     - 退出\n")
                }
                is CLILifecycleState.CommandList -> {
                    append("模块: ${state.moduleName}\n")
                    append("请选择命令...\n")
                }
                is CLILifecycleState.ParameterInput -> {
                    append("命令: ${state.commandName}\n")
                    append("请输入参数...\n")
                }
                is CLILifecycleState.CommandExecution -> {
                    append("执行命令: ${state.command}\n")
                    append("进度: ${state.progress}%\n")
                    append("[" + "█".repeat(state.progress / 10) + "░".repeat(10 - state.progress / 10) + "]\n")
                }
                is CLILifecycleState.ResultDisplay -> {
                    append("执行成功！\n")
                    append("输出内容:\n")
                    append(state.output.take(200) + "\n")
                }
                is CLILifecycleState.ErrorDisplay -> {
                    append("❌ 执行失败\n")
                    append("错误: ${state.error}\n")
                    append("建议: ${state.suggestion}\n")
                }
                is CLILifecycleState.TimeoutState -> {
                    append("⏱ 执行超时\n")
                    append("已执行: ${state.elapsedSeconds} 秒\n")
                }
                is CLILifecycleState.ActionMenu -> {
                    append("后续操作:\n")
                    append("1. 执行另一个命令\n")
                    append("2. 返回主菜单\n")
                    append("3. 查看完整日志\n")
                    append("4. 退出\n")
                }
                is CLILifecycleState.Terminated -> {
                    append("应用已退出\n")
                }
                is CLILifecycleState.Failed -> {
                    append("❌ 应用错误\n")
                    append("错误信息: ${state.message}\n")
                }
                else -> {
                    append("当前状态: ${state::class.simpleName}\n")
                }
            }

            append("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
            append("时间: ${state.getFormattedTimestamp()}\n")
        }
    }

    /**
     * 模拟用户交互和命令执行流程
     */
    suspend fun simulateUserInteraction(appContext: LifecycleEnabledAppContext) {
        val uiManager = UIStateManager(appContext)

        // 1. 初始化
        appContext.lifecycleOwner.toInitialized()
        println(uiManager.renderUI())
        delay(1000)

        // 2. 准备就绪
        appContext.lifecycleOwner.toReady()
        println(uiManager.renderUI())
        delay(1000)

        // 3. 显示菜单
        appContext.lifecycleOwner.toMenuSelection()
        println(uiManager.renderUI())
        delay(1500)

        // 4. 选择命令模块（模拟用户选择 "create"）
        appContext.lifecycleOwner.toCommandList("create", "项目创建")
        println(uiManager.renderUI())
        delay(1000)

        // 5. 参数输入
        appContext.lifecycleOwner.toParameterInput("create", "创建新项目")
        println(uiManager.renderUI())
        delay(1500)

        // 6. 命令执行
        appContext.lifecycleOwner.toCommandExecutionStart("android create --output=/path/to/project")
        println(uiManager.renderUI())

        // 模拟执行进度
        for (progress in listOf(25, 50, 75, 100)) {
            delay(500)
            appContext.lifecycleOwner.toCommandExecutionProgress(
                "android create --output=/path/to/project",
                progress
            )
            println(uiManager.renderUI())
        }

        // 7. 执行完成
        appContext.lifecycleOwner.toCommandExecutionFinish("android create --output=/path/to/project")

        // 8. 显示结果
        appContext.lifecycleOwner.toResultDisplay(
            "create",
            "Project successfully created!\nProject location: /path/to/project\nNext steps:\n1. cd /path/to/project\n2. ./gradlew build",
            CLILifecycleState.ResultDisplay.OutputType.TEXT
        )
        println(uiManager.renderUI())
        delay(1500)

        // 9. 操作菜单
        appContext.lifecycleOwner.toActionMenu("create")
        println(uiManager.renderUI())
        delay(1500)

        // 10. 退出
        appContext.lifecycleOwner.toTerminated()
        println(uiManager.renderUI())
    }

    /**
     * 辅助函数：延迟
     */
    private suspend fun delay(ms: Long) {
        kotlinx.coroutines.delay(ms)
    }

    @JvmStatic
    fun main(args: Array<String>) {
        runBlocking {
            simulateUserInteraction(LifecycleEnabledAppContext())
        }
    }
}