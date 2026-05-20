package com.rhyme.sidekick.cmdx.lifecycle.runtime

/**
 * 生命周期事件观察者 - 函数式接口
 *
 * 提供了一种函数式的方式来观察生命周期事件，与 @OnLifecycleEvent 注解提供相同的功能。
 *
 * 两种使用方式：
 *
 * **方式 1: 注解驱动（传统方式）**
 * ```kotlin
 * class MyObserver : LifecycleObserver {
 *     @OnLifecycleEvent(value = "ON_CREATE")
 *     fun onCreate() { ... }
 * }
 * lifecycle.addObserver(MyObserver())
 * ```
 *
 * **方式 2: 函数式（推荐方式）**
 * ```kotlin
 * val observer = LifecycleEventObserver { source, event ->
 *     when (event) {
 *         Lifecycle.Event.ON_CREATE -> { ... }
 *         Lifecycle.Event.ON_READY -> { ... }
 *         else -> { }
 *     }
 * }
 * lifecycle.addObserver(observer)
 * ```
 *
 * 项目名：SideKickCLI
 * 包名：com.rhyme.sidekick.cmdx.lifecycle.runtime
 *
 * @author 李春俊-Rhyme
 * @since 18:06 2026/4/24 星期五
 * @version 1.0
 */
fun interface LifecycleEventObserver : LifecycleObserver {
    /**
     * 当生命周期状态转换时调用
     *
     * @param source 事件的源（生命周期所有者）
     * @param event 发生的生命周期事件
     */
    fun onStateChanged(source: LifecycleOwner<*>, event: Lifecycle.Event)
}
