package com.rhyme.sidekick.cmdx.navigation.lifecycle

/**
 * 导航生命周期状态持有者
 */
interface NavLifecycleOwner {
    val lifecycleState: NavLifecycleState
}