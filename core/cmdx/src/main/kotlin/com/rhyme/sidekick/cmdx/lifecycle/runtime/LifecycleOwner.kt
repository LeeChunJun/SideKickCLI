package com.rhyme.sidekick.cmdx.lifecycle.runtime

/**
 * 生命周期所有者接口
 *
 * 仿照 androidx.lifecycle.LifecycleOwner，任何拥有生命周期的对象都应该实现此接口。
 *
 * 这里刻意**不做泛型化**：生命周期状态机本身与业务状态无关，
 * 泛型化会导致 `LifecycleOwner<*>` 通配符，进而逼迫调用方到处做不安全强转。
 * 业务状态请由业务侧自行用 StateFlow 暴露。
 *
 * 使用示例：
 * ```kotlin
 * class MyApp : LifecycleOwner {
 *     override val lifecycle: Lifecycle = LifecycleRegistry()
 * }
 * ```
 */
interface LifecycleOwner {

    /**
     * 返回此所有者的生命周期对象
     */
    val lifecycle: Lifecycle
}
