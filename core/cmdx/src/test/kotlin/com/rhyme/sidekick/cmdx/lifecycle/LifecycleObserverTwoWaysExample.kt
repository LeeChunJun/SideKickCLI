package com.rhyme.sidekick.cmdx.lifecycle

import com.rhyme.sidekick.cmdx.lifecycle.runtime.Lifecycle
import com.rhyme.sidekick.cmdx.lifecycle.runtime.LifecycleEventObserver
import com.rhyme.sidekick.cmdx.lifecycle.runtime.LifecycleObserver
import com.rhyme.sidekick.cmdx.lifecycle.runtime.OnLifecycleEvent


/**
 * 生命周期观察者的两种实现方式示例
 *
 * 展示如何同时使用 @OnLifecycleEvent 注解方式和 LifecycleEventObserver 函数式方式
 * 两种方式都能正确处理生命周期事件
 */
object LifecycleObserverTwoWaysExample {

    /**
     * 方式 1：使用 @OnLifecycleEvent 注解（传统方式）
     */
    class AnnotationBasedObserver : LifecycleObserver {
        @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
        fun onCreate() {
            println("✓ [注解方式] ON_CREATE 事件触发 - 应用已创建")
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
        fun onResume() {
            println("✓ [注解方式] ON_RESUME 事件触发 - 应用已准备好")
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        fun onPause() {
            println("✓ [注解方式] ON_PAUSE 事件触发 - 应用暂时不可交互")
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        fun onDestroy() {
            println("✓ [注解方式] ON_DESTROY 事件触发 - 应用销毁，释放资源")
        }
    }

    /**
     * 方式 2：使用 LifecycleEventObserver 函数式接口（推荐方式）
     * 优点：更灵活，可以响应所有事件在一个地方，更容易写单元测试
     */
    suspend fun createFunctionalObserver(): LifecycleEventObserver {
        return LifecycleEventObserver { source, event ->
            when (event) {
                Lifecycle.Event.ON_CREATE -> {
                    println("✓ [函数式] ON_CREATE 事件触发 - 应用已创建")
                }

                Lifecycle.Event.ON_RESUME -> {
                    println("✓ [函数式] ON_RESUME 事件触发 - 应用已准备好")
                }

                Lifecycle.Event.ON_PAUSE -> {
                    println("✓ [函数式] ON_PAUSE 事件触发 - 应用暂时不可交互")
                }

                Lifecycle.Event.ON_DESTROY -> {
                    println("✓ [函数式] ON_DESTROY 事件触发 - 应用销毁，释放资源")
                }
            }
        }
    }

    /**
     * 方式 3：结合两种方式 - 注解方式处理特定事件，函数式方式处理日志记录
     */
    class HybridObserver : LifecycleObserver {
        @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
        fun onCreate() {
            println("✓ [混合] 注解方式：初始化应用资源")
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        fun onDestroy() {
            println("✓ [混合] 注解方式：清理应用资源")
        }
    }

    suspend fun createLoggingObserver(): LifecycleEventObserver {
        return LifecycleEventObserver { source, event ->
            // 记录所有生命周期事件
            val timestamp = System.currentTimeMillis()
            println("📝 [日志] [$timestamp] 生命周期事件: ${event.name}")
        }
    }

    /**
     * 方式 4：函数式观察者，支持复杂的业务逻辑
     */
    suspend fun createStatefulObserver(): LifecycleEventObserver {
        var eventCount = 0
        val eventLog = mutableListOf<Lifecycle.Event>()

        return LifecycleEventObserver { source, event ->
            eventCount++
            eventLog.add(event)

            println(
                """
                ✓ [有状态函数式]
                  事件序号: $eventCount
                  当前事件: ${event.name}
                  已记录事件总数: ${eventLog.size}
                  事件日志: ${eventLog.map { it.name }.take(3)}...
            """.trimIndent()
            )
        }
    }

    /**
     * 完整的演示 - 同时使用两种方式
     */
    suspend fun demonstrateBothWays() {
        println("╔════════════════════════════════════════════════════════╗")
        println("║  生命周期观察者的两种实现方式演示                      ║")
        println("╚════════════════════════════════════════════════════════╝\n")

        println("示例 1: 使用注解方式")
        println("━".repeat(50))
        val annotationObserver = AnnotationBasedObserver()
        println("观察者类型: ${annotationObserver::class.simpleName}")
        println("观察者实现: LifecycleObserver")
        println("事件处理: 通过 @OnLifecycleEvent 注解标记的方法")
        println("特点: 结构清晰，每个方法负责一个事件\n")

        println("示例 2: 使用函数式方式")
        println("━".repeat(50))
        val functionalObserver = createFunctionalObserver()
        println("观察者类型: LifecycleEventObserver (函数式接口)")
        println("观察者实现: lambda 表达式")
        println("事件处理: 在 onStateChanged 方法中使用 when 表达式")
        println("特点: 灵活，所有事件处理在一个地方\n")

        println("示例 3: 混合方式")
        println("━".repeat(50))
        val hybridObserver = HybridObserver()
        val loggingObserver = createLoggingObserver()
        println("观察者 1 类型: ${hybridObserver::class.simpleName} (注解方式)")
        println("观察者 2 类型: LifecycleEventObserver (函数式方式)")
        println("特点: 结合两种方式的优点，实现关注点分离\n")

        println("示例 4: 有状态函数式方式")
        println("━".repeat(50))
        val statefulObserver = createStatefulObserver()
        println("观察者类型: LifecycleEventObserver with State")
        println("特点: 支持在 lambda 中维护状态和上下文\n")
    }

    /**
     * 方式对比表格
     */
    fun printComparison() {
        println("""

            ╔════════════════════════════════════════════════════════════════════╗
            ║                  @OnLifecycleEvent vs LifecycleEventObserver               ║
            ╠════════════════════════════════════════════════════════════════════╣
            ║ 特性                │ @OnLifecycleEvent    │ LifecycleEventObserver       ║
            ╠═════════════════════╪══════════════════════╪══════════════════════════════╣
            ║ 实现方式            │ 注解 + 类方法        │ 函数式接口                   ║
            ║ 代码风格            │ 命令式               │ 函数式                       ║
            ║ 事件处理分散度      │ 高（多个方法）      │ 低（一个地方）              ║
            ║ 单个事件处理        │ ✓ 容易分离          │ ✓ 容易组织                   ║
            ║ 复杂业务逻辑        │ ✓ 容易扩展          │ ✓ 易于定制                   ║
            ║ 状态维护            │ 需要成员变量        │ ✓ Closure 变量               ║
            ║ 测试难度            │ 中等                │ ✓ 容易（纯函数）            ║
            ║ 类型检查            │ ✓ 强类型检查        │ ✓ 强类型检查                ║
            ║ 反射开销            │ 首次扫描+缓存       │ ✓ 无反射                    ║
            ║ 推荐场景            │ 复杂应用            │ ✓ 快速原型、测试            ║
            ╚════════════════════════════════════════════════════════════════════╝
        """.trimIndent())
    }
}

/**
 * 实际项目中的使用示例
 */
object RealWorldExample {

    /**
     * 示例：应用启动和清理
     */
    fun setupLifecycleObservers(lifecycle: Lifecycle) {

        // 方式 1: 注解方式 - 处理应用级别的初始化/清理
        lifecycle.addObserver(object : LifecycleObserver {
            @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
            fun initialize() {
                println("初始化数据库连接...")
                println("加载配置文件...")
                println("预热缓存...")
            }

            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            fun cleanup() {
                println("关闭数据库连接...")
                println("保存状态...")
                println("释放内存...")
            }
        })

        // 方式 2: 函数式方式 - 处理 UI 状态更新
        lifecycle.addObserver(LifecycleEventObserver { source, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    println("显示主界面...")
                }

                Lifecycle.Event.ON_PAUSE -> {
                    println("隐藏加载动画...")
                }

                else -> {}
            }
        })

        // 方式 3: 函数式方式 - 记录分析数据
        lifecycle.addObserver(LifecycleEventObserver { source, event ->
            // 发送统计事件到分析服务
            println("📊 记录事件到分析服务: ${event.name}")
        })
    }
}
