package com.rhyme.sidekick.cmdx.navigation.runtime

import com.rhyme.sidekick.cmdx.navigation.lifecycle.NavLifecycleState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 导航控制器 (NavController)
 * 核心大脑。负责 navigate、pop 操作，并维护当前的 backStack 状态。
 */
class NavController(private val graph: NavGraph) {

    private val _backStack = MutableStateFlow<List<NavEntry>>(emptyList())
    val backStack: StateFlow<List<NavEntry>> = _backStack.asStateFlow()

    val currentEntry: NavEntry?
        get() = _backStack.value.lastOrNull()

    /**
     * 导航到指定路由
     */
    fun navigate(route: String, args: Map<String, Any?> = emptyMap()) {
        val dest = graph.destinations[route] ?: throw IllegalArgumentException("Route $route not found")

        val current = currentEntry

        // 1. 旧页面从 RESUMED 降级到 STARTED (可见但非焦点)
        current?.updateState(NavLifecycleState.STARTED)

        val newEntry = NavEntry(destination = dest, arguments = args)

        val newList = _backStack.value.toMutableList()
        newList.add(newEntry)
        _backStack.value = newList

        // 2. 新页面依次经历 INITIALIZED -> CREATED -> STARTED -> RESUMED
        newEntry.updateState(NavLifecycleState.CREATED)
        newEntry.updateState(NavLifecycleState.STARTED)
        newEntry.updateState(NavLifecycleState.RESUMED)
    }

    /**
     * 返回上一页
     */
    fun popBackStack(): Boolean {
        val stack = _backStack.value
        if (stack.size <= 1) return false // 栈底，不可再退

        // 1. 当前页面销毁
        val lastEntry = stack.last()
        lastEntry.updateState(NavLifecycleState.STARTED)
        lastEntry.updateState(NavLifecycleState.CREATED)
        lastEntry.updateState(NavLifecycleState.DESTROYED)

        val newList = stack.dropLast(1)
        _backStack.value = newList

        // 2. 恢复之前的页面到 RESUMED
        currentEntry?.updateState(NavLifecycleState.RESUMED)

        return true
    }

    /**
     * 清理并跳转 (对应 popUpTo)
     */
    fun navigateAndPopUpTo(route: String, popUpToRoute: String, inclusive: Boolean = false) {
        val stack = _backStack.value.toMutableList()
        val index = stack.indexOfLast { it.destination.route == popUpToRoute }

        if (index != -1) {
            val removeCount = if (inclusive) stack.size - index else stack.size - index - 1
            repeat(removeCount) {
                stack.removeAt(stack.size - 1).updateState(NavLifecycleState.DESTROYED)
            }
        }

        _backStack.value = stack
        navigate(route)
    }
}