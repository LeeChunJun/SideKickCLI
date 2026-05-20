# AndroMosaic CLI 生命周期系统

## 快速开始

### 创建生命周期所有者
```kotlin
val lifecycleOwner = CLILifecycleOwner()
```

### 转换状态
```kotlin
lifecycleOwner.toInitialized()          // 初始化
lifecycleOwner.toReady()                // 准备好
lifecycleOwner.toMenuSelection()        // 显示菜单
lifecycleOwner.toCommandList("create", "Create Module")  // 命令列表
lifecycleOwner.toParameterInput("create", "create")      // 参数输入
lifecycleOwner.toCommandExecutionStart("command")        // 开始执行
lifecycleOwner.toResultDisplay("id", "output")           // 显示结果
lifecycleOwner.toTerminated()           // 退出
```

### 创建观察者
```kotlin
class MyObserver : LifecycleObserver {
    @OnLifecycleEvent(value = "ON_CREATE")
    fun onCreate() {
        println("应用已创建")
    }
    
    @OnLifecycleEvent(value = "ON_DESTROY")
    fun onDestroy() {
        println("应用销毁")
    }
}

lifecycleOwner.addObserver(MyObserver())
```

### 监听状态变化
```kotlin
val coroutineScope = CoroutineScope(Dispatchers.Default)
lifecycleOwner.observeState(coroutineScope) { state ->
    println("状态: ${state::class.simpleName}")
}
```

## 文件结构

```
lifecycle/
├── Lifecycle.kt                    # 核心接口定义
├── LifecycleObserver.kt           # 观察者接口
├── LifecycleOwner.kt              # 所有者接口
├── LifecycleEventAnnotation.kt    # @OnLifecycleEvent 注解
├── LifecycleRegistry.kt           # 生命周期实现
├── CLILifecycleState.kt           # CLI 状态定义
├── CLILifecycleOwner.kt           # CLI 生命周期所有者
├── LifecycleExtensions.kt         # 扩展函数
├── CLILifecycleUsageExample.kt    # 完整使用示例
├── CLILifecycleVerification.kt    # 验证脚本
└── README.md                       # 详细文档
```

## 核心概念

### Lifecycle.Event
生命周期事件，代表状态转换的触发点：
- ON_CREATE - 应用创建
- ON_READY - 应用就绪
- ON_MENU_SELECTION - 菜单选择
- ON_COMMAND_EXECUTION_START - 命令开始执行
- ON_DESTROY - 应用销毁
- 等等...

### Lifecycle.State
生命周期状态，代表应用所处的状态阶段：
- DESTROYED - 已销毁
- CREATED - 已创建
- STARTED - 已启动
- RESUMED - 已激活

### CLILifecycleState
CLI 特定的 UI 状态：
- Initialized - 初始化
- MenuSelection - 菜单选择
- CommandList - 命令列表
- CommandExecution - 命令执行
- ResultDisplay - 结果显示
- ErrorDisplay - 错误显示
- TimeoutState - 超时
- ActionMenu - 操作菜单
- 等等...

## 设计原理

该实现遵循 Android `androidx.lifecycle` 的设计原理：

1. **观察者模式** - 使用观察者模式实现事件通知
2. **事件驱动** - 通过事件驱动状态转换
3. **泛型支持** - 支持任意类型的生命周期状态
4. **线程安全** - 使用 Mutex 保证线程安全
5. **Flow 友好** - 使用 Kotlin Flow 和 StateFlow
6. **注解驱动** - 使用 @OnLifecycleEvent 注解

## 完整流程示例

```kotlin
suspend fun main() {
    val lifecycleOwner = CLILifecycleOwner()
    
    // 添加观察者
    lifecycleOwner.addObserver(object : LifecycleObserver {
        @OnLifecycleEvent(value = "ON_CREATE")
        fun onCreate() {
            println("✓ App created")
        }
        
        @OnLifecycleEvent(value = "ON_DESTROY")
        fun onDestroy() {
            println("✓ App destroyed")
        }
    })
    
    // 执行生命周期流程
    lifecycleOwner.toInitialized()
    lifecycleOwner.toReady()
    lifecycleOwner.toMenuSelection()
    lifecycleOwner.toCommandList("create", "Create Module")
    lifecycleOwner.toParameterInput("create", "create command")
    lifecycleOwner.toCommandExecutionStart("command")
    lifecycleOwner.toResultDisplay("id", "Success!")
    lifecycleOwner.toTerminated()
}
```

## 与 AppLifecycle 的关系

新的生命周期系统与现有的 `AppLifecycle` 可以并行存在：
- `AppLifecycle` 用于应用级别的初始化和清理
- `CLILifecycleOwner` 用于 UI 状态管理和用户交互

两者可以相互补充，不需要立即替换。

## 常见问题

### Q: 如何处理异步操作？
```kotlin
coroutineScope.launch {
    lifecycleOwner.toCommandExecutionStart("command")
    try {
        val result = executeCommandAsync()
        lifecycleOwner.toResultDisplay("id", result)
    } catch (e: Exception) {
        lifecycleOwner.toErrorDisplay("id", e.message ?: "Error")
    }
}
```

### Q: 如何确保观察者方法被调用？
确保：
1. 观察者类实现 `LifecycleObserver` 接口
2. 方法标注 `@OnLifecycleEvent(value = "EVENT_NAME")`
3. 事件名称与 `Lifecycle.Event` 枚举名称完全匹配

### Q: 支持多个观察者吗？
是的，可以添加多个观察者。每个事件都会通知所有已注册的观察者：
```kotlin
lifecycleOwner.addObserver(observer1)
lifecycleOwner.addObserver(observer2)
lifecycleOwner.addObserver(observer3)
```

## 下一步

查看详细文档：`README.md`

查看完整示例：`CLILifecycleUsageExample.kt`

运行验证脚本：`CLILifecycleVerification.kt`
