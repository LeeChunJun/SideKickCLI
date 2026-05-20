package com.rhyme.sidekick.cmdx.navigation.simple

/**
 * 简易导航实例，代表在回退栈里面的一个界面
 */
data class CLINavEntry(
    val route: String,
    val args: Map<String, String> = emptyMap()
)