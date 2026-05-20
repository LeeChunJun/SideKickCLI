# App 生命周期系统 - 架构和设计决策文档

## 执行摘要

本文档记录了新的 App 生命周期系统的架构设计、核心决策以及与现有系统的集成方案。

## 1. 核心架构

### 1.1 整体架构

```
应用层 (Application Layer)
    ↓
状态流 (State Flow)  ← CLILifecycleState
    ↓
事件系统 (Event System)  ← Lifecycle.Event
    ↓
生命周期管理 (Lifecycle Management)
    ├── Lifecycle<S> 接口
    ├── LifecycleRegistry 实现
    └── LifecycleOwner<S> 接口
    ↓
观察者系统 (Observer System)
    ├── LifecycleObserver 接口
    ├── @OnLifecycleEvent 注解
    └── 方法自动发现和调用
```

### 1.2 关键设计原则

1. **泛型支持** - `Lifecycle<S>` 可以与任何状态类型配合
2. **事件驱动** - 状态转换通过事件驱动，而非直接状态赋值
3. **观察者模式** - 完全解耦的事件通知机制
4. **线程安全** - 所有状态操作都用 Mutex 保护
5. **Flow 友好** - 基于 Kotlin Flow 和 StateFlow 的响应式设计

## 2. 文件结构

### 2.1 核心接口

```
lifecycle/
├── Lifecycle.kt
│   ├── Lifecycle<S> 接口
│   ├── Event 枚举 (19 个事件)
│   ├── State 枚举 (4 个状态)
│   └── 观察者管理接口
│
├── LifecycleObserver.kt
│   └── 标记接口
│
├── LifecycleOwner.kt
│   └── 所有者接口
│
└── LifecycleEventAnnotation.kt
    └── @OnLifecycleEvent 注解
```

### 2.2 实现类

```
├── LifecycleRegistry.kt
│   ├── 核心实现
│   ├── 线程安全的状态管理
│   ├── 观察者自动发现
│   └── 事件分发
│
├── CLILifecycleState.kt
│   ├── 13 个具体状态类
│   ├── 状态属性和元数据
│   └── 辅助方法
│
└── CLILifecycleOwner.kt
    ├── CLI 所有者实现
    ├── 便捷的状态转换方法
    └── 状态查询接口
```

### 2.3 工具和示例

```
├── LifecycleExtensions.kt
│   ├── onEvent() - 事件监听扩展
│   ├── onState() - 状态监听扩展
│   ├── observeState() - 状态观察
│   ├── observeEvent() - 事件观察
│   └── 查询工具函数
│
├── CLILifecycleUsageExample.kt
│   ├── 5 个完整示例
│   ├── 错误处理演示
│   ├── 自定义观察者
│   └── Flow 使用示例
│
├── CLILifecycleVerification.kt
│   ├── 单元验证脚本
│   ├── 功能验证
│   └── 集成测试
│
├── MosaicLifecycleIntegration.kt
│   ├── UI 集成示例
│   ├── 应用上下文扩展
│   └── 用户交互模拟
│
├── README.md
│   └── 详细技术文档
│
└── QUICKSTART.md
    └── 快速开始指南
```

## 3. 生命周期事件映射

### 3.1 初始化阶段

```
ON_CREATE ─────→ State.CREATED
├── 应用首次创建
├── 初始化核心服务
└── 加载基础配置
```

### 3.2 启动阶段

```
ON_CONFIG_LOAD_START ────→ State.STARTED
ON_CONFIG_LOAD_FINISH ────→ State.STARTED
ON_RESOURCE_INIT_START ───→ State.STARTED
ON_RESOURCE_INIT_FINISH ──→ State.STARTED
├── 加载配置文件
├── 初始化资源（语言、图标等）
└── 准备 UI 框架
```

### 3.3 激活阶段

```
ON_READY ────────────────→ State.RESUMED
ON_MENU_SELECTION ───────→ State.RESUMED
ON_COMMAND_LIST_SHOW ────→ State.RESUMED
ON_PARAMETER_INPUT ──────→ State.RESUMED
├── 应用完全就绪
├── 接收用户交互
└── 正常运行状态
```

### 3.4 执行阶段

```
ON_COMMAND_EXECUTION_START ─→ State.STARTED
├── 执行命令
├── 显示进度
└── 处理输出
```

### 3.5 显示阶段

```
ON_RESULT_DISPLAY ────────────→ State.RESUMED
ON_ERROR_DISPLAY ─────────────→ State.RESUMED
ON_TIMEOUT_DISPLAY ───────────→ State.RESUMED
ON_ACTION_MENU ────────────────→ State.RESUMED
├── 显示执行结果
├── 提供后续操作
└── 等待用户输入
```

### 3.6 清理阶段

```
ON_PAUSE ────────→ State.STARTED
ON_STOP ─────────→ State.CREATED
ON_DESTROY ──────→ State.DESTROYED
├── 暂停活动
├── 保存状态
├── 释放资源
└── 应用终止
```

## 4. CLI 生命周期状态

### 4.1 状态层次结构

```
CLILifecycleState (sealed)
├── Initialized      - 应用初始化
├── Ready           - 应用就绪
├── MenuSelection   - 主菜单（用户选择模块）
├── CommandList     - 命令列表（选择具体命令）
├── ParameterInput  - 参数输入（配置命令参数）
├── CommandExecution - 命令执行（进行中）
├── ResultDisplay   - 结果显示（成功）
├── ErrorDisplay    - 错误显示（失败）
├── TimeoutState    - 超时（执行超时）
├── ActionMenu      - 操作菜单（后续操作）
├── Cleanup         - 清理资源
├── Terminated      - 应用退出
└── Failed          - 应用错误
```

### 4.2 状态属性

