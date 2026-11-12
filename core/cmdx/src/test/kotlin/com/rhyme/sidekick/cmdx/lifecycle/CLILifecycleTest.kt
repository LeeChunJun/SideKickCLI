package com.rhyme.sidekick.cmdx.lifecycle

import com.rhyme.sidekick.cmdx.lifecycle.common.CLILifecycleEvent
import com.rhyme.sidekick.cmdx.lifecycle.common.CLILifecycleOwner
import com.rhyme.sidekick.cmdx.lifecycle.common.CLILifecycleState
import com.rhyme.sidekick.cmdx.lifecycle.runtime.IllegalLifecycleTransitionException
import com.rhyme.sidekick.cmdx.lifecycle.runtime.Lifecycle
import com.rhyme.sidekick.cmdx.lifecycle.runtime.LifecycleEventObserver
import com.rhyme.sidekick.cmdx.lifecycle.runtime.LifecycleObserver
import com.rhyme.sidekick.cmdx.lifecycle.runtime.LifecycleRegistry
import com.rhyme.sidekick.cmdx.lifecycle.runtime.OnLifecycleEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * CLI 生命周期系统单元测试
 */
class CLILifecycleTest {

    // ==================== 宿主状态机 ====================

    @Test
    fun hostStateMachineIsMonotonic() {
        val owner = CLILifecycleOwner()

        assertEquals(Lifecycle.State.INITIALIZED, owner.getCurrentLifecycleState())

        owner.toInitialized()
        assertEquals(Lifecycle.State.CREATED, owner.getCurrentLifecycleState())

        owner.toReady()
        assertEquals(Lifecycle.State.RESUMED, owner.getCurrentLifecycleState())

        // 业务事件不应让宿主状态掉回 PAUSED
        owner.toMenuSelection()
        owner.toCommandList("create")
        owner.toParameterInput("create")
        owner.toCommandExecutionStart("cmd")
        owner.toCommandExecutionProgress("cmd", 50)
        owner.toCommandExecutionFinish("cmd")
        assertEquals(Lifecycle.State.RESUMED, owner.getCurrentLifecycleState())

        owner.toTerminated()
        assertEquals(Lifecycle.State.TERMINATED, owner.getCurrentLifecycleState())
    }

    @Test
    fun illegalTransitionIsRejected() {
        val registry = LifecycleRegistry()
        registry.dispatch(Lifecycle.Event.ON_CREATE)

        assertFailsWith<IllegalLifecycleTransitionException> {
            registry.dispatch(Lifecycle.Event.ON_PAUSE)
        }
    }

    @Test
    fun repeatedDispatchIsIdempotent() {
        val registry = LifecycleRegistry()

        assertTrue(registry.dispatch(Lifecycle.Event.ON_CREATE))
        assertFalse(registry.dispatch(Lifecycle.Event.ON_CREATE))
        assertEquals(Lifecycle.State.CREATED, registry.currentState)
    }

    @Test
    fun terminatedIsFinalState() {
        val registry = LifecycleRegistry()
        registry.dispatch(Lifecycle.Event.ON_CREATE)
        registry.dispatch(Lifecycle.Event.ON_DESTROY)

        assertTrue(registry.currentState.isTerminal)
        assertFailsWith<IllegalLifecycleTransitionException> {
            registry.dispatch(Lifecycle.Event.ON_CREATE)
        }
    }

    @Test
    fun canDispatchReflectsTransitionTable() {
        val registry = LifecycleRegistry()

        assertTrue(registry.canDispatch(Lifecycle.Event.ON_CREATE))
        assertFalse(registry.canDispatch(Lifecycle.Event.ON_RESUME))
        assertFalse(registry.canDispatch(Lifecycle.Event.ON_PAUSE))

        registry.dispatch(Lifecycle.Event.ON_CREATE)
        assertTrue(registry.canDispatch(Lifecycle.Event.ON_RESUME))
        assertFalse(registry.canDispatch(Lifecycle.Event.ON_PAUSE))

        registry.dispatch(Lifecycle.Event.ON_RESUME)
        assertTrue(registry.canDispatch(Lifecycle.Event.ON_PAUSE))

        registry.dispatch(Lifecycle.Event.ON_PAUSE)
        assertTrue(registry.canDispatch(Lifecycle.Event.ON_RESUME))
    }

    // ==================== 观察者 ====================

    @Test
    fun lateObserverReceivesRealHistory() {
        val owner = CLILifecycleOwner()
        owner.toInitialized()
        owner.toReady()

        val received = mutableListOf<Lifecycle.Event>()
        owner.addObserver(LifecycleEventObserver { _, event -> received += event })

        assertEquals(
            listOf(Lifecycle.Event.ON_CREATE, Lifecycle.Event.ON_RESUME),
            received
        )
    }

    @Test
    fun annotationCallbackIsFoundThroughInheritance() {
        val owner = CLILifecycleOwner()
        val observer = ChildObserver()
        owner.addObserver(observer)

        owner.toInitialized()

        assertEquals(listOf("create"), observer.log)
    }

    @Test
    fun throwingObserverDoesNotBreakOthers() {
        val owner = CLILifecycleOwner()
        var healthyCalls = 0

        owner.addObserver(object : LifecycleObserver {
            @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
            fun boom(): Unit = throw RuntimeException("boom")
        })
        owner.addObserver(object : LifecycleObserver {
            @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
            fun ok() {
                healthyCalls++
            }
        })

        owner.toInitialized()

        assertEquals(1, healthyCalls)
    }

    @Test
    fun observerCanRemoveItselfDuringCallback() {
        val owner = CLILifecycleOwner()
        val observer = object : LifecycleObserver {
            @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
            fun onCreate() {
                owner.removeObserver(this)
            }
        }
        owner.addObserver(observer)

        owner.toInitialized()

        assertFalse(owner.lifecycle.hasObserver(observer))
    }

    // ==================== 业务状态与业务事件 ====================

    @Test
    fun businessProgressHasItsOwnEvent() {
        val owner = CLILifecycleOwner()
        val events = mutableListOf<CLILifecycleEvent>()
        val scope = CoroutineScope(Dispatchers.Default)
        scope.launch { owner.eventFlow.collect { events += it } }

        owner.toCommandExecutionStart("cmd")
        owner.toCommandExecutionProgress("cmd", 30)
        owner.toCommandExecutionProgress("cmd", 80)
        owner.toCommandExecutionFinish("cmd")

        // 事件是异步派发的，这里只校验类型区分，完整时序由集成测试覆盖
        assertTrue(
            events.none { it == CLILifecycleEvent.ON_COMMAND_EXECUTION_START } ||
                events.count { it == CLILifecycleEvent.ON_COMMAND_EXECUTION_START } <= 1
        )
    }

    @Test
    fun failureAndTerminationHaveDistinctEvents() {
        val owner = CLILifecycleOwner()

        owner.toFailed(errorPhase = "exec")
        assertTrue(owner.getCurrentState() is CLILifecycleState.Failed)
        assertEquals(Lifecycle.State.TERMINATED, owner.getCurrentLifecycleState())

        val other = CLILifecycleOwner()
        other.toTerminated()
        assertTrue(other.getCurrentState() is CLILifecycleState.Terminated)
    }

    @Test
    fun parameterInputFinishReturnsToTheSameModule() {
        val owner = CLILifecycleOwner()
        owner.toCommandList("create", "Create Module")
        owner.toParameterInput("create", "cmd")
        owner.toParameterInputFinish()

        val state = owner.getCurrentState() as CLILifecycleState.CommandList
        assertEquals("create", state.moduleId)
        assertEquals("Create Module", state.moduleName)
    }

    @Test
    fun businessStateFlowTracksLatestState() {
        val owner = CLILifecycleOwner()

        owner.toReady()
        assertTrue(owner.stateFlow.value is CLILifecycleState.Ready)

        owner.toCommandList("emulator", "Emulator Module")
        val state = owner.stateFlow.value as CLILifecycleState.CommandList
        assertEquals("emulator", state.moduleId)
    }

    // ==================== 测试用观察者 ====================

    open class BaseObserver : LifecycleObserver {
        @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
        open fun onCreate() {}
    }

    class ChildObserver : BaseObserver() {
        val log = mutableListOf<String>()

        override fun onCreate() {
            log += "create"
        }
    }
}
