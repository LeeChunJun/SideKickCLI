package com.rhyme.sidekick.cmdx.navigation.lifecycle

/**
 * 定义观察者接口
 * 模仿 Android 的 DefaultLifecycleObserver，提供一个包含默认空实现的接口，方便用户只关注自己需要的生命周期。
 */
interface NavLifecycleObserver {
    fun onStateChanged(source: NavLifecycleOwner, state: NavLifecycleState) {}

    // 方便使用的扩展回调
    fun onCreate(owner: NavLifecycleOwner) {}
    fun onStart(owner: NavLifecycleOwner) {}
    fun onResume(owner: NavLifecycleOwner) {}
    fun onPause(owner: NavLifecycleOwner) {}
    fun onStop(owner: NavLifecycleOwner) {}
    fun onDestroy(owner: NavLifecycleOwner) {}
}