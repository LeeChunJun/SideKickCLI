package com.rhyme.sidekick.cmdx.navigation.runtime

/**
 * 导航目的地（NavDestination）
 * 定义“目的地”。它可以是一个 Class、一个 ID 或一个 Route 字符串。
 */
data class NavDestination(
    val route: String,
    val content: (NavEntry) -> Unit // 这里可以是 UI 渲染函数或占位符
)