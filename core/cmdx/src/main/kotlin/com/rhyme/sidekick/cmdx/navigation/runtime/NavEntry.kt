package com.rhyme.sidekick.cmdx.navigation.runtime

import com.rhyme.sidekick.cmdx.navigation.lifecycle.NavLifecycleObserver
import com.rhyme.sidekick.cmdx.navigation.lifecycle.NavLifecycleOwner
import com.rhyme.sidekick.cmdx.navigation.lifecycle.NavLifecycleState
import java.util.UUID
import java.util.concurrent.CopyOnWriteArrayList

/**
 *  导航条目 (NavEntry)
 *  对应 Android 的 NavBackStackEntry。
 *  它是目的地在 BackStack 中的实例，是回退栈中的最小单元，持有参数（Arguments）和唯一的 ID。
 */
class NavEntry(
    val id: String = UUID.randomUUID().toString(),
    val destination: NavDestination,
    val arguments: Map<String, Any?> = emptyMap()
) : NavLifecycleOwner {

    private var _state = NavLifecycleState.INITIALIZED
    override val lifecycleState: NavLifecycleState
        get() = _state

    // 使用 CopyOnWriteArrayList 保证线程安全，防止在回调过程中修改列表导致 ConcurrentModificationException
    private val observers = CopyOnWriteArrayList<NavLifecycleObserver>()

    fun addObserver(observer: NavLifecycleObserver) {
        observers.add(observer)
        // 模仿 Android 特性：添加观察者时立即同步当前状态
        // 同步时按从低到高的顺序触发 enter 回调，确保观察者能看到完整的生命周期状态（例如在 RESUMED 时也能收到 onCreate/onStart/onResume）
        val state = _state
        observer.onStateChanged(this, state)
        when (state) {
            NavLifecycleState.DESTROYED -> observer.onDestroy(this)
            else -> {
                if (state.ordinal >= NavLifecycleState.CREATED.ordinal) observer.onCreate(this)
                if (state.ordinal >= NavLifecycleState.STARTED.ordinal) observer.onStart(this)
                if (state.ordinal >= NavLifecycleState.RESUMED.ordinal) observer.onResume(this)
            }
        }
    }

    fun removeObserver(observer: NavLifecycleObserver) {
        observers.remove(observer)
    }

    fun updateState(newState: NavLifecycleState) {
        if (_state == newState) return
        val oldState = _state
        _state = newState

        // 通知所有观察者，语义化回调根据状态提升/降级分别映射到 onCreate/onStart/onResume 或 onPause/onStop/onDestroy
        for (observer in observers) {
            observer.onStateChanged(this, newState)

            if (newState == NavLifecycleState.DESTROYED) {
                observer.onDestroy(this)
                continue
            }

            if (newState.ordinal > oldState.ordinal) {
                // 状态提升（例如 CREATED -> STARTED -> RESUMED）
                when (newState) {
                    NavLifecycleState.CREATED -> observer.onCreate(this)
                    NavLifecycleState.STARTED -> observer.onStart(this)
                    NavLifecycleState.RESUMED -> observer.onResume(this)
                    else -> {}
                }
            } else if (newState.ordinal < oldState.ordinal) {
                // 状态降级（例如 RESUMED -> STARTED -> CREATED）
                when (newState) {
                    NavLifecycleState.STARTED -> observer.onPause(this)
                    NavLifecycleState.CREATED -> observer.onStop(this)
                    NavLifecycleState.INITIALIZED -> {
                        // 如果回到 INITIALIZED，视为完全清理（调用 onDestroy 作为兜底）
                        observer.onDestroy(this)
                    }
                    else -> {}
                }
            }
        }
    }

}