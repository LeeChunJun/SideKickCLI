package com.rhyme.sidekick.cmdx.lifecycle.runtime

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.lang.reflect.Method

/**
 * 生命周期注册表实现
 *
 * 仿照 androidx.lifecycle.LifecycleRegistry，提供生命周期的具体实现。
 * 管理观察者注册、状态转换和事件分发。
 *
 * 特性：
 * - 线程安全的状态管理
 * - 事件驱动的状态转换
 * - 观察者自动发现和通知
 * - 支持注解驱动的事件处理
 *
 * @param S 生命周期状态类型
 */
class LifecycleRegistry<S : Any>(
    initialState: S,
    private val initialLifecycleState: Lifecycle.State = Lifecycle.State.CREATED
) : Lifecycle<S> {

    private val mutex = Mutex()

    // 观察者集合
    private val observers = mutableListOf<LifecycleObserver>()

    // 事件方法缓存，用于高效的注解方法查找
    private val eventMethodCache = mutableMapOf<Pair<Class<*>, String>, Method?>()

    // 当前生命周期状态
    private var _currentState = initialLifecycleState

    // 状态流
    private val _stateFlow = MutableStateFlow(initialState)
    override val stateFlow: StateFlow<S> = _stateFlow.asStateFlow()

    // 事件流
    private val _eventFlow = MutableSharedFlow<Lifecycle.Event>(
        replay = 0,
        extraBufferCapacity = 64
    )
    override val eventFlow: Flow<Lifecycle.Event> = _eventFlow.asSharedFlow()

    override fun addObserver(observer: LifecycleObserver) {
        // 同步添加观察者并触发已有事件
        if (observers.contains(observer)) return

        observers.add(observer)
        // 触发初始事件以使观察者跟上当前状态
        triggerInitialEvents(observer)
    }

    override fun removeObserver(observer: LifecycleObserver) {
        observers.remove(observer)
    }

    override fun getObservers(): List<LifecycleObserver> = observers.toList()

    override fun hasObserver(observer: LifecycleObserver): Boolean = observer in observers

    /**
     * 转换到新状态并触发相应事件
     */
    suspend fun handleEvent(event: Lifecycle.Event, newState: S) {
        mutex.withLock {
            val targetState = event.targetState
            _currentState = targetState
            _stateFlow.value = newState

            // 发送事件到所有观察者
            _eventFlow.emit(event)
            notifyObservers(event)
        }
    }

    /**
     * 同步版本的状态转换
     */
    fun handleEventSync(event: Lifecycle.Event, newState: S) {
        synchronized(this) {
            val targetState = event.targetState
            _currentState = targetState
            _stateFlow.value = newState

            // 发送事件到观察者
            try {
                notifyObserversSync(event)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 获取当前状态
     */
    fun getCurrentState(): S = _stateFlow.value

    /**
     * 获取当前生命周期状态
     */
    override val currentState: Lifecycle.State
        get() = _currentState

    /**
     * 检查是否处于至少某个状态
     */
    fun isAtLeast(state: Lifecycle.State): Boolean = _currentState.isAtLeast(state)

    /**
     * 为新添加的观察者触发初始事件
     */
    private fun triggerInitialEvents(observer: LifecycleObserver) {
        // 根据当前状态，为观察者触发相应的初始事件
        val initialEvents = when (currentState) {
            Lifecycle.State.DESTROYED -> emptyList()
            Lifecycle.State.CREATED -> listOf(Lifecycle.Event.ON_CREATE)
            Lifecycle.State.STARTED -> listOf(
                Lifecycle.Event.ON_CREATE,
                Lifecycle.Event.ON_READY
            )

            Lifecycle.State.RESUMED -> listOf(
                Lifecycle.Event.ON_CREATE,
                Lifecycle.Event.ON_READY
            )
        }

        initialEvents.forEach { event ->
            try {
                callObserverMethod(observer, event)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 通知所有观察者（异步）
     */
    private suspend fun notifyObservers(event: Lifecycle.Event) {
        observers.forEach { observer ->
            try {
                callObserverMethod(observer, event)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 通知所有观察者（同步）
     */
    private fun notifyObserversSync(event: Lifecycle.Event) {
        observers.forEach { observer ->
            try {
                callObserverMethod(observer, event)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 调用观察者的方法 - 支持两种方式：
     * 1. @OnLifecycleEvent 注解方式
     * 2. LifecycleEventObserver 函数式接口方式
     */

    private fun callObserverMethod(observer: LifecycleObserver, event: Lifecycle.Event) {
        // 方式 1: 如果是 LifecycleEventObserver 函数式接口，直接调用
        if (observer is LifecycleEventObserver) {
            try {
                // 这里需要 LifecycleOwner，我们通过获取当前所有者来传递
                // 为了简化，我们可以创建一个临时的 LifecycleOwner 包装器
                val lifecycleOwner: LifecycleOwner<Any> = object : LifecycleOwner<Any> {
                    @Suppress("UNCHECKED_CAST")
                    override val lifecycle = this@LifecycleRegistry as Lifecycle<Any>
                }
                observer.onStateChanged(lifecycleOwner, event)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return
        }

        // 方式 2: 使用 @OnLifecycleEvent 注解方式
        val observerClass = observer::class.java
        val cacheKey = Pair(observerClass, event.name)

        // 从缓存中获取方法，或者进行查找
        val method = eventMethodCache.getOrPut(cacheKey) {
            findEventMethod(observerClass, event)
        }

        method?.let {
            try {
                it.isAccessible = true
                it.invoke(observer)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 查找观察者中标注了 @OnLifecycleEvent 的方法
     */
    private fun findEventMethod(clazz: Class<*>, event: Lifecycle.Event): Method? {
        return clazz.declaredMethods.find { method ->
            val annotation = method.getAnnotation(OnLifecycleEvent::class.java)
            annotation?.value == event.name
        }
    }
}
