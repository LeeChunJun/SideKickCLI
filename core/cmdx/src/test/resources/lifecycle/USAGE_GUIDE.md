# 🚀 AndroMosaic CLI - App 生命周期系统使用指南

## 📋 目录

1. [系统概览](#系统概览)
2. [快速开始](#快速开始)
3. [核心概念](#核心概念)
4. [完整示例](#完整示例)
5. [文档索引](#文档索引)

---

## 系统概览

### 什么是新的生命周期系统？

这是一个完全仿照 Android `androidx.lifecycle` 设计的应用生命周期管理系统，为 AndroMosaic CLI 提供：

- ✅ **清晰的状态管理** - 从初始化到退出的完整生命周期
- ✅ **事件驱动架构** - 通过事件驱动状态转换
- ✅ **观察者模式** - 多个观察者可以响应状态变化
- ✅ **响应式 API** - 完全支持 Kotlin Flow 和协程
- ✅ **线程安全** - 所有操作都是线程安全的

### 为什么需要这个系统？

现有的 `AppLifecycle` 是简化的实现，无法处理复杂的 UI 状态转换。新系统提供了：

- 一个清晰的、易于扩展的架构
- 与 Android 生态一致的设计模式
- 完整的类型安全和错误处理
- 丰富的工具函数和扩展

---

## 快速开始

### 1. 创建生命周期所有者

```kotlin
val lifecycleOwner = CLILifecycleOwner()
```

### 2. 转换状态

```kotlin
// 初始化阶段
lifecycleOwner.toInitialized()
lifecycleOwner.toReady()

// UI 交互阶段
lifecycleOwner.toMenuSelection()
lifecycleOwner.toCommandList("create", "创建项目")
lifecycleOwner.toParameterInput("create", "create 命令")

// 执行阶段
lifecycleOwner.toCommandExecutionStart("android create --output=/path")
lifecycleOwner.toCommandExecutionProgress("android create --output=/path", 50)
lifecycleOwner.toCommandExecutionFinish("android create --output=/path")

// 结果显示
lifecycleOwner.toResultDisplay("create", "项目创建成功！")

// 清理和退出
lifecycleOwner.toTerminated()
```

### 3. 创建观察者

```kotlin
class MyObserver : LifecycleObserver {
    @OnLifecycleEvent(value = "ON_CREATE")
    fun onCreate() {
        println("应用已创建，初始化资源...")
    }
    
    @OnLifecycleEvent(value = "ON_READY")
    fun onReady() {
        println("应用已准备好，显示主菜单")
    }
    
    @OnLifecycleEvent(value = "ON_DESTROY")
    fun onDestroy() {
        println("应用销毁，释放资源")
    }
}

lifecycleOwner.addObserver(MyObserver())
```

### 4. 监听状态变化

```kotlin
val scope = CoroutineScope(Dispatchers.Default)

lifecycleOwner.observeState(scope) { state ->
    println("状态: ${state::class.simpleName}")
    println("消息: ${state.message}")
    
    when (state) {
        is CLILifecycleState.MenuSelection -> {
            // 显示菜单
        }
        is CLILifecycleState.CommandExecution -> {
            // 显示执行进度
            println("进度: ${state.progress}%")
        }
        is CLILifecycleState.ResultDisplay -> {
            // 显示结果
            println("输出: ${state.output}")
        }
        else -> {
            // 其他状态
        }
    }
}
```

---

## 核心概念

### Lifecycle.Event (事件)

事件代表生命周期的转换点。每个事件都会产生一个状态变化。

| 事件 | 说明 |
|------|------|
| `ON_CREATE` | 应用创建 |
| `ON_READY` | 应用就绪 |
| `ON_MENU_SELECTION` | 用户选择菜单 |
| `ON_COMMAND_LIST_SHOW` | 显示命令列表 |
| `ON_PARAMETER_INPUT` | 用户输入参数 |
| `ON_COMMAND_EXECUTION_START` | 命令开始执行 |
| `ON_RESULT_DISPLAY` | 显示执行结果 |
| `ON_ERROR_DISPLAY` | 显示错误信息 |
| `ON_TIMEOUT_DISPLAY` | 显示超时 |
| `ON_ACTION_MENU` | 显示操作菜单 |
| `ON_DESTROY` | 应用销毁 |

### Lifecycle.State (生命周期状态)

生命周期状态代表应用所处的阶段。

```
┌──────────────┐
│ DESTROYED    │  (已销毁)
└──────┬───────┘
       ▲
       │
       ▼
┌──────────────┐
│ CREATED      │  (已创建)
└──────┬───────┘
       ▲
       │
       ▼
┌──────────────┐
│ STARTED      │  (已启动)
└──────┬───────┘
       ▲
       │
       ▼
┌──────────────┐
│ RESUMED      │  (已激活)
└──────────────┘
```

### CLILifecycleState (UI 状态)

13 个 UI 状态，对应设计文档中的各个页面：

- `Initialized` - 初始化
- `Ready` - 准备好
- `MenuSelection` - 菜单选择
- `CommandList` - 命令列表
- `ParameterInput` - 参数输入
- `CommandExecution` - 命令执行
- `ResultDisplay` - 成功结果
- `ErrorDisplay` - 错误信息
- `TimeoutState` - 超时
- `ActionMenu` - 操作菜单
- `Cleanup` - 清理资源
- `Terminated` - 应用退出
- `Failed` - 应用错误

---

## 完整示例

### 示例 1: 完整的命令执行流程

```kotlin
suspend fun executeCommand(
    lifecycleOwner: CLILifecycleOwner,
    command: String
) {
    // 1. 开始执行
    lifecycleOwner.toCommandExecutionStart(command)
    
    try {
        // 2. 模拟执行过程，更新进度
        for (progress in listOf(25, 50, 75, 100)) {
            kotlinx.coroutines.delay(500)
            lifecycleOwner.toCommandExecutionProgress(command, progress)
        }
        
        // 3. 执行完成
        lifecycleOwner.toCommandExecutionFinish(command)
        
        // 4. 显示结果
        val output = "Project successfully created!\nLocation: /path/to/project"
        lifecycleOwner.toResultDisplay("create", output)
        
    } catch (e: Exception) {
        // 错误处理
        lifecycleOwner.toErrorDisplay(
            "create",
            e.message ?: "Unknown error",
            "ERR_EXEC",
            "请检查参数后重试",
            e.stackTraceToString()
        )
    }
    
    // 5. 操作菜单
    lifecycleOwner.toActionMenu("create")
}
```

### 示例 2: 响应式 UI 更新

```kotlin
@Composable
fun AppUI(lifecycleOwner: CLILifecycleOwner) {
    val state by lifecycleOwner.lifecycle.stateFlow.collectAsState()
    
    Box {
        when (state) {
            is CLILifecycleState.MenuSelection -> {
                MenuSelectionUI()
            }
            is CLILifecycleState.CommandList -> {
                val commandListState = state as CLILifecycleState.CommandList
                CommandListUI(commandListState.moduleName)
            }
            is CLILifecycleState.CommandExecution -> {
                val execState = state as CLILifecycleState.CommandExecution
                CommandExecutionUI(execState.progress)
            }
            is CLILifecycleState.ResultDisplay -> {
                val resultState = state as CLILifecycleState.ResultDisplay
                ResultDisplayUI(resultState.output)
            }
            else -> {
                Text("Loading...")
            }
        }
    }
}
```

### 示例 3: 自定义观察者

```kotlin
class AnalyticsObserver : LifecycleObserver {
    private val events = mutableListOf<String>()
    
    @OnLifecycleEvent(value = "ON_MENU_SELECTION")
    fun onMenuSelection() {
        events.add("menu_selection_${System.currentTimeMillis()}")
    }
    
    @OnLifecycleEvent(value = "ON_COMMAND_EXECUTION_START")
    fun onCommandStart() {
        events.add("command_start_${System.currentTimeMillis()}")
    }
    
    @OnLifecycleEvent(value = "ON_DESTROY")
    fun onDestroy() {
        // 上报所有事件
        reportAnalytics(events)
    }
}

lifecycleOwner.addObserver(AnalyticsObserver())
```

---

## 文档索引

### 📘 详细文档

1. **README.md** - 完整的技术文档
   - 架构概览
   - 所有接口和类的详细说明
   - 高级用法和最佳实践

2. **QUICKSTART.md** - 快速开始指南
   - 基础使用
   - 常见问题
   - 文件结构

3. **ARCHITECTURE.md** - 架构和设计决策
   - 整体架构
   - 设计原理
   - 线程安全设计
   - 性能考虑

4. **COMPLETION_SUMMARY.md** - 项目完成总结
   - 所有成就总结
   - 文件清单
   - 项目统计

### 📝 代码示例

1. **CLILifecycleUsageExample.kt** - 5 个完整示例
   - 基本生命周期流程
   - 错误处理流程
   - 超时处理流程
   - 自定义观察者
   - Flow 观察

2. **MosaicLifecycleIntegration.kt** - Mosaic UI 集成
   - UI 状态管理器
   - 响应式 UI 示例
   - 用户交互模拟

3. **CLILifecycleVerification.kt** - 验证脚本
   - 功能验证
   - 单元测试
   - 集成测试

### 🔗 相关文件位置

```
D:\AppsData\IdeaProjects\SideKickCLI\
  core\cmdx\src\com.rhyme.sidekick.cmdx.navigation.main\kotlin\
    com\rhyme\sidekick\cmdx\
        lifecycle\
          ├── Lifecycle.kt                    # 核心接口
          ├── LifecycleObserver.kt           # 观察者接口
          ├── LifecycleOwner.kt              # 所有者接口
          ├── LifecycleEventAnnotation.kt    # 事件注解
          ├── LifecycleRegistry.kt           # 实现类
          ├── CLILifecycleState.kt           # UI 状态
          ├── CLILifecycleOwner.kt           # CLI 所有者
          ├── LifecycleExtensions.kt         # 扩展函数
          ├── CLILifecycleUsageExample.kt    # 使用示例
          ├── CLILifecycleVerification.kt    # 验证脚本
          ├── MosaicLifecycleIntegration.kt  # UI 集成
          ├── README.md                       # 技术文档
          ├── QUICKSTART.md                   # 快速指南
          ├── ARCHITECTURE.md                 # 架构文档
          └── COMPLETION_SUMMARY.md           # 完成总结
```

---

## 常见任务

### 任务 1: 显示主菜单并等待用户选择

```kotlin
lifecycleOwner.toMenuSelection()

lifecycleOwner.observeState(scope) { state ->
    if (state is CLILifecycleState.MenuSelection) {
        // 显示菜单，等待用户输入
        val selectedModule = getUserInput()
        lifecycleOwner.toCommandList(selectedModule, selectedModule)
    }
}
```

### 任务 2: 执行命令并显示进度

```kotlin
lifecycleOwner.toCommandExecutionStart(command)

val process = ProcessBuilder(command.split(" ")).start()
var progress = 0

while (process.isAlive) {
    progress = (progress + 25).coerceAtMost(100)
    lifecycleOwner.toCommandExecutionProgress(command, progress)
    Thread.sleep(500)
}

lifecycleOwner.toCommandExecutionFinish(command)
```

### 任务 3: 处理执行错误

```kotlin
try {
    executeCommand()
} catch (e: Exception) {
    lifecycleOwner.toErrorDisplay(
        commandId = "create",
        error = e.message ?: "Unknown error",
        errorCode = "ERR_${e::class.simpleName}",
        suggestion = "请检查参数和权限，然后重试",
        stackTrace = e.stackTraceToString()
    )
}
```

### 任务 4: 实现自动重试超时的命令

```kotlin
val observer = object : LifecycleObserver {
    @OnLifecycleEvent(value = "ON_TIMEOUT_DISPLAY")
    fun onTimeout() {
        // 自动重试
        lifecycleOwner.toCommandExecutionStart(lastCommand)
    }
}

lifecycleOwner.addObserver(observer)
```

---

## 下一步

1. **阅读文档** - 从 `QUICKSTART.md` 开始
2. **运行示例** - 执行 `CLILifecycleUsageExample.kt`
3. **集成到应用** - 在现有的 Mosaic UI 中使用
4. **观察和优化** - 根据实际使用情况改进

---

## 获取帮助

- 📖 查看详细文档：`README.md`
- 🚀 快速开始：`QUICKSTART.md`
- 🏗️ 架构设计：`ARCHITECTURE.md`
- 💡 完整示例：`CLILifecycleUsageExample.kt`
- ✓ 验证功能：`CLILifecycleVerification.kt`

---

**最后更新**: 2026-04-24  
**版本**: 1.0  
**状态**: ✅ 完成
