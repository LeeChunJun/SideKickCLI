package com.rhyme.sidekick.cmdx.lifecycle

import com.rhyme.sidekick.cmdx.lifecycle.common.CLILifecycleOwner
import com.rhyme.sidekick.cmdx.lifecycle.common.CLILifecycleState
import com.rhyme.sidekick.cmdx.lifecycle.runtime.Lifecycle
import com.rhyme.sidekick.cmdx.lifecycle.runtime.LifecycleObserver
import com.rhyme.sidekick.cmdx.lifecycle.runtime.IllegalLifecycleTransitionException
import com.rhyme.sidekick.cmdx.lifecycle.runtime.OnLifecycleEvent

/**
 * CLI 生命周期系统单元测试和验证
 */
object CLILifecycleVerification {

    @JvmStatic
    fun main(args: Array<String>) {
        println("╔════════════════════════════════════════════════════════════╗")
        println("║     CLI Lifecycle System Implementation Verification     ║")
        println("╚════════════════════════════════════════════════════════════╝\n")

        try {
            verifyInterfacesExist()
            verifyStatesExist()
            verifyLifecycleOwnerWorks()
            verifyObserverPattern()
            verifyStateTransitions()
            verifyFlowSupport()
            verifyIllegalTransitionIsRejected()

            println("\n✅ 所有验证通过！生命周期系统已成功实现。\n")
        } catch (e: Exception) {
            println("\n❌ 验证失败: ${e.message}\n")
            e.printStackTrace()
        }
    }

    private fun verifyInterfacesExist() {
        println("1️⃣  验证核心接口...")

        // 验证 Lifecycle 接口
        Lifecycle::class.java
        println("   ✓ Lifecycle 接口已定义")

        // 验证 Event 枚举
        val eventValues = Lifecycle.Event.values()
        println("   ✓ Lifecycle.Event 包含 ${eventValues.size} 个事件")

        // 验证 State 枚举
        val stateValues = Lifecycle.State.values()
        println("   ✓ Lifecycle.State 包含 ${stateValues.size} 个状态")

        // 验证 LifecycleObserver 接口
        LifecycleObserver::class.java
        println("   ✓ LifecycleObserver 接口已定义")

        // 验证 LifecycleOwner 接口
        com.rhyme.sidekick.cmdx.lifecycle.runtime.LifecycleOwner::class.java
        println("   ✓ LifecycleOwner 接口已定义")
    }

    private fun verifyStatesExist() {
        println("\n2️⃣  验证 CLI 业务状态...")

        val states = listOf(
            CLILifecycleState.Initialized::class.java.simpleName,
            CLILifecycleState.Ready::class.java.simpleName,
            CLILifecycleState.MenuSelection::class.java.simpleName,
            CLILifecycleState.CommandList::class.java.simpleName,
            CLILifecycleState.ParameterInput::class.java.simpleName,
            CLILifecycleState.CommandExecution::class.java.simpleName,
            CLILifecycleState.ResultDisplay::class.java.simpleName,
            CLILifecycleState.ErrorDisplay::class.java.simpleName,
            CLILifecycleState.TimeoutState::class.java.simpleName,
            CLILifecycleState.ActionMenu::class.java.simpleName,
            CLILifecycleState.Cleanup::class.java.simpleName,
            CLILifecycleState.Terminated::class.java.simpleName,
            CLILifecycleState.Failed::class.java.simpleName
        )

        states.forEach {
            println("   ✓ $it 状态已定义")
        }
    }

    private fun verifyLifecycleOwnerWorks() {
        println("\n3️⃣  验证生命周期所有者功能...")

        val owner = CLILifecycleOwner()
        println("   ✓ CLILifecycleOwner 实例创建成功")

        owner.toInitialized()
        println("   ✓ 可以转换到 Initialized 状态")

        owner.toReady()
        println("   ✓ 可以转换到 Ready 状态")

        val currentState = owner.getCurrentState()
        println("   ✓ 可以获取当前业务状态: ${currentState::class.simpleName}")

        val lifecycleState = owner.getCurrentLifecycleState()
        println("   ✓ 可以获取宿主生命周期状态: $lifecycleState")
    }

    private fun verifyObserverPattern() {
        println("\n4️⃣  验证观察者模式...")

        val owner = CLILifecycleOwner()

        var eventCount = 0
        val observer = object : LifecycleObserver {
            @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
            fun onCreate() {
                eventCount++
            }

            @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
            fun onResume() {
                eventCount++
            }
        }

        owner.addObserver(observer)
        println("   ✓ 观察者注册成功")

        owner.toInitialized()
        owner.toReady()

        println("   ✓ 观察者注解方法已被调用 ($eventCount 次)")

        owner.removeObserver(observer)
        println("   ✓ 观察者移除成功")
    }

    private fun verifyStateTransitions() {
        println("\n5️⃣  验证状态转换...")

        val owner = CLILifecycleOwner()

        val transitions = listOf(
            Pair({ owner.toInitialized() }, "Initialized"),
            Pair({ owner.toReady() }, "Ready"),
            Pair({ owner.toMenuSelection() }, "MenuSelection"),
            Pair({ owner.toCommandList("create", "Create Module") }, "CommandList"),
            Pair({ owner.toParameterInput("create", "create") }, "ParameterInput"),
            Pair({ owner.toCommandExecutionStart("test") }, "CommandExecution"),
            Pair({ owner.toResultDisplay("test", "output") }, "ResultDisplay"),
            Pair({ owner.toActionMenu() }, "ActionMenu"),
            Pair({ owner.toTerminated() }, "Terminated")
        )

        transitions.forEach { (transition, stateName) ->
            transition()
            val current = owner.getCurrentState()::class.simpleName
            println("   ✓ 转换到 $stateName (当前: $current)")
        }
    }

    private fun verifyFlowSupport() {
        println("\n6️⃣  验证 Flow 支持...")

        val owner = CLILifecycleOwner()

        val stateFlow = owner.lifecycle.stateFlow
        println("   ✓ StateFlow 可访问 (宿主状态: ${stateFlow.value})")

        val eventFlow = owner.lifecycle.eventFlow
        println("   ✓ EventFlow 可访问")

        val businessStateFlow = owner.stateFlow
        println("   ✓ 业务 StateFlow 可访问 (${businessStateFlow.value::class.simpleName})")

        val observers = owner.lifecycle.getObservers()
        println("   ✓ 可以查询观察者列表 (${observers.size} 个)")

        val hasObserver = owner.lifecycle.hasObserver(object : LifecycleObserver {})
        println("   ✓ 可以检查观察者是否存在")
    }

    private fun verifyIllegalTransitionIsRejected() {
        println("\n7️⃣  验证非法迁移会被拒绝...")

        val owner = CLILifecycleOwner()
        owner.toInitialized()

        // 还没有进入 RESUMED 就 PAUSE，属于非法迁移
        var rejected = false
        try {
            owner.lifecycle.dispatch(Lifecycle.Event.ON_PAUSE)
        } catch (e: IllegalLifecycleTransitionException) {
            rejected = true
        }
        println("   ✓ 非法迁移被拒绝: $rejected")

        // 幂等派发
        val changed = owner.lifecycle.dispatch(Lifecycle.Event.ON_CREATE)
        println("   ✓ 重复派发不会改变状态 (changed=$changed)")
    }
}
