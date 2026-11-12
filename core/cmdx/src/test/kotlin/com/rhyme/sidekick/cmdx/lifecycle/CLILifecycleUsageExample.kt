package com.rhyme.sidekick.cmdx.lifecycle

import com.rhyme.sidekick.cmdx.lifecycle.common.CLILifecycleEvent
import com.rhyme.sidekick.cmdx.lifecycle.common.CLILifecycleOwner
import com.rhyme.sidekick.cmdx.lifecycle.common.CLILifecycleState
import com.rhyme.sidekick.cmdx.lifecycle.runtime.Lifecycle
import com.rhyme.sidekick.cmdx.lifecycle.runtime.LifecycleObserver
import com.rhyme.sidekick.cmdx.lifecycle.runtime.OnLifecycleEvent
import com.rhyme.sidekick.cmdx.lifecycle.runtime.observeStates
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

/**
 * CLI 生命周期使用示例
 *
 * 演示如何在实际应用中使用新的生命周期系统
 */
object CLILifecycleUsageExample {

    /**
     * 示例 1：基本的生命周期流程
     */
    suspend fun exampleBasicLifecycle() {
        println("=== 示例 1：基本的生命周期流程 ===\n")

        val lifecycleOwner = CLILifecycleOwner()

        // 添加观察者来监听宿主生命周期
        addStateChangeListener(lifecycleOwner)

        // 模拟应用生命周期流程
        lifecycleOwner.toInitialized()
        println("→ 应用已初始化\n")

        Thread.sleep(500)
        lifecycleOwner.toReady()
        println("→ 应用已就绪\n")

        Thread.sleep(500)
        lifecycleOwner.toMenuSelection()
        println("→ 显示主菜单\n")

        Thread.sleep(500)
        lifecycleOwner.toCommandList("create", "Create Module")
        println("→ 显示 create 模块的命令列表\n")

        Thread.sleep(500)
        lifecycleOwner.toParameterInput("create", "create command")
        println("→ 用户输入参数\n")

        Thread.sleep(500)
        lifecycleOwner.toCommandExecutionStart("android create --output=/path")
        println("→ 开始执行命令\n")

        // 模拟执行进度
        for (progress in listOf(25, 50, 75)) {
            Thread.sleep(300)
            lifecycleOwner.toCommandExecutionProgress("android create --output=/path", progress)
            println("→ 执行进度: $progress%\n")
        }

        Thread.sleep(300)
        lifecycleOwner.toCommandExecutionFinish("android create --output=/path")
        println("→ 命令执行完成\n")

        Thread.sleep(500)
        lifecycleOwner.toResultDisplay(
            "create",
            "Project successfully created!\nProject location: /path/to/project",
            CLILifecycleState.ResultDisplay.OutputType.TEXT
        )
        println("→ 显示结果\n")

        Thread.sleep(500)
        lifecycleOwner.toActionMenu("create")
        println("→ 显示操作菜单\n")

        Thread.sleep(500)
        lifecycleOwner.toCleanup()
        println("→ 清理资源\n")

        Thread.sleep(500)
        lifecycleOwner.toTerminated()
        println("→ 应用退出\n")
    }

    /**
     * 示例 2：错误处理流程
     */
    suspend fun exampleErrorHandling() {
        println("=== 示例 2：错误处理流程 ===\n")

        val lifecycleOwner = CLILifecycleOwner()
        addStateChangeListener(lifecycleOwner)

        lifecycleOwner.toInitialized()
        println("→ 应用初始化\n")

        Thread.sleep(500)
        lifecycleOwner.toReady()
        println("→ 应用就绪\n")

        Thread.sleep(500)
        lifecycleOwner.toMenuSelection()
        lifecycleOwner.toCommandList("create", "Create Module")
        lifecycleOwner.toParameterInput("create", "create command")
        println("→ 进入参数输入\n")

        Thread.sleep(500)
        lifecycleOwner.toCommandExecutionStart("android create --output=/invalid")
        println("→ 开始执行命令（但会失败）\n")

        Thread.sleep(1000)
        lifecycleOwner.toErrorDisplay(
            "create",
            "Error: Invalid output directory",
            "ERR_INVALID_PATH",
            "1. 检查目录路径是否正确\n2. 确保目录存在或使用 mkdir 创建",
            "java.nio.file.NoSuchFileException: /invalid"
        )
        println("→ 显示错误信息\n")

        Thread.sleep(500)
        lifecycleOwner.toActionMenu("create")
        println("→ 用户可以重试或返回菜单\n")

        Thread.sleep(500)
        lifecycleOwner.toFailed(errorPhase = "命令执行")
        println("→ 应用失败退出\n")
    }

