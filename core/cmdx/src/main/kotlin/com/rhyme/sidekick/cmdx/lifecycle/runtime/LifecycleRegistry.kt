package com.rhyme.sidekick.cmdx.lifecycle.runtime

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.util.Optional
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.coroutines.EmptyCoroutineContext

/**
 * 非法生命周期迁移异常
 */
class IllegalLifecycleTransitionException(message: String) : IllegalStateException(message)

/**
 * 生命周期注册表实现
 *
 * 仿照 androidx.lifecycle.LifecycleRegistry，提供生命周期的具体实现。
 *
 * 与旧版实现相比修正了这些问题：
 * - **单一锁**：状态变更、观察者集合、历史事件全部由同一把锁保护，
 *   不再出现 `Mutex` 与 `synchronized` 两把锁互不排斥的竞态。
 * - **迁移守卫**：非法迁移抛 [IllegalLifecycleTransitionException]，不会静默把状态写坏。
 * - **幂等**：重复派发同一事件是空操作（返回 false），不会重复通知观察者。
 * - **事件流不阻塞**：使用 `tryEmit` + `DROP_OLDEST`，慢消费者不会卡死状态机。
 * - **真实历史回放**：记录实际派发过的事件，新观察者按真实顺序收到 catch-up 回调。
 * - **反射支持继承**：`@OnLifecycleEvent` 方法在父类声明时同样可以被发现。
 * - **观察者异常隔离**：单个观察者抛异常不会影响其它观察者，并可通过
 *   [onObserverError] 接管异常上报。
 *
 * 线程模型：
 * - 默认在**调用线程**同步回调观察者（与 Android 的主线程模型一致，
 *   调用方需自行保证在 UI 线程派发）。
 * - 传入 [observerDispatcher] 后，观察者回调会被投递到该线程执行，
 *   适合 Compose / Mosaic 这类要求 UI 更新的场景。
 *
 * @param initialState 初始状态
 * @param observerDispatcher 观察者回调线程，null 表示在调用线程同步回调
 */
