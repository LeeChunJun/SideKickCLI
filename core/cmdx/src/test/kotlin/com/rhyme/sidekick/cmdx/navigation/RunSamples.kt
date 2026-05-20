package com.rhyme.sidekick.cmdx.navigation

import com.rhyme.sidekick.cmdx.navigation.common.createNavController
import com.rhyme.sidekick.cmdx.navigation.lifecycle.NavLifecycleObserver
import com.rhyme.sidekick.cmdx.navigation.lifecycle.NavLifecycleOwner
import com.rhyme.sidekick.cmdx.navigation.lifecycle.NavLifecycleState
import com.rhyme.sidekick.cmdx.navigation.runtime.NavEntry
import com.rhyme.sidekick.cmdx.navigation.simple.CLINavigator

/**
 * 额外的可执行示例，方便在 IDE / tests 中直接运行。
 */

fun main() {
    println("Ready: com.rhyme.sidekick.cmdx.navigation.runSimpleSample")
    runSimpleSample()
    println()
    println("Ready: com.rhyme.sidekick.cmdx.navigation.runRuntimeSample")
    runRuntimeSample()
    println()
    println("Ready: com.rhyme.sidekick.cmdx.navigation.runRuntimeObserverSample")
    runRuntimeObserverSample()
}

fun runSimpleSample() {
    val cli = CLINavigator("home")
    println("Start: ${cli.currentEntry.route}")

    cli.navigate("todo")
    println("After navigate: ${cli.currentEntry.route}")

    cli.popBackStack()
    println("After pop: ${cli.currentEntry.route}")
}

fun runRuntimeSample() {
    val navController = createNavController {
        destination("home") { entry ->
            println("Rendering HOME screen")
        }
        destination("detail") { entry ->
            val userId = entry.arguments["userId"]
            println("Rendering DETAIL screen for user: $userId")
        }
    }

    val myObserver = object : NavLifecycleObserver {

        override fun onCreate(owner: NavLifecycleOwner) {
            val entry = owner as NavEntry
            println("--- [Observer] 页面 ${entry.destination.route} 创建了，可以初始化数据了")
            entry.destination.content(entry)
        }

    }

    navController.navigate("home")
    navController.currentEntry?.addObserver(myObserver)

    navController.navigate("detail", mapOf("userId" to "12345"))
    navController.currentEntry?.addObserver(myObserver)

    println("Current stack size: ${navController.backStack.value.size}")
    navController.popBackStack()
    println("After pop, current route: ${navController.currentEntry?.destination?.route}")

    // 移除当前 Entry 监听
    navController.currentEntry?.removeObserver(myObserver)
}

fun runRuntimeObserverSample() {
    val myObserver = object : NavLifecycleObserver {
        override fun onStateChanged(
            source: NavLifecycleOwner,
            state: NavLifecycleState
        ) {
            val entry = source as NavEntry
            println("--- [Observer] 页面 ${entry.destination.route} 状态变更为: $state")
        }

        override fun onCreate(owner: NavLifecycleOwner) {
            val entry = owner as NavEntry
            println("--- [Observer] 页面 ${entry.destination.route} 创建了，可以初始化数据了")
        }

        override fun onStart(owner: NavLifecycleOwner) {
            val entry = owner as NavEntry
            println("--- [Observer] 页面 ${entry.destination.route} 启动了，开始可见")
        }

        override fun onResume(owner: NavLifecycleOwner) {
            val entry = owner as NavEntry
            println("--- [Observer] 页面 ${entry.destination.route} 获得了焦点，处于活跃状态")
        }

        override fun onPause(owner: NavLifecycleOwner) {
            val entry = owner as NavEntry
            println("--- [Observer] 页面 ${entry.destination.route} 暂停了，失去焦点")
        }

        override fun onStop(owner: NavLifecycleOwner) {
            val entry = owner as NavEntry
            println("--- [Observer] 页面 ${entry.destination.route} 停止了，不再可见")
        }

        override fun onDestroy(owner: NavLifecycleOwner) {
            val entry = owner as NavEntry
            println("--- [Observer] 页面 ${entry.destination.route} 销毁了，释放资源")
        }
    }

    val controller = createNavController {
        destination("home") { entry ->
            /* UI */
        }
        destination("settings") { entry ->
            /* UI */
        }
    }

    println(">> 准备跳转到主页")
    controller.navigate("home")
    // 为新创建的 home entry 添加监听
    controller.currentEntry?.addObserver(myObserver)

    println(">> 准备跳转到设置页")
    controller.navigate("settings")
    // 为新创建的 settings entry 添加监听（home 已经有了）
    controller.currentEntry?.addObserver(myObserver)

    println(">> 准备返回")
    controller.popBackStack()
    // settings 被销毁，其监听会自动失效；home 恢复为 RESUMED

    // 清理：从现存的 entry 移除监听
    controller.currentEntry?.removeObserver(myObserver)
}
