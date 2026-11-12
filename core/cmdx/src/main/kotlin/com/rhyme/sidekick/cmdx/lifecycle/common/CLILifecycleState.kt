package com.rhyme.sidekick.cmdx.lifecycle.common

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * CLI 应用的业务状态
 *
 * 密封类层次结构，代表 CLI 应用的各个界面 / 业务状态。
 *
 * **注意**：这是**业务状态快照**，通过 StateFlow 暴露；
 * 它与宿主生命周期（[com.rhyme.sidekick.cmdx.lifecycle.runtime.Lifecycle.State]）
 * 是两件正交的事，不要用业务状态去推导宿主生命周期状态。
 *
 * 状态转移流：
 * MenuSelection → CommandList → ParameterInput → CommandExecution
 * → (ResultDisplay | ErrorDisplay | TimeoutState) → ActionMenu → (循环或退出)
 */
sealed class CLILifecycleState {
    abstract val timestamp: LocalDateTime
    abstract val message: String

    // ==================== 初始化状态 ====================

    /**
     * 应用初始化状态
     */
    data class Initialized(
        override val timestamp: LocalDateTime = LocalDateTime.now(),
        override val message: String = "应用已初始化"
    ) : CLILifecycleState()

    /**
     * 应用已就绪，显示主菜单
     */
    data class Ready(
        override val timestamp: LocalDateTime = LocalDateTime.now(),
        override val message: String = "应用已就绪"
    ) : CLILifecycleState()

    // ==================== UI 交互状态 ====================

    /**
     * 主菜单选择状态 - 用户选择要执行的命令模块
     */
    data class MenuSelection(
        override val timestamp: LocalDateTime = LocalDateTime.now(),
        override val message: String = "等待用户选择命令模块"
    ) : CLILifecycleState()

    /**
     * 命令列表显示状态 - 显示选定模块的所有命令
     */
    data class CommandList(
        val moduleId: String = "",
        val moduleName: String = "",
        override val timestamp: LocalDateTime = LocalDateTime.now(),
        override val message: String = "显示命令列表"
    ) : CLILifecycleState()

    /**
     * 参数输入状态 - 收集命令所需的参数
     */
    data class ParameterInput(
        val commandId: String = "",
        val commandName: String = "",
        override val timestamp: LocalDateTime = LocalDateTime.now(),
        override val message: String = "等待用户输入参数"
    ) : CLILifecycleState()

    // ==================== 命令执行状态 ====================

    /**
     * 命令执行状态 - 正在通过 ProcessBuilder 执行命令
     */
    data class CommandExecution(
        val command: String = "",
        val progress: Int = 0,
        val isRunning: Boolean = true,
        override val timestamp: LocalDateTime = LocalDateTime.now(),
        override val message: String = "正在执行命令..."
    ) : CLILifecycleState()

    // ==================== 结果显示状态 ====================

    /**
     * 成功执行结果显示状态
     */
    data class ResultDisplay(
        val commandId: String = "",
        val output: String = "",
        val outputType: OutputType = OutputType.TEXT,
        override val timestamp: LocalDateTime = LocalDateTime.now(),
        override val message: String = "显示执行结果"
    ) : CLILifecycleState() {
        enum class OutputType {
            TEXT,      // 纯文本输出
            MARKDOWN,  // Markdown 格式
            JSON,      // JSON 格式
            HTML       // HTML 格式
        }
    }

    /**
     * 错误显示状态
     */
    data class ErrorDisplay(
        val commandId: String = "",
        val error: String = "",
        val errorCode: String = "",
        val suggestion: String = "",
        val stackTrace: String = "",
        override val timestamp: LocalDateTime = LocalDateTime.now(),
        override val message: String = "显示错误信息"
    ) : CLILifecycleState()

    /**
     * 超时状态 - 命令执行超时或被中断
     */
    data class TimeoutState(
        val commandId: String = "",
        val elapsedSeconds: Long = 0,
        override val timestamp: LocalDateTime = LocalDateTime.now(),
        override val message: String = "命令执行超时"
    ) : CLILifecycleState()

    // ==================== 后续操作状态 ====================

    /**
     * 操作菜单状态 - 提供后续操作选项
     */
    data class ActionMenu(
        val previousCommandId: String = "",
        override val timestamp: LocalDateTime = LocalDateTime.now(),
        override val message: String = "显示操作菜单"
    ) : CLILifecycleState()

    // ==================== 清理和退出状态 ====================

    /**
     * 清理资源状态
     */
    data class Cleanup(
        override val timestamp: LocalDateTime = LocalDateTime.now(),
        override val message: String = "正在清理资源..."
    ) : CLILifecycleState()

    /**
     * 应用终止状态
     */
    data class Terminated(
        override val timestamp: LocalDateTime = LocalDateTime.now(),
        override val message: String = "应用已正常退出"
    ) : CLILifecycleState()

    /**
     * 应用失败状态
     */
    data class Failed(
        val error: Throwable? = null,
        val errorPhase: String = "",
        override val timestamp: LocalDateTime = LocalDateTime.now(),
        override val message: String = "应用执行过程中发生错误"
    ) : CLILifecycleState()

    // ==================== 辅助方法 ====================

    /**
     * 是否为终态（终止 / 失败）
     */
    val isTerminal: Boolean
        get() = this is Terminated || this is Failed

    /**
     * 获取格式化的时间戳
     */
    fun getFormattedTimestamp(): String {
        val formatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS")
        return timestamp.format(formatter)
    }

    /**
     * 获取状态名称
     */
    fun getStateName(): String = this::class.simpleName ?: "Unknown"

    /**
     * 获取完整的状态描述
     */
    fun getFullDescription(): String = "[${getFormattedTimestamp()}] ${getStateName()}: $message"
}