class LifecycleRegistry(
    initialState: Lifecycle.State = Lifecycle.State.INITIALIZED,
    private val observerDispatcher: CoroutineDispatcher? = null
) : Lifecycle, LifecycleOwner {

    /**
     * 唯一锁：保护状态、观察者集合与历史事件
     */
    private val lock = Any()

    /**
     * 观察者集合：写时复制，允许在回调中安全地增删观察者
     */
    private val observers = CopyOnWriteArrayList<LifecycleObserver>()

    /**
     * 已真实派发过的事件历史，用于新观察者的 catch-up 回放
     */
    private val history = ArrayDeque<Lifecycle.Event>()

    /**
     * 注解方法缓存：key = (观察者类型, 事件)，value = 找到的方法（可能为空）
     */
    private val methodCache =
        ConcurrentHashMap<Pair<Class<*>, Lifecycle.Event>, Optional<Method>>()

    private val _stateFlow = MutableStateFlow(initialState)
    override val stateFlow: StateFlow<Lifecycle.State> = _stateFlow.asStateFlow()

    private val _eventFlow = MutableSharedFlow<Lifecycle.Event>(
        replay = 0,
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    override val eventFlow: Flow<Lifecycle.Event> = _eventFlow.asSharedFlow()

    override val lifecycle: Lifecycle
        get() = this

    override val currentState: Lifecycle.State
        get() = _stateFlow.value

    /**
     * 观察者回调抛异常时的上报钩子，为 null 时输出到标准错误流
     */
    var onObserverError: ((observer: LifecycleObserver, event: Lifecycle.Event, error: Throwable) -> Unit)? =
        null

    override fun canDispatch(event: Lifecycle.Event): Boolean = synchronized(lock) {
        val from = _stateFlow.value
        if (from == event.targetState) return false
        isLegal(from, event)
    }

    override fun dispatch(event: Lifecycle.Event): Boolean = synchronized(lock) {
        val from = _stateFlow.value

        // 幂等：已经是目标状态则什么都不做
        if (from == event.targetState) return false

        if (!isLegal(from, event)) {
            throw IllegalLifecycleTransitionException(
                "非法的生命周期迁移: $from -> ${event.targetState}（事件 ${event.name}）"
            )
        }

        _stateFlow.value = event.targetState
        history.addLast(event)
        while (history.size > MAX_HISTORY) {
            history.removeFirst()
        }

        // 非阻塞发射：缓冲区满时丢弃最旧事件，绝不背压卡住状态机
        _eventFlow.tryEmit(event)

        notifyObservers(event)
        true
    }

    override fun addObserver(observer: LifecycleObserver) {
        val replay: List<Lifecycle.Event>
        synchronized(lock) {
            if (observers.contains(observer)) return
            observers.add(observer)
            replay = history.toList()
        }
        // 回放真实发生过的历史事件，让新观察者跟上当前进度
        replay.forEach { invokeObserver(observer, it) }
    }

    override fun removeObserver(observer: LifecycleObserver) {
        observers.remove(observer)
    }

    override fun getObservers(): List<LifecycleObserver> = observers.toList()

    override fun hasObserver(observer: LifecycleObserver): Boolean = observer in observers

    /**
     * 校验状态迁移是否合法
     */
    private fun isLegal(from: Lifecycle.State, event: Lifecycle.Event): Boolean = when (event) {
        Lifecycle.Event.ON_CREATE -> from == Lifecycle.State.INITIALIZED
        Lifecycle.Event.ON_RESUME -> from == Lifecycle.State.CREATED || from == Lifecycle.State.PAUSED
        Lifecycle.Event.ON_PAUSE -> from == Lifecycle.State.RESUMED
        Lifecycle.Event.ON_DESTROY -> from != Lifecycle.State.TERMINATED
    }

    /**
     * 通知所有观察者
     *
     * 在锁内调用以保证事件顺序；[CopyOnWriteArrayList] 的迭代基于快照，
     * 观察者在回调中增删观察者不会导致并发修改异常。
     */
    private fun notifyObservers(event: Lifecycle.Event) {
        val snapshot = observers.toList()
        if (snapshot.isEmpty()) return

        val dispatcher = observerDispatcher
        if (dispatcher == null) {
            snapshot.forEach { invokeObserver(it, event) }
        } else {
            snapshot.forEach { observer ->
                dispatcher.dispatch(EmptyCoroutineContext) { invokeObserver(observer, event) }
            }
        }
    }

    /**
     * 调用观察者的回调，支持两种方式：
     * 1. [LifecycleEventObserver] 函数式接口
     * 2. `@OnLifecycleEvent` 注解方式
     */
    private fun invokeObserver(observer: LifecycleObserver, event: Lifecycle.Event) {
        try {
            if (observer is LifecycleEventObserver) {
                observer.onStateChanged(this, event)
            } else {
                val method = findEventMethod(observer.javaClass, event) ?: return
                method.isAccessible = true
                method.invoke(observer)
            }
        } catch (e: InvocationTargetException) {
            reportError(observer, event, e.targetException ?: e)
        } catch (e: Throwable) {
            reportError(observer, event, e)
        }
    }

    /**
     * 查找观察者中标注了 [OnLifecycleEvent] 的方法
     *
     * 会沿继承链向上查找，父类声明的回调方法同样有效。
     */
    private fun findEventMethod(clazz: Class<*>, event: Lifecycle.Event): Method? {
        val key = clazz to event
        methodCache[key]?.let { return it.orElse(null) }

        var current: Class<*>? = clazz
        var found: Method? = null
        while (current != null && current != Any::class.java && found == null) {
            found = current.declaredMethods.firstOrNull { method ->
                method.getAnnotation(OnLifecycleEvent::class.java)?.value == event
            }
            current = current.superclass
        }

        methodCache[key] = Optional.ofNullable(found)
        return found
    }

    /**
     * 上报观察者异常
     */
    private fun reportError(observer: LifecycleObserver, event: Lifecycle.Event, error: Throwable) {
        val handler = onObserverError
        if (handler != null) {
            handler(observer, event, error)
        } else {
            System.err.println(
                "[Lifecycle] 观察者 ${observer.javaClass.name} 处理 ${event.name} 失败: $error"
            )
        }
    }

    private companion object {
        const val MAX_HISTORY = 64
    }
}
