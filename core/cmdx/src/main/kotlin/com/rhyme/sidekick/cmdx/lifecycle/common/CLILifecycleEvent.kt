package com.rhyme.sidekick.cmdx.lifecycle.common

/**
 * CLI 业务生命周期事件
 *
 * 与宿主生命周期事件（[com.rhyme.sidekick.cmdx.lifecycle.runtime.Lifecycle.Event]）区分开：
 * 这里描述的是**业务/界面**发生了什么，与应用是否可交互无关。
 *
 * 业务状态本身用 [CLILifecycleState] 通过 StateFlow 暴露；
 * 业务事件则是一次性的瞬时通知，用 SharedFlow 暴露。
 */
enum class CLILifecycleEvent {

    /**
     * 应用初始化完成
     */
    ON_INITIALIZED,

    /**
     * 应用已就绪，可以开始交互
     */
    ON_READY,

    /**
     * 显示主菜单
     */
    ON_MENU_SELECTION,

    /**
     * 显示命令列表
     */
    ON_COMMAND_LIST_SHOW,

    /**
     * 进入参数输入
     */
    ON_PARAMETER_INPUT,

    /**
     * 参数输入结束
     */
    ON_PARAMETER_INPUT_FINISH,

    /**
     * 命令开始执行
     */
    ON_COMMAND_EXECUTION_START,

    /**
     * 命令执行进度更新
     */
    ON_COMMAND_EXECUTION_PROGRESS,

    /**
     * 命令执行结束
     */
    ON_COMMAND_EXECUTION_FINISH,

    /**
     * 显示执行结果
     */
    ON_RESULT_DISPLAY,

    /**
     * 显示错误信息
     */
    ON_ERROR_DISPLAY,

    /**
     * 显示超时信息
     */
    ON_TIMEOUT_DISPLAY,

    /**
     * 显示后续操作菜单
     */
    ON_ACTION_MENU,

    /**
     * 开始清理资源
     */
    ON_CLEANUP,

    /**
     * 应用正常退出
     */
    ON_TERMINATED,

    /**
     * 应用异常失败
     */
    ON_FAILED
}