每个状态都包含：

```kotlin
// 必有属性
val timestamp: LocalDateTime  // 状态转换时间
val message: String          // 状态描述

// 可选属性（取决于具体状态）
val moduleId: String         // CommandList
val commandId: String        // ParameterInput
val progress: Int            // CommandExecution
val output: String           // ResultDisplay
val error: String            // ErrorDisplay
val elapsedSeconds: Long     // TimeoutState
```

## 5. 观察者模式实现

### 5.1 观察者注册

```kotlin
val observer = MyObserver()
lifecycleOwner.lifecycle.addObserver(observer)
```

### 5.2 事件处理方法

```kotlin
class MyObserver : LifecycleObserver {
    @OnLifecycleEvent(value = "ON_CREATE")
    fun onCreate() { /* ...*/ }
    
    @OnLifecycleEvent(value = "ON_DESTROY")
    fun onDestroy() { /* ...*/ }
}
```

### 5.3 自动发现机制

1. 注解扫描 - 通过反射查找 `@OnLifecycleEvent` 注解
2. 方法缓存 - 缓存已查找的方法，避免重复反射
3. 自动调用 - 事件触发时自动调用对应方法
4. 异常处理 - 单个观察者异常不影响其他观察者

## 6. 线程安全设计

### 6.1 同步机制

```kotlin
private val mutex = Mutex()

// 异步状态转换
suspend fun handleEvent(event: Lifecycle.Event, newState: S) {
    mutex.withLock {
        // 状态操作
    }
}

// 同步状态转换
fun handleEventSync(event: Lifecycle.Event, newState: S) {
    synchronized(this) {
        // 状态操作
    }
}
```

### 6.2 线程安全保证

- ✅ 状态转换原子性
- ✅ 事件分发顺序性
- ✅ 观察者通知一致性
- ✅ Flow 线程安全

## 7. 与现有系统的集成

### 7.1 与 AppContext 的关系

```kotlin
interface AppContext {
    val config: AppConfig
    val resources: AppResources
    val preferences: AppPreferences
    val coroutineScope: CoroutineScope
    val lifecycleOwner: CLILifecycleOwner
}
```

### 7.2 迁移路径

**阶段 1: 并行存在**
- 新旧系统共存
- 逐步迁移关键业务

**阶段 2: 适配层**
- AppLifecycle 作为适配层
- 新系统作为主体

**阶段 3: 完全迁移**
- 所有代码使用新系统
- 移除旧系统（可选）

### 7.3 互操作性

```kotlin
// 可以同时使用两个系统
val appContext = getAppContext()
appContext.lifecycle.toReady()           // 旧系统
appContext.cliLifecycleOwner.toReady()  // 新系统
```

## 8. 性能考虑

### 8.1 优化策略

1. **方法缓存** - 避免重复的反射查找
2. **Flow 性能** - 使用 SharedFlow 和 StateFlow
3. **观察者管理** - 及时移除未使用的观察者
4. **内存管理** - 及时清理历史状态

### 8.2 基准数据

假设在典型的 CLI 应用中：

- 状态转换：< 1ms
- 观察者通知（10 个观察者）：< 5ms
- 内存占用：< 1MB（包括所有状态历史）

## 9. 扩展性

### 9.1 添加新状态

```kotlin
// 在 CLILifecycleState 中添加
data class MyCustomState(
    override val timestamp: LocalDateTime = LocalDateTime.now(),
    override val message: String = "My state"
) : CLILifecycleState()
```

### 9.2 添加新事件

```kotlin
// 在 Lifecycle.Event 中添加
enum class Event {
    ON_MY_EVENT
}
```

### 9.3 创建自定义所有者

```kotlin
class MyCustomOwner : LifecycleOwner<MyState> {
    override val lifecycle: Lifecycle<MyState> = LifecycleRegistry(/* ...*/)
}
```

## 10. 测试策略

### 10.1 单元测试

```kotlin
fun testStateTransition() {
    val owner = CLILifecycleOwner()
    owner.toInitialized()
    assertEquals(CLILifecycleState.Initialized::class, 
                 owner.getCurrentState()::class)
}
```

### 10.2 集成测试

```kotlin
fun testObserverNotification() {
    val owner = CLILifecycleOwner()
    var eventFired = false
    
    val observer = object : LifecycleObserver {
        @OnLifecycleEvent(value = "ON_READY")
        fun onReady() { eventFired = true }
    }
    
    owner.addObserver(observer)
    owner.toReady()
    assert(eventFired)
}
```

## 11. 常见模式

### 11.1 响应式 UI 更新

```kotlin
lifecycleOwner.observeState(scope) { state ->
    updateUI(state)  // 每当状态变化时更新 UI
}
```

### 11.2 资源管理

```kotlin
val observer = object : LifecycleObserver {
    @OnLifecycleEvent(value = "ON_CREATE")
    fun onCreate() { allocateResources() }
    
    @OnLifecycleEvent(value = "ON_DESTROY")
    fun onDestroy() { releaseResources() }
}
```

### 11.3 条件执行

```kotlin
if (lifecycleOwner.isAlive()) {
    // 只在应用活跃时执行
}
```

## 12. 总结

新的 App 生命周期系统提供了：

✅ **清晰的架构** - 易于理解和维护
✅ **灵活的设计** - 支持各种扩展场景
✅ **完全的控制** - 精细管理应用状态
✅ **丰富的工具** - 大量辅助函数和扩展
✅ **最佳实践** - 遵循 Android 生态标准
✅ **向后兼容** - 与现有系统无缝集成

该系统已准备好用于生产环境，并为 AndroMosaic 应用提供了强大的基础。

---

**创建日期**: 2026-04-24  
**版本**: 1.0  
**状态**: 完成  
