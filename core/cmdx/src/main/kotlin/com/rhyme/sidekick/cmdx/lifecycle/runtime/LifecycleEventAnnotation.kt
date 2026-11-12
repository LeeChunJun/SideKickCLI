package com.rhyme.sidekick.cmdx.lifecycle.runtime

/**
 * 生命周期事件注解
 *
 * 标记方法在指定生命周期事件发生时调用。
 * 仿照 androidx.lifecycle 中的 @OnLifecycleEvent 注解。
 *
 * 与 Android 原版不同的是，这里直接使用**枚举**作为注解参数而不是字符串：
 * - 事件重命名时编译期即可发现所有调用点，不会留下拼写错误的"静默失效"注解；
 * - 不需要运行期做字符串匹配。
 *
 * 使用示例：
 * ```kotlin
 * class MyObserver : LifecycleObserver {
 *     @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
 *     fun onCreate() {
 *         println("应用已创建")
 *     }
 *
 *     @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
 *     fun onDestroy() {
 *         println("应用正在销毁")
 *     }
 * }
 * ```
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class OnLifecycleEvent(
    val value: Lifecycle.Event
)
