package com.rhyme.sidekick.cmdx.navigation.runtime

/**
 * 导航图（NavGraph）
 * 目的地的集合，规定了哪些 Route 是合法的。
 */
class NavGraph(
    val destinations: Map<String, NavDestination>
)