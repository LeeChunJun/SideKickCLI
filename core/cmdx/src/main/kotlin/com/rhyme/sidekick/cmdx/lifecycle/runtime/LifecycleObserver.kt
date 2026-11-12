package com.rhyme.sidekick.cmdx.lifecycle.runtime

/**
 * 生命周期观察者标记接口
 *
 * 仿照 androidx.lifecycle.LifecycleObserver，用于标记实现此接口的类为生命周期观察者。
 *
 * 两种回调方式：
 * 1. 注解方式：`@OnLifecycleEvent(Lifecycle.Event.ON_CREATE)`
 * 2. 函数式：实现 [LifecycleEventObserver]
 */
interface LifecycleObserver
