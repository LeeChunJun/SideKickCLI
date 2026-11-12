package com.rhyme.sidekick.cmdx.lifecycle.runtime

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

/**
 * 生命周期扩展函数
 *
 * 提供便捷的生命周期事件观察方式
 */

/**
 * 在指定生命周期事件发生时执行代码
 *
 * 使用示例：
 * ```kotlin
 * lifecycleOwner.onEvent(Lifecycle.Event.ON_READY) {
 *     println("App is ready!")
 * }
 * ```
 */
inline fun <S : Any> LifecycleOwner<S>.onEvent(
    et: Lifecycle.Event,
    coroutineScope: CoroutineScope,
    crossinline block: suspend (S) -> Unit
): LifecycleObserver {
    val observer = object : LifecycleEventObserver {

        override fun onStateChanged(
            source: LifecycleOwner<*>,
            event: Lifecycle.Event
        ) {
            if (et == event) {
                coroutineScope.launch {
                    @Suppress("UNCHECKED_CAST")
                    block(source.lifecycle.stateFlow.value as S)
                }
            }
        }
    }

    lifecycle.addObserver(observer)
    return observer
}

/**
 * 在指定生命周期状态下执行代码
 *
 * 使用示例：
 * ```kotlin
 * lifecycleOwner.onState(Lifecycle.State.RESUMED) {
 *     println("App is resumed!")
 * }
 * ```
 */
inline fun <S : Any> LifecycleOwner<S>.onState(
    state: Lifecycle.State,
    coroutineScope: CoroutineScope,
    crossinline block: suspend (S) -> Unit
) {
    coroutineScope.launch {
        lifecycle.stateFlow.collectLatest { currentState ->
            if (lifecycle.currentState.isAtLeast(state)) {
                block(currentState)
            }
        }
    }
}

/**
 * 监听生命周期状态变化
 */
fun <S : Any> LifecycleOwner<S>.observeStates(
    coroutineScope: CoroutineScope,
    block: suspend (S) -> Unit
) {
    coroutineScope.launch {
        lifecycle.stateFlow.collectLatest { state ->
            block(state)
        }
    }
}

/**
 * 监听生命周期事件变化
 */
fun <S : Any> LifecycleOwner<S>.observeEvents(
    coroutineScope: CoroutineScope,
    block: suspend (Lifecycle.Event) -> Unit
) {
    coroutineScope.launch {
        lifecycle.eventFlow.collectLatest { event ->
            block(event)
        }
    }
}

/**
 * 监听特定的生命周期事件
 */
fun <S : Any> LifecycleOwner<S>.observeEvent(
    event: Lifecycle.Event,
    coroutineScope: CoroutineScope,
    block: suspend () -> Unit
) {
    coroutineScope.launch {
        lifecycle.eventFlow
            .filter { it == event }
            .collectLatest {
                block()
            }
    }
}

/**
 * 检查生命周期是否处于活跃状态（RESUMED）
 */
fun <S : Any> LifecycleOwner<S>.isAlive(): Boolean =
    lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)

/**
 * 检查生命周期是否已创建
 */
fun <S : Any> LifecycleOwner<S>.isCreated(): Boolean =
    lifecycle.currentState.isAtLeast(Lifecycle.State.CREATED)

/**
 * 检查生命周期是否已启动
 */
fun <S : Any> LifecycleOwner<S>.isStarted(): Boolean =
    lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)
