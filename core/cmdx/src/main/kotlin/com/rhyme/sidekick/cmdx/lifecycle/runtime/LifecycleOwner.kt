package com.rhyme.sidekick.cmdx.lifecycle.runtime

/**
 * 生命周期所有者接口
 * 
 * 仿照 androidx.lifecycle.LifecycleOwner，任何拥有生命周期的对象都应该实现此接口。
 * 提供对其生命周期对象的访问。
 * 
 * 使用示例：
 * ```kotlin
 * class MyActivity : LifecycleOwner<CLILifecycleState> {
 *     override val lifecycle: Lifecycle<CLILifecycleState> = LifecycleRegistry()
 * }
 * ```
 */
interface LifecycleOwner<S : Any> {
    /**
     * 返回此所有者的生命周期对象
     */
    val lifecycle: Lifecycle<S>
}
