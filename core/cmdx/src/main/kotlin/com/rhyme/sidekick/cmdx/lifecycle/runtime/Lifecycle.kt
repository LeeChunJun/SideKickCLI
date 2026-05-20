package com.rhyme.sidekick.cmdx.lifecycle.runtime

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * 生命周期接口，定义了一个可观察的状态机
 *
 * 仿照 androidx.lifecycle.Lifecycle 的设计，提供事件和状态的管理。
 * 可以观察生命周期状态的变化，支持观察者模式。
 *
 * @param S 生命周期状态的类型参数
 */
interface Lifecycle<S : Any> {
    /**
     * 生命周期事件枚举
     *
     * 事件代表生命周期的转换点，从一个状态到另一个状态
     */
    enum class Event {
        // 初始化相关事件
        ON_CREATE,

        // 配置加载事件
        ON_CONFIG_LOAD_START,
        ON_CONFIG_LOAD_FINISH,

        // 资源初始化事件
        ON_RESOURCE_INIT_START,
        ON_RESOURCE_INIT_FINISH,

        // 准备就绪事件
        ON_READY,

        // 用户交互事件
        ON_MENU_SELECTION,
        ON_COMMAND_LIST_SHOW,
        ON_PARAMETER_INPUT,
        ON_PARAMETER_INPUT_FINISH,

        // 命令执行事件
        ON_COMMAND_EXECUTION_START,
        ON_COMMAND_EXECUTION_FINISH,

        // 结果显示事件
        ON_RESULT_DISPLAY,
        ON_ERROR_DISPLAY,
        ON_TIMEOUT_DISPLAY,

        // 操作菜单事件
        ON_ACTION_MENU,

        // 清理和退出事件
        ON_PAUSE,
        ON_STOP,
        ON_DESTROY;

        /**
         * 获取对应的目标状态
         */
        val targetState: State
            get() = when (this) {
                ON_CREATE -> State.CREATED
                ON_CONFIG_LOAD_START -> State.STARTED
                ON_CONFIG_LOAD_FINISH -> State.STARTED
                ON_RESOURCE_INIT_START -> State.STARTED
                ON_RESOURCE_INIT_FINISH -> State.STARTED
                ON_READY -> State.RESUMED
                ON_MENU_SELECTION -> State.RESUMED
                ON_COMMAND_LIST_SHOW -> State.RESUMED
                ON_PARAMETER_INPUT -> State.RESUMED
                ON_PARAMETER_INPUT_FINISH -> State.RESUMED
                ON_COMMAND_EXECUTION_START -> State.STARTED
                ON_COMMAND_EXECUTION_FINISH -> State.RESUMED
                ON_RESULT_DISPLAY -> State.RESUMED
                ON_ERROR_DISPLAY -> State.RESUMED
                ON_TIMEOUT_DISPLAY -> State.RESUMED
                ON_ACTION_MENU -> State.RESUMED
                ON_PAUSE -> State.STARTED
                ON_STOP -> State.CREATED
                ON_DESTROY -> State.DESTROYED
            }
    }

    /**
     * 生命周期状态枚举
     *
     * 代表生命周期所处的状态，是多个事件的聚合结果
     */
    enum class State {
        // 还未创建
        DESTROYED,

        // 已创建但未启动
        CREATED,

        // 已启动但未激活
        STARTED,

        // 已激活（活跃状态）
        RESUMED;

        /**
         * 检查是否至少处于某个状态
         */
        fun isAtLeast(state: State): Boolean = this.ordinal >= state.ordinal
    }

    /**
     * 当前生命周期状态
     */
    val currentState: State

    /**
     * 状态流，用于观察状态变化
     */
    val stateFlow: StateFlow<S>

    /**
     * 事件流，用于观察事件触发
     */
    val eventFlow: Flow<Event>

    /**
     * 添加生命周期观察者
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
