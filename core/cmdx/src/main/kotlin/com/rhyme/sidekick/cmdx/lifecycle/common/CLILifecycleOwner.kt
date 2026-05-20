package com.rhyme.sidekick.cmdx.lifecycle.common

import com.rhyme.sidekick.cmdx.lifecycle.runtime.Lifecycle
import com.rhyme.sidekick.cmdx.lifecycle.runtime.LifecycleObserver
import com.rhyme.sidekick.cmdx.lifecycle.runtime.LifecycleOwner
import com.rhyme.sidekick.cmdx.lifecycle.runtime.LifecycleRegistry

/**
 * CLI 应用的生命周期所有者
 *
 * 实现 LifecycleOwner 接口，为命令行应用提供生命周期管理。
 * 提供便捷的状态转换方法，与 CLILifecycleState 配套使用。
 */
class CLILifecycleOwner : LifecycleOwner<CLILifecycleState> {

    private val registry = LifecycleRegistry<CLILifecycleState>(
        initialState = CLILifecycleState.Initialized(),
        initialLifecycleState = Lifecycle.State.CREATED
    )

    override val lifecycle: Lifecycle<CLILifecycleState> = registry

    // ==================== 初始化状态转换 ====================

    fun toInitialized() {
        registry.handleEventSync(
            Lifecycle.Event.ON_CREATE,
            CLILifecycleState.Initialized()
        )
    }

    fun toReady() {
        registry.handleEventSync(
            Lifecycle.Event.ON_READY,
            CLILifecycleState.Ready()
        )
    }

    // ==================== UI 交互状态转换 ====================

    fun toMenuSelection() {
        registry.handleEventSync(
            Lifecycle.Event.ON_MENU_SELECTION,
            CLILifecycleState.MenuSelection()
        )
    }

    fun toCommandList(moduleId: String, moduleName: String = "") {
        registry.handleEventSync(
            Lifecycle.Event.ON_COMMAND_LIST_SHOW,
            CLILifecycleState.CommandList(
                moduleId = moduleId,
                moduleName = moduleName
            )
        )
    }

    fun toParameterInput(commandId: String, commandName: String = "") {
        registry.handleEventSync(
            Lifecycle.Event.ON_PARAMETER_INPUT,
            CLILifecycleState.ParameterInput(
                commandId = commandId,
                commandName = commandName
            )
        )
    }

    fun toParameterInputFinish(commandId: String) {
        registry.handleEventSync(
            Lifecycle.Event.ON_PARAMETER_INPUT_FINISH,
            CLILifecycleState.CommandList(moduleId = commandId)
        )
    }

    // ==================== 命令执行状态转换 ====================

    fun toCommandExecutionStart(command: String) {
        registry.handleEventSync(
            Lifecycle.Event.ON_COMMAND_EXECUTION_START,
            CLILifecycleState.CommandExecution(
                command = command,
                progress = 0,
                isRunning = true
            )
        )
    }

    fun toCommandExecutionProgress(command: String, progress: Int) {
        registry.handleEventSync(
            Lifecycle.Event.ON_COMMAND_EXECUTION_START,
            CLILifecycleState.CommandExecution(
                command = command,
                progress = progress.coerceIn(0, 100),
                isRunning = true
            )
        )
    }

    fun toCommandExecutionFinish(command: String) {
        registry.handleEventSync(
            Lifecycle.Event.ON_COMMAND_EXECUTION_FINISH,
            CLILifecycleState.CommandExecution(
                command = command,
                progress = 100,
                isRunning = false
            )
        )
    }

    // ==================== 结果显示状态转换 ====================

    fun toResultDisplay(
        commandId: String,
        output: String,
        outputType: CLILifecycleState.ResultDisplay.OutputType = CLILifecycleState.ResultDisplay.OutputType.TEXT
    ) {
        registry.handleEventSync(
            Lifecycle.Event.ON_RESULT_DISPLAY,
            CLILifecycleState.ResultDisplay(
                commandId = commandId,
                output = output,
                outputType = outputType
            )
        )
    }

    fun toErrorDisplay(
        commandId: String,
        error: String,
        errorCode: String = "",
        suggestion: String = "",
        stackTrace: String = ""
    ) {
        registry.handleEventSync(
            Lifecycle.Event.ON_ERROR_DISPLAY,
            CLILifecycleState.ErrorDisplay(
                commandId = commandId,
                error = error,
                errorCode = errorCode,
                suggestion = suggestion,
                stackTrace = stackTrace
            )
        )
    }

    fun toTimeoutState(commandId: String, elapsedSeconds: Long = 0) {
        registry.handleEventSync(
            Lifecycle.Event.ON_TIMEOUT_DISPLAY,
            CLILifecycleState.TimeoutState(
                commandId = commandId,
                elapsedSeconds = elapsedSeconds
            )
        )
    }

    // ==================== 操作菜单状态转换 ====================

    fun toActionMenu(previousCommandId: String = "") {
        registry.handleEventSync(
            Lifecycle.Event.ON_ACTION_MENU,
            CLILifecycleState.ActionMenu(previousCommandId = previousCommandId)
        )
    }

    // ==================== 清理和退出状态转换 ====================

    fun toCleanup() {
        registry.handleEventSync(
            Lifecycle.Event.ON_STOP,
            CLILifecycleState.Cleanup()
        )
    }

    fun toTerminated() {
        registry.handleEventSync(
            Lifecycle.Event.ON_DESTROY,
            CLILifecycleState.Terminated()
        )
    }

    fun toFailed(error: Throwable? = null, errorPhase: String = "", message: String = "") {
        registry.handleEventSync(
            Lifecycle.Event.ON_DESTROY,
            CLILifecycleState.Failed(
                error = error,
                errorPhase = errorPhase,
                message = message.ifEmpty { "应用执行过程中发生错误" }
            )
        )
    }

    // ==================== 观察者管理 ====================

    fun addObserver(observer: LifecycleObserver) {
        registry.addObserver(observer)
    }

    fun removeObserver(observer: LifecycleObserver) {
        registry.removeObserver(observer)
    }

    fun getObservers(): List<LifecycleObserver> = registry.getObservers()

    // ==================== 状态查询 ====================

    fun getCurrentState(): CLILifecycleState = registry.getCurrentState()

    fun getCurrentLifecycleState(): Lifecycle.State = registry.currentState

    fun isAtLeast(state: Lifecycle.State): Boolean = registry.isAtLeast(state)

    fun isResumed(): Boolean = isAtLeast(Lifecycle.State.RESUMED)

    fun isStarted(): Boolean = isAtLeast(Lifecycle.State.STARTED)
}
