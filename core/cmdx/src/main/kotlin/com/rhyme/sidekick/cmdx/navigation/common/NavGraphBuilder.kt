package com.rhyme.sidekick.cmdx.navigation.common

import com.rhyme.sidekick.cmdx.navigation.runtime.NavController
import com.rhyme.sidekick.cmdx.navigation.runtime.NavDestination
import com.rhyme.sidekick.cmdx.navigation.runtime.NavEntry
import com.rhyme.sidekick.cmdx.navigation.runtime.NavGraph

/**
 * 辅助 DSL (贴近 Android 用法)
 * 用法类似 Android 的 NavHost，提供一个 DSL 构造器
 */
class NavGraphBuilder {
    private val destinations = mutableMapOf<String, NavDestination>()

    fun destination(route: String, content: (NavEntry) -> Unit) {
        destinations[route] = NavDestination(route, content)
    }

    fun build() = NavGraph(destinations)
}

fun createNavController(block: NavGraphBuilder.() -> Unit): NavController {
    val builder = NavGraphBuilder()
    builder.block()
    return NavController(builder.build())
}