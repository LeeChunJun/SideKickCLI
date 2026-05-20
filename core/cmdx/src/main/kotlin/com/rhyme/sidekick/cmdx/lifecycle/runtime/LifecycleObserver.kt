package com.rhyme.sidekick.cmdx.lifecycle.runtime

/**
 * 生命周期观察者标记接口
 * 
 * 仿照 androidx.lifecycle.LifecycleObserver，用于标记实现该接口的类为生命周期观察者。
 * 观察者可以通过 @OnLifecycleEvent 注解在特定的生命周期事件发生时执行相应的方法。
 */
interface LifecycleObserver
