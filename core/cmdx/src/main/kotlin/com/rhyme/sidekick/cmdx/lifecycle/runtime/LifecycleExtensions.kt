package com.rhyme.sidekick.cmdx.lifecycle.runtime

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * 生命周期扩展函数
 *
 * 提供便捷的生命周期事件观察方式。
 *
 * 注意：这里统一使用 `collect` 而不是 `collectLatest`。
 * `collectLatest` 会在新值到来时取消上一个回调，对于事件处理是错误的语义
 * （长耗时的回调会被下一次事件打断）。
 */

/**
 * 在指定生命周期事件发生时执行代码
 *
 * 使用示例：
 * ```kotlin
 * lifecycle.onEvent(Lifecycle.Event.ON_RESUME, scope) { owner ->
 *     println("App is resumed!")
 * }
 * ```
 *
 * @return 注册的观察者，调用 [Lifecycle.removeObserver] 可取消订阅
 */
fun Lifecycle.onEvent(
    event: Lifecycle.Event,
    coroutineScope: CoroutineScope,
    block: suspend (LifecycleOwner) -> Unit
): LifecycleEventObserver {
    val observer = LifecycleEventObserver { source, current ->
        if (current == event) {
            coroutineScope.launch { block(source) }
        }
    }
    addObserver(observer)
    return observer
}

/**
 * 在指定生命周期状态被到达时执行代码（只触发一次）
 */
fun Lifecycle.onState(
    state: Lifecycle.State,
    coroutineScope: CoroutineScope,
    block: suspend (Lifecycle.State) -> Unit
): Job = coroutineScope.launch {
    stateFlow.first { it.isAtLeast(state) }
    block(currentState)
}

/**
 * 监听生命周期状态变化
 */
fun Lifecycle.observeStates(
    coroutineScope: CoroutineScope,
    block: suspend (Lifecycle.State) -> Unit
): Job = coroutineScope.launch {
    stateFlow.collect { block(it) }
}

/**
 * 监听生命周期事件变化
 */
fun Lifecycle.observeEvents(
    coroutineScope: CoroutineScope,
    block: suspend (Lifecycle.Event) -> Unit
): Job = coroutineScope.launch {
    eventFlow.collect { block(it) }
}

/**
 * 监听特定的生命周期事件
 */
fun Lifecycle.observeEvent(
    event: Lifecycle.Event,
    coroutineScope: CoroutineScope,
    block: suspend () -> Unit
): Job = coroutineScope.launch {
    eventFlow.filter { it == event }.collect { block() }
}

/**
 * 检查生命周期是否处于活跃状态（RESUMED）
 */
fun Lifecycle.isAlive(): Boolean = currentState == Lifecycle.State.RESUMED

/**
 * 检查生命周期是否已创建
 */
fun Lifecycle.isCreated(): Boolean = currentState.isAtLeast(Lifecycle.State.CREATED)

/**
 * 检查生命周期是否已暂停
 */
fun Lifecycle.isPaused(): Boolean = currentState == Lifecycle.State.PAUSED

/**
 * 检查生命周期是否已销毁
 */
fun Lifecycle.isTerminated(): Boolean = currentState.isTerminal

// ==================== LifecycleOwner 转发 ====================

fun LifecycleOwner.onEvent(
    event: Lifecycle.Event,
    coroutineScope: CoroutineScope,
    block: suspend (LifecycleOwner) -> Unit
): LifecycleEventObserver = lifecycle.onEvent(event, coroutineScope, block)

fun LifecycleOwner.onState(
    state: Lifecycle.State,
    coroutineScope: CoroutineScope,
    block: suspend (Lifecycle.State) -> Unit
): Job = lifecycle.onState(state, coroutineScope, block)

fun LifecycleOwner.observeStates(
    coroutineScope: CoroutineScope,
    block: suspend (Lifecycle.State) -> Unit
): Job = lifecycle.observeStates(coroutineScope, block)

fun LifecycleOwner.observeEvents(
    coroutineScope: CoroutineScope,
    block: suspend (Lifecycle.Event) -> Unit
): Job = lifecycle.observeEvents(coroutineScope, block)

fun LifecycleOwner.observeEvent(
    event: Lifecycle.Event,
    coroutineScope: CoroutineScope,
    block: suspend () -> Unit
): Job = lifecycle.observeEvent(event, coroutineScope, block)

fun LifecycleOwner.isAlive(): Boolean = lifecycle.isAlive()

fun LifecycleOwner.isCreated(): Boolean = lifecycle.isCreated()

fun LifecycleOwner.isPaused(): Boolean = lifecycle.isPaused()

fun LifecycleOwner.isTerminated(): Boolean = lifecycle.isTerminated()
