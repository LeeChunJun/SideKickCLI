package com.rhyme.sidekick.cmdx.lifecycle.common

import com.rhyme.sidekick.cmdx.lifecycle.runtime.Lifecycle
import com.rhyme.sidekick.cmdx.lifecycle.runtime.LifecycleObserver
import com.rhyme.sidekick.cmdx.lifecycle.runtime.LifecycleOwner
import com.rhyme.sidekick.cmdx.lifecycle.runtime.LifecycleRegistry
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

/**
 * CLI 应用的生命周期所有者
 *
 * 组合了**两层正交的状态**：
 *
 * 1. **宿主生命周期**（[lifecycle]）：应用是否创建 / 是否可交互 / 是否结束。
 *    只有 5 个状态、4 个事件，状态机严格单调，是唯一能反映"应用还活着吗"的依据。
 * 2. **业务状态**（[stateFlow]）：当前处在哪个界面（菜单 / 命令列表 / 执行中等），
 *    以及相应的瞬时业务事件（[eventFlow]）。
 *
 * 宿主生命周期与业务状态**不再互相推导**：
 * 命令执行开始不会让宿主状态掉回 PAUSED，失败与正常退出也有各自独立的事件。
 *
 * 典型用法：
 * ```kotlin
 * val owner = CLILifecycleOwner()
 * owner.toInitialized()          // 宿主: CREATED，业务: Initialized
 * owner.toReady()                // 宿主: RESUMED，业务: Ready
 *
 * // 观察宿主生命周期（资源初始化 / 释放）
 * owner.addObserver(object : LifecycleObserver {
 *     @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
 *     fun onDestroy() { /* 释放资源 */ }
 * })
 *
 * // 观察业务状态（界面渲染）
 * owner.observeStates(scope) { state -> render(state) }
 * ```
 *
 * @param observerDispatcher 观察者回调线程，null 表示在调用线程同步回调
 */
class CLILifecycleOwner(
    observerDispatcher: CoroutineDispatcher? = null
) : LifecycleOwner {

    private val registry = LifecycleRegistry(observerDispatcher = observerDispatcher)

    override val lifecycle: Lifecycle
        get() = registry

    // ==================== 业务状态 ====================

    private val _stateFlow = MutableStateFlow<CLILifecycleState>(CLILifecycleState.Initialized())

    /**
     * 当前业务状态（界面状态）
     */
    val stateFlow: StateFlow<CLILifecycleState> = _stateFlow.asStateFlow()

    private val _eventFlow = MutableSharedFlow<CLILifecycleEvent>(
        replay = 0,
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    /**
     * 业务事件流（瞬时事件，不回放）
     */
    val eventFlow: Flow<CLILifecycleEvent> = _eventFlow.asSharedFlow()

    /**
     * 最近一次进入的命令模块，用于参数输入结束后回到正确的命令列表
     */
    private var lastModuleId: String = ""
    private var lastModuleName: String = ""

    // ==================== 初始化状态转换 ====================

    fun toInitialized() {
        dispatchHost(Lifecycle.Event.ON_CREATE)
        transition(CLILifecycleState.Initialized(), CLILifecycleEvent.ON_INITIALIZED)
    }

    fun toReady() {
        dispatchHost(Lifecycle.Event.ON_RESUME)
        transition(CLILifecycleState.Ready(), CLILifecycleEvent.ON_READY)
    }

    // ==================== UI 交互状态转换 ====================

    fun toMenuSelection() {
        transition(CLILifecycleState.MenuSelection(), CLILifecycleEvent.ON_MENU_SELECTION)
    }

    fun toCommandList(moduleId: String, moduleName: String = "") {
        lastModuleId = moduleId
        lastModuleName = moduleName
        transition(
            CLILifecycleState.CommandList(moduleId = moduleId, moduleName = moduleName),
            CLILifecycleEvent.ON_COMMAND_LIST_SHOW
        )
    }

    fun toParameterInput(commandId: String, commandName: String = "") {
        transition(
            CLILifecycleState.ParameterInput(commandId = commandId, commandName = commandName),
            CLILifecycleEvent.ON_PARAMETER_INPUT
        )
    }

    /**
     * 参数输入结束，回到进入参数输入之前所在的命令列表
     */
    fun toParameterInputFinish() {
        transition(
            CLILifecycleState.CommandList(moduleId = lastModuleId, moduleName = lastModuleName),
            CLILifecycleEvent.ON_PARAMETER_INPUT_FINISH
        )
    }

    // ==================== 命令执行状态转换 ====================

    fun toCommandExecutionStart(command: String) {
        transition(
            CLILifecycleState.CommandExecution(command = command, progress = 0, isRunning = true),
            CLILifecycleEvent.ON_COMMAND_EXECUTION_START
        )
    }

    /**
     * 更新命令执行进度
     *
     * 使用独立的事件 [CLILifecycleEvent.ON_COMMAND_EXECUTION_PROGRESS]，
     * 观察者可以区分"开始执行"与"进度更新"。
     */
    fun toCommandExecutionProgress(command: String, progress: Int) {
        transition(
            CLILifecycleState.CommandExecution(
                command = command,
                progress = progress.coerceIn(0, 100),
                isRunning = true
            ),
            CLILifecycleEvent.ON_COMMAND_EXECUTION_PROGRESS
        )
    }

    fun toCommandExecutionFinish(command: String) {
        transition(
            CLILifecycleState.CommandExecution(command = command, progress = 100, isRunning = false),
            CLILifecycleEvent.ON_COMMAND_EXECUTION_FINISH
        )
    }

    // ==================== 结果显示状态转换 ====================

    fun toResultDisplay(
        commandId: String,
        output: String,
        outputType: CLILifecycleState.ResultDisplay.OutputType = CLILifecycleState.ResultDisplay.OutputType.TEXT
    ) {
        transition(
            CLILifecycleState.ResultDisplay(
                commandId = commandId,
                output = output,
                outputType = outputType
            ),
            CLILifecycleEvent.ON_RESULT_DISPLAY
        )
    }

    fun toErrorDisplay(
        commandId: String,
        error: String,
        errorCode: String = "",
        suggestion: String = "",
        stackTrace: String = ""
    ) {
        transition(
            CLILifecycleState.ErrorDisplay(
                commandId = commandId,
                error = error,
                errorCode = errorCode,
                suggestion = suggestion,
                stackTrace = stackTrace
            ),
            CLILifecycleEvent.ON_ERROR_DISPLAY
        )
    }

    fun toTimeoutState(commandId: String, elapsedSeconds: Long = 0) {
        transition(
            CLILifecycleState.TimeoutState(
                commandId = commandId,
                elapsedSeconds = elapsedSeconds
            ),
            CLILifecycleEvent.ON_TIMEOUT_DISPLAY
        )
    }

    // ==================== 操作菜单状态转换 ====================

    fun toActionMenu(previousCommandId: String = "") {
        transition(
            CLILifecycleState.ActionMenu(previousCommandId = previousCommandId),
            CLILifecycleEvent.ON_ACTION_MENU
        )
    }

    // ==================== 清理和退出状态转换 ====================

    fun toCleanup() {
        dispatchHost(Lifecycle.Event.ON_PAUSE)
        transition(CLILifecycleState.Cleanup(), CLILifecycleEvent.ON_CLEANUP)
    }

    fun toTerminated() {
        dispatchHost(Lifecycle.Event.ON_DESTROY)
        transition(CLILifecycleState.Terminated(), CLILifecycleEvent.ON_TERMINATED)
    }

    fun toFailed(error: Throwable? = null, errorPhase: String = "", message: String = "") {
        dispatchHost(Lifecycle.Event.ON_DESTROY)
        transition(
            CLILifecycleState.Failed(
                error = error,
                errorPhase = errorPhase,
                message = message.ifEmpty { "应用执行过程中发生错误" }
            ),
            CLILifecycleEvent.ON_FAILED
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

    // ==================== 业务状态订阅 ====================

    /**
     * 监听业务状态变化
     *
     * 命名约定：`owner.xxx` 订阅的是**业务**状态，
     * `owner.lifecycle.xxx` 订阅的是**宿主**生命周期状态。
     */
    fun observeStates(
        coroutineScope: CoroutineScope,
        block: suspend (CLILifecycleState) -> Unit
    ): Job = coroutineScope.launch {
        stateFlow.collect { block(it) }
    }

    /**
     * 监听业务事件
     */
    fun observeEvents(
        coroutineScope: CoroutineScope,
        block: suspend (CLILifecycleEvent) -> Unit
    ): Job = coroutineScope.launch {
        eventFlow.collect { block(it) }
    }

    /**
     * 监听特定的业务事件
     */
    fun observeEvent(
        event: CLILifecycleEvent,
        coroutineScope: CoroutineScope,
        block: suspend () -> Unit
    ): Job = coroutineScope.launch {
        eventFlow.filter { it == event }.collect { block() }
    }

    // ==================== 状态查询 ====================

    /**
     * 当前业务状态
     */
    fun getCurrentState(): CLILifecycleState = _stateFlow.value

    /**
     * 当前宿主生命周期状态
     */
    fun getCurrentLifecycleState(): Lifecycle.State = registry.currentState

    fun isAtLeast(state: Lifecycle.State): Boolean = registry.currentState.isAtLeast(state)

    fun isResumed(): Boolean = registry.currentState == Lifecycle.State.RESUMED

    fun isCreated(): Boolean = registry.currentState.isAtLeast(Lifecycle.State.CREATED)

    fun isTerminated(): Boolean = registry.currentState.isTerminal

    // ==================== 内部实现 ====================

    /**
     * 派发宿主生命周期事件
     *
     * 业务层对非法迁移是容忍的（例如应用还没进入 RESUMED 就清理资源），
     * 此时直接跳过；框架层 [LifecycleRegistry.dispatch] 仍然会抛异常。
     */
    private fun dispatchHost(event: Lifecycle.Event) {
        val current = registry.currentState
        if (current == event.targetState) return
        if (!registry.canDispatch(event)) return
        registry.dispatch(event)
    }

    /**
     * 更新业务状态并派发业务事件
     */
    private fun transition(state: CLILifecycleState, event: CLILifecycleEvent) {
        _stateFlow.value = state
        _eventFlow.tryEmit(event)
    }
}
