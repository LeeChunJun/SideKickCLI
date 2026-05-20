package com.rhyme.sidekick.cmdx.navigation

import com.rhyme.sidekick.cmdx.navigation.common.createNavController
import com.rhyme.sidekick.cmdx.navigation.lifecycle.NavLifecycleObserver
import com.rhyme.sidekick.cmdx.navigation.lifecycle.NavLifecycleOwner
import com.rhyme.sidekick.cmdx.navigation.runtime.NavEntry

/**
 * navigation 用例
 */
class UseNavSample {
    fun main() {
        // 1. 定义导航图
        val navController = createNavController {
            destination("home") { entry ->
                println("Rendering HOME screen")
            }
            destination("detail") { entry ->
                val userId = entry.arguments["userId"]
                println("Rendering DETAIL screen for user: $userId")
            }
        }

        // 2. 初始跳转
        navController.navigate("home")

        // 3. 跳转详情页
        navController.navigate("detail", mapOf("userId" to "12345"))

        // 4. 模拟返回
        println("Current stack size: ${navController.backStack.value.size}")
        navController.popBackStack()
        println("After pop, current route: ${navController.currentEntry?.destination?.route}")
    }

    fun mainWithObserver() {
        val controller = createNavController {
            destination("home") { /* UI */ }
            destination("settings") { /* UI */ }
        }

        // 模拟在 UI 组件中监听
        val myObserver = object : NavLifecycleObserver {
            override fun onCreate(owner: NavLifecycleOwner) {
                println("--- [Observer] 页面创建了，可以初始化数据了")
            }

            override fun onResume(owner: NavLifecycleOwner) {
                val entry = owner as NavEntry
                println("--- [Observer] 页面 ${entry.destination.route} 获得了焦点")
            }

            override fun onDestroy(owner: NavLifecycleOwner) {
                println("--- [Observer] 页面销毁了，释放资源")
            }
        }

        // 在导航前或导航后添加观察者均可
        controller.navigate("home")
        controller.currentEntry?.addObserver(myObserver)

        println(">> 准备跳转到设置页")
        controller.navigate("settings")

        println(">> 准备返回")
        controller.popBackStack()
    }
}