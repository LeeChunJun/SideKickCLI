package com.rhyme.sidekick.cmdx.navigation.simple

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 导航器简易实现
 * 基于栈的命令行界面导航器
 */
class CLINavigator(startDestination: String) {
    private val _backStack = MutableStateFlow(listOf(CLINavEntry(startDestination)))
    val backStack: StateFlow<List<CLINavEntry>> = _backStack.asStateFlow()

    val currentEntry: CLINavEntry
        get() = _backStack.value.last()

    fun canGoBack(): Boolean = _backStack.value.size > 1

    fun navigate(route: String, args: Map<String, String> = emptyMap()) {
        val current = _backStack.value.toMutableList()
        current.add(CLINavEntry(route, args))
        _backStack.value = current
    }

    fun popBackStack(): Boolean {
        val current = _backStack.value
        if (current.size <= 1) return false
        _backStack.value = current.dropLast(1)
        return true
    }

    fun replace(route: String, args: Map<String, String> = emptyMap()) {
        val current = _backStack.value.toMutableList()
        current[current.lastIndex] = CLINavEntry(route, args)
        _backStack.value = current
    }
}