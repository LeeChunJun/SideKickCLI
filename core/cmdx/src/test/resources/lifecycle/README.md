# App 生命周期系统 (Lifecycle System) 完整实现指南

## 概述

本文档介绍新的 App 生命周期系统实现，该系统遵循 Android `androidx.lifecycle` 的设计原理和思想，为 AndroMosaic CLI 应用提供完整的生命周期管理能力。

## 架构概览

```
┌─────────────────────────────────────────────────────────┐
│              Lifecycle System Architecture              │
├─────────────────────────────────────────────────────────┤
│                                                         │
│  ┌──────────────────────────────────────────────┐     │
│  │       LifecycleOwner<CLILifecycleState>     │     │
│  │  (拥有生命周期的对象，如 Activity、Service) │     │
│  └────────────────────┬─────────────────────────┘     │
│                       │                                 │
│                       │ owns                            │
│                       ▼                                 │
│  ┌──────────────────────────────────────────────┐     │
│  │   Lifecycle<CLILifecycleState>               │     │
│  │  (观察者模式核心，定义事件和状态)           │     │
│  └────────────────────┬─────────────────────────┘     │
│                       │                                 │
│       ┌───────────────┼───────────────┐               │
│       │               │               │               │
│    implements      emits           manages            │
│       │               │               │               │
│       ▼               ▼               ▼               │
│  ┌────────────┐ ┌──────────┐  ┌──────────────┐       │
│  │ Lifecycle  │ │  Event   │  │ LifecycleReg │       │
│  │  Registry  │ │   Flow   │  │ istry Impl   │       │
│  └────────────┘ └──────────┘  └──────────────┘       │
│       │                           │                    │
│       └────────────┬──────────────┘                   │
│                    │                                   │
│              notifies observers                        │
│                    │                                   │
│                    ▼                                   │
│  ┌──────────────────────────────────────────────┐    │
│  │    LifecycleObserver                         │    │
│  │  (实现 @OnLifecycleEvent 的观察者类)        │    │
│  └──────────────────────────────────────────────┘    │
│                                                       │
└─────────────────────────────────────────────────────────┘
```

## 核心组件

### 1. Lifecycle 接口
**位置**: `Lifecycle.kt`

定义了生命周期的抽象接口，包含：

- **Event 枚举**: 生命周期事件（ON_CREATE, ON_READY, ON_MENU_SELECTION 等）
- **State 枚举**: 生命周期状态（DESTROYED, CREATED, STARTED, RESUMED）
- **StateFlow**: 用于观察 UI 状态变化
- **EventFlow**: 用于观察生命周期事件触发

```kotlin
interface Lifecycle<S : Any> {
    enum class Event { ON_CREATE, ON_READY, /* ...*/ }
    enum class State { DESTROYED, CREATED, STARTED, RESUMED }
    
    val currentState: State
    val stateFlow: StateFlow<S>
    val eventFlow: Flow<Event>
    
    fun addObserver(observer: LifecycleObserver)
    fun removeObserver(observer: LifecycleObserver)
}
```

### 2. LifecycleObserver 接口
**位置**: `LifecycleObserver.kt`

标记接口，用于标识生命周期观察者。

```kotlin
interface LifecycleObserver
```

### 3. @OnLifecycleEvent 注解
**位置**: `LifecycleEventAnnotation.kt`

用于标记方法，使其在特定生命周期事件触发时自动调用。

```kotlin
@OnLifecycleEvent(value = "ON_CREATE")
fun onCreate() {
    // 在 ON_CREATE 事件触发时调用
}
```

### 4. LifecycleOwner 接口
**位置**: `LifecycleOwner.kt`

表示拥有生命周期的对象。任何需要生命周期管理的类都应该实现此接口。

```kotlin
interface LifecycleOwner<S : Any> {
    val lifecycle: Lifecycle<S>
}
```

### 5. LifecycleRegistry 实现
**位置**: `LifecycleRegistry.kt`

`Lifecycle` 的具体实现，提供：

- 观察者管理（注册/注销）
- 状态转换处理
- 事件分发
- 注解方法自动调用
- 线程安全的状态管理

```kotlin
class LifecycleRegistry<S : Any>(
    initialState: S,
    initialLifecycleState: Lifecycle.State = Lifecycle.State.CREATED
) : Lifecycle<S> { /* ...*/ }
```

### 6. CLILifecycleState 密封类
**位置**: `CLILifecycleState.kt`

定义了 CLI 特定的 UI 状态，包括：

- `Initialized` - 应用初始化
- `Ready` - 应用就绪
- `MenuSelection` - 主菜单选择
- `CommandList` - 命令列表显示
- `ParameterInput` - 参数输入
- `CommandExecution` - 命令执行中
- `ResultDisplay` - 结果显示
- `ErrorDisplay` - 错误显示
- `TimeoutState` - 超时状态
- `ActionMenu` - 操作菜单
- `Cleanup` - 清理资源
- `Terminated` - 应用终止
- `Failed` - 应用失败

### 7. CLILifecycleOwner 类
**位置**: `CLILifecycleOwner.kt`

CLI 应用的生命周期所有者，实现 `LifecycleOwner<CLILifecycleState>` 接口。

提供便捷的状态转换方法：

```kotlin
val lifecycleOwner = CLILifecycleOwner()

lifecycleOwner.toInitialized()
lifecycleOwner.toReady()
lifecycleOwner.toMenuSelection()
lifecycleOwner.toCommandList("create", "Create Module")
lifecycleOwner.toParameterInput("create", "create command")
lifecycleOwner.toCommandExecutionStart("android create --output=/path")
lifecycleOwner.toResultDisplay("create", "Project created successfully")
lifecycleOwner.toTerminated()
```

## 生命周期事件和状态映射

| 事件 | 目标状态 | 说明 |
|-----|--------|------|
| ON_CREATE | CREATED | 应用创建 |
| ON_CONFIG_LOAD_START | STARTED | 配置加载开始 |
| ON_CONFIG_LOAD_FINISH | STARTED | 配置加载完成 |
| ON_RESOURCE_INIT_START | STARTED | 资源初始化开始 |
| ON_RESOURCE_INIT_FINISH | STARTED | 资源初始化完成 |
| ON_READY | RESUMED | 应用已准备好 |
| ON_MENU_SELECTION | RESUMED | 菜单选择 |
| ON_COMMAND_LIST_SHOW | RESUMED | 命令列表显示 |
| ON_PARAMETER_INPUT | RESUMED | 参数输入 |
| ON_COMMAND_EXECUTION_START | STARTED | 命令执行开始 |
| ON_COMMAND_EXECUTION_FINISH | RESUMED | 命令执行完成 |
| ON_RESULT_DISPLAY | RESUMED | 结果显示 |
| ON_ERROR_DISPLAY | RESUMED | 错误显示 |
| ON_TIMEOUT_DISPLAY | RESUMED | 超时显示 |
| ON_ACTION_MENU | RESUMED | 操作菜单 |
| ON_PAUSE | STARTED | 应用暂停 |
| ON_STOP | CREATED | 应用停止 |
| ON_DESTROY | DESTROYED | 应用销毁 |

## 使用示例

### 基础使用

```kotlin
// 创建生命周期所有者
val lifecycleOwner = CLILifecycleOwner()

// 状态转换
lifecycleOwner.toInitialized()
lifecycleOwner.toReady()
lifecycleOwner.toMenuSelection()
lifecycleOwner.toTerminated()

// 查询当前状态
val currentState = lifecycleOwner.getCurrentState()
val isResumed = lifecycleOwner.isResumed()
```

### 创建观察者

```kotlin
class MyObserver : LifecycleObserver {
    @OnLifecycleEvent(value = "ON_CREATE")
    fun onCreate() {
        println("应用已创建")
    }
    
    @OnLifecycleEvent(value = "ON_READY")
    fun onReady() {
        println("应用已准备好")
    }
    
    @OnLifecycleEvent(value = "ON_DESTROY")
    fun onDestroy() {
        println("应用销毁，释放资源")
    }
}

// 注册观察者
val observer = MyObserver()
lifecycleOwner.addObserver(observer)
```

### 监听状态变化

```kotlin
val coroutineScope = CoroutineScope(Dispatchers.Default)

// 监听所有状态变化
lifecycleOwner.observeState(coroutineScope) { state ->
    println("状态: ${state::class.simpleName} - ${state.message}")
}

// 监听所有事件
lifecycleOwner.observeEvents(coroutineScope) { event ->
    println("事件: ${event.name}")
}

// 监听特定事件
lifecycleOwner.observeEvent(coroutineScope, Lifecycle.Event.ON_READY) {
    println("应用已就绪!")
}
```

### 集成到现有代码

```kotlin
// 在 Mosaic UI 中使用
@Composable
fun AppUI(lifecycleOwner: CLILifecycleOwner) {
    val state = lifecycleOwner.lifecycle.stateFlow.collectAsState()
    
    when (val currentState = state.value) {
        is CLILifecycleState.MenuSelection -> {
            // 显示菜单
        }
        is CLILifecycleState.CommandExecution -> {
            // 显示执行进度
        }
        is CLILifecycleState.ResultDisplay -> {
            // 显示结果
        }
        else -> {
            // 其他状态
        }
    }
}
```

## 高级用法

### 状态转换的完整流程

```kotlin
suspend fun executeCommand(
    lifecycleOwner: CLILifecycleOwner,
    command: String,
    output: String
) {
    // 1. 开始执行
    lifecycleOwner.toCommandExecutionStart(command)
    
    // 2. 更新进度
    for (progress in 1..100 step 25) {
        delay(500)
        lifecycleOwner.toCommandExecutionProgress(command, progress)
    }
    
    // 3. 完成执行
    lifecycleOwner.toCommandExecutionFinish(command)
    
    // 4. 显示结果
    lifecycleOwner.toResultDisplay(
        "command-id",
        output,
        CLILifecycleState.ResultDisplay.OutputType.TEXT
    )
    
    // 5. 操作菜单
    lifecycleOwner.toActionMenu("command-id")
}
```

### 错误处理

```kotlin
try {
    // 执行命令
    executeCommand()
} catch (e: Exception) {
    lifecycleOwner.toErrorDisplay(
        commandId = "command-id",
        error = e.message ?: "Unknown error",
        errorCode = "ERR_EXECUTION",
        suggestion = "Please check the parameters and try again",
        stackTrace = e.stackTraceToString()
    )
}
```

### 自定义观察者工具类

```kotlin
class LoggingObserver : LifecycleObserver {
    @OnLifecycleEvent(value = "ON_CREATE")
    fun logCreate() {
        log("APP_CREATE")
    }
    
    @OnLifecycleEvent(value = "ON_DESTROY")
    fun logDestroy() {
        log("APP_DESTROY")
    }
    
    private fun log(event: String) {
        // 记录生命周期事件
    }
}
```

## 与现有系统的集成

### AppContext 集成

```kotlin
interface AppContext {
    val config: AppConfig
    val resources: AppResources
    val preferences: AppPreferences
    val coroutineScope: CoroutineScope
    val lifecycle: AppLifecycle  // 旧的
    val cliLifecycleOwner: CLILifecycleOwner  // 新的
}
```

### 向后兼容

现有的 `AppLifecycle` 保留为向后兼容层，新代码应该使用 `CLILifecycleOwner`。

## 设计原理

### 1. 观察者模式
生命周期系统使用观察者模式，允许多个观察者在生命周期事件发生时收到通知。

### 2. 事件驱动
状态转换通过事件驱动，每个事件都会产生一个状态变化。

### 3. 泛型支持
`Lifecycle<S>` 和 `LifecycleOwner<S>` 支持任意状态类型，不仅限于 `CLILifecycleState`。

### 4. 线程安全
所有状态转换都通过 `Mutex` 保护，确保线程安全。

### 5. Flow 友好
使用 Kotlin Flow 和 StateFlow，与协程生态无缝集成。

## 文件清单

```
lifecycle/
├── Lifecycle.kt                      # 核心接口
├── LifecycleObserver.kt             # 观察者接口
├── LifecycleEventAnnotation.kt      # 事件注解
├── LifecycleOwner.kt                # 所有者接口
├── LifecycleRegistry.kt             # 注册表实现
├── CLILifecycleState.kt             # CLI 状态定义
├── CLILifecycleOwner.kt             # CLI 所有者
├── LifecycleExtensions.kt           # 扩展函数
└── CLILifecycleUsageExample.kt      # 使用示例
```

## 总结

新的生命周期系统提供了一个强大、灵活且易于使用的方式来管理 CLI 应用的生命周期。通过遵循 Android `androidx.lifecycle` 的设计原理，我们实现了：

- ✅ 清晰的生命周期状态管理
- ✅ 事件驱动的架构
- ✅ 观察者模式的优雅实现
- ✅ 注解驱动的方法调用
- ✅ 完整的线程安全保证
- ✅ Kotlin Flow 友好的 API
- ✅ 与现有代码的向后兼容

这使得 AndroMosaic 应用能够以一种清晰、可维护和易扩展的方式管理其各种状态转换。
