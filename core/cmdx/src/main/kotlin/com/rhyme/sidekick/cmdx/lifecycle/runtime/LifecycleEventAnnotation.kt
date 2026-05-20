package com.rhyme.sidekick.cmdx.lifecycle.runtime

/**
 * 生命周期事件注解
 * 
 * 标记方法在指定生命周期事件发生时调用。
 * 仿照 androidx.lifecycle 中的 @OnLifecycleEvent 注解。
 * 
 * 使用示例：
 * ```kotlin
 * class MyObserver : LifecycleObserver {
 *     @OnLifecycleEvent(Lifecycle.Event.ON_READY)
 *     fun onReady() {
 *         println("App is ready!")
 *     }
 *     
 *     @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
 *     fun onDestroy() {
 *         println("App is being destroyed")
 *     }
 * }
 * ```
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class OnLifecycleEvent(
    val value: String // 使用 String 以支持运行时反射
)

/**
 * 便捷函数，用于创建注解值
 * 
 * 虽然 Kotlin 枚举不能直接用于注解参数，但可以使用 name 属性
 */
fun lifecycleEvent(event: Lifecycle.Event): String = event.name