    /**
     * 示例 3：超时处理流程
     */
    suspend fun exampleTimeoutHandling() {
        println("=== 示例 3：超时处理流程 ===\n")

        val lifecycleOwner = CLILifecycleOwner()
        addStateChangeListener(lifecycleOwner)

        lifecycleOwner.toInitialized()
        lifecycleOwner.toReady()
        lifecycleOwner.toMenuSelection()
        lifecycleOwner.toCommandList("emulator", "Emulator Module")
        println("→ 用户选择 emulator 模块\n")

        Thread.sleep(500)
        lifecycleOwner.toCommandExecutionStart("android emulator start medium_phone")
        println("→ 开始启动虚拟设备\n")

        // 模拟长时间执行
        for (second in listOf(1, 2, 3, 4, 5)) {
            Thread.sleep(300)
            println("→ 执行中... ($second 秒)\n")
        }

        lifecycleOwner.toTimeoutState("emulator", 5)
        println("→ 命令执行超时 (超过 5 分钟限制)\n")

        Thread.sleep(500)
        lifecycleOwner.toActionMenu("emulator")
        println("→ 用户可以重试或返回\n")

        Thread.sleep(500)
        lifecycleOwner.toTerminated()
        println("→ 应用退出\n")
    }

    /**
     * 示例 4：自定义观察者
     */
    suspend fun exampleCustomObserver() {
        println("=== 示例 4：自定义观察者 ===\n")

        val lifecycleOwner = CLILifecycleOwner()

        // 添加自定义观察者
        val customObserver = MyCustomLifecycleObserver()
        lifecycleOwner.addObserver(customObserver)

        lifecycleOwner.toInitialized()
        println("→ 应用初始化\n")

        Thread.sleep(500)
        lifecycleOwner.toReady()
        println("→ 应用就绪\n")

        Thread.sleep(500)
        lifecycleOwner.toMenuSelection()
        println("→ 显示菜单\n")

        Thread.sleep(500)
        lifecycleOwner.toTerminated()
        println("→ 应用退出\n")
    }

    /**
     * 示例 5：使用 Flow 观察业务状态变化
     */
    suspend fun exampleFlowObservation() {
        println("=== 示例 5：使用 Flow 观察状态变化 ===\n")

        val lifecycleOwner = CLILifecycleOwner()
        val coroutineScope = CoroutineScope(Dispatchers.Default)

        // 监听业务状态
        lifecycleOwner.observeStates(coroutineScope) { state ->
            val stateName = state::class.simpleName ?: "Unknown"
            val timestamp = state.getFormattedTimestamp()
            println("[$timestamp] 状态变化: $stateName → ${state.message}")
        }

        // 监听宿主生命周期状态
        lifecycleOwner.lifecycle.observeStates(coroutineScope) { lifecycleState ->
            println("🔄 宿主生命周期: $lifecycleState")
        }

        // 执行状态转换
        lifecycleOwner.toInitialized()
        Thread.sleep(500)
        lifecycleOwner.toReady()
        Thread.sleep(500)
        lifecycleOwner.toMenuSelection()
        Thread.sleep(500)
        lifecycleOwner.toTerminated()
        Thread.sleep(500)
    }

    /**
     * 添加宿主生命周期监听器
     */
    private fun addStateChangeListener(lifecycleOwner: CLILifecycleOwner) {
        val observer = object : LifecycleObserver {
            @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
            fun onCreate() {
                println("📍 [观察者] ON_CREATE 事件已触发")
            }

            @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
            fun onResume() {
                println("📍 [观察者] ON_RESUME 事件已触发")
            }

            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            fun onDestroy() {
                println("📍 [观察者] ON_DESTROY 事件已触发")
            }
        }
        lifecycleOwner.addObserver(observer)
    }
}

/**
 * 自定义生命周期观察者示例
 */
class MyCustomLifecycleObserver : LifecycleObserver {

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate() {
        println("✓ [自定义观察者] 应用已创建，初始化依赖项...")
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume() {
        println("✓ [自定义观察者] 应用已准备就绪，开始加载资源...")
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        println("✓ [自定义观察者] 应用销毁，释放资源和保存状态...")
    }
}

/**
 * 业务事件监听示例
 */
class BusinessEventObserver {

    suspend fun observe(owner: CLILifecycleOwner) {
        owner.eventFlow.collect { event ->
            when (event) {
                CLILifecycleEvent.ON_MENU_SELECTION -> println("🔸 进入菜单")
                CLILifecycleEvent.ON_COMMAND_EXECUTION_START -> println("🔸 开始执行命令")
                CLILifecycleEvent.ON_COMMAND_EXECUTION_PROGRESS -> println("🔸 命令进度更新")
                CLILifecycleEvent.ON_COMMAND_EXECUTION_FINISH -> println("🔸 命令执行结束")
                CLILifecycleEvent.ON_FAILED -> println("🔸 应用失败")
                else -> {}
            }
        }
    }
}

/**
 * 执行所有示例
 */
suspend fun runAllExamples() {
    try {
        CLILifecycleUsageExample.exampleBasicLifecycle()
        println("\n" + "=".repeat(50) + "\n")

        CLILifecycleUsageExample.exampleErrorHandling()
        println("\n" + "=".repeat(50) + "\n")

        CLILifecycleUsageExample.exampleTimeoutHandling()
        println("\n" + "=".repeat(50) + "\n")

        CLILifecycleUsageExample.exampleCustomObserver()
        println("\n" + "=".repeat(50) + "\n")

        CLILifecycleUsageExample.exampleFlowObservation()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun main(args: Array<String>) {
    runBlocking {
        runAllExamples()
    }
}
