package com.rhyme.sidekick.cmdx.lifecycle.runtime

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * 生命周期接口
 *
 * 仿照 androidx.lifecycle.Lifecycle 的设计，但针对 CLI / TUI 场景做了收敛与修正：
 *
 * 1. 只保留**宿主语义**的状态机（应用是否创建 / 是否可交互 / 是否结束），
 *    不再把业务事件（菜单选择、命令执行……）混入事件枚举，避免状态来回抖动。
 * 2. 事件与目标状态一一对应，且迁移非法时**抛异常**，而不是静默写坏状态。
 * 3. 观察者注册后自动回放**真实发生过的**历史事件（catch-up），与 Android 行为一致。
 *
 * 宿主状态语义：
 * - [State.INITIALIZED] 刚创建，尚未初始化
 * - [State.CREATED] 资源就绪，尚未进入交互
 * - [State.RESUMED] 正在与用户交互
 * - [State.PAUSED] 暂时不可交互（例如等待子进程结束）
 * - [State.TERMINATED] 已销毁，终态，不可再迁移
 *
 * 合法迁移：
 * ```
 * INITIALIZED --ON_CREATE--> CREATED --ON_RESUME--> RESUMED
 *                                        ^            |
 *                                        |            ON_PAUSE
 *                                        +--- PAUSED <-+
 * *            --ON_DESTROY--> TERMINATED
 * ```
 */
interface Lifecycle {

    /**
     * 生命周期状态
     */
    enum class State {
        INITIALIZED,
        CREATED,
        RESUMED,
        PAUSED,
        TERMINATED;

        /**
         * 状态排序，用于 [isAtLeast] 比较。
         *
         * 注意 [PAUSED] 排在 [RESUMED] 之前：暂停态并不算"至少已激活"。
         */
        val order: Int
            get() = when (this) {
                INITIALIZED -> 0
                CREATED -> 1
                PAUSED -> 2
                RESUMED -> 3
                TERMINATED -> 4
            }

        /**
         * 是否为终态（销毁后不可再迁移）
         */
        val isTerminal: Boolean
            get() = this == TERMINATED

        /**
         * 检查是否至少处于某个状态
         */
        fun isAtLeast(state: State): Boolean = order >= state.order
    }

    /**
     * 生命周期事件
     *
     * 事件与 [State] 一一对应，仅描述宿主生命周期。
     * 业务事件请使用 `CLILifecycleEvent`。
     */
    enum class Event {
        ON_CREATE,
        ON_RESUME,
        ON_PAUSE,
        ON_DESTROY;

        /**
         * 获取该事件对应的目标状态
         */
        val targetState: State
            get() = when (this) {
                ON_CREATE -> State.CREATED
                ON_RESUME -> State.RESUMED
                ON_PAUSE -> State.PAUSED
                ON_DESTROY -> State.TERMINATED
            }
    }

    /**
     * 当前生命周期状态
     */
    val currentState: State

    /**
     * 状态流，用于观察状态变化
     */
    val stateFlow: StateFlow<State>

    /**
     * 事件流，用于观察事件触发
     *
     * 事件是**瞬时**的：不回放，慢消费者可能丢事件。
     * 需要可靠消费请使用 [addObserver]。
     */
    val eventFlow: Flow<Event>

    /**
     * 该事件此刻是否能够引起状态变化
     *
     * 已经是目标状态时返回 false（幂等，不视为可迁移）。
     */
    fun canDispatch(event: Event): Boolean

    /**
     * 派发事件
     *
     * @return 状态是否真的发生了变化（幂等派发返回 false）
     * @throws IllegalLifecycleTransitionException 非法迁移时抛出
     */
    fun dispatch(event: Event): Boolean

    /**
     * 添加生命周期观察者
     *
     * 新观察者会先收到已经真实发生过的历史事件，再收到后续事件。
     */
    fun addObserver(observer: LifecycleObserver)

    /**
     * 移除生命周期观察者
     */
    fun removeObserver(observer: LifecycleObserver)

    /**
     * 获取当前所有观察者
     */
    fun getObservers(): List<LifecycleObserver>

    /**
     * 检查是否有指定的观察者
     */
    fun hasObserver(observer: LifecycleObserver): Boolean
}
