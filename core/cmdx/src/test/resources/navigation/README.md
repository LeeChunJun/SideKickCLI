# 仿 Jetpack Navigation（JVM 实现）

这是一个针对 JVM 平台（如 CLI、Compose Desktop、JavaFX）的简化版 Jetpack Navigation 实现，目标是保留“声明式路由”、“栈式管理”与“页面生命周期”三大核心概念，脱离 Android 平台依赖。

## 目录结构（重要文件）
- simple/CLINavigator.kt — 轻量、基于 StateFlow 的命令行/Compose 演示导航器（推荐学习入口）
- common/NavGraphBuilder.kt — DSL 辅助构造器（createNavController）
- runtime/* — 完整示例：NavController、NavEntry、NavDestination、NavGraph 等核心实现
- test/navigation/CLINavSample.kt — 简易可运行的 Compose/CLI 示例（演示 CLINavigator）
- test/navigation/UseNavSample.kt — 运行时样例（演示 NavController + 生命周期）
- test/navigation/RunSamples.kt — （新增）提供便捷的运行函数：com.rhyme.sidekick.cmdx.navigation.runSimpleSample(), com.rhyme.sidekick.cmdx.navigation.runRuntimeSample(), com.rhyme.sidekick.cmdx.navigation.runRuntimeObserverSample()

## 快速开始

1. 简单实现（推荐从这里开始）

```kotlin
val cli = com.rhyme.sidekick.cmdx.navigation.simple.CLINavigator("home")
cli.navigate("todo")
println(cli.currentEntry.route) // -> todo
cli.popBackStack()
```

在 Compose 中，使用 CLIRoot + CLIRouter 演示（见 test/navigation/CLINavSample.kt）。

2. 完整运行时实现（DSL + 生命周期）

```kotlin
val navController = createNavController {
  destination("home") { entry -> println("HOME") }
  destination("detail") { entry -> println("DETAIL: ${'$'}{entry.arguments["userId"]}") }
}

navController.navigate("home")
navController.navigate("detail", mapOf("userId" to "123"))
navController.popBackStack()
```

要演示生命周期，可以实现 NavLifecycleObserver（见 test/navigation/UseNavSample.kt），在 onCreate/onResume/onDestroy 中处理资源。

## 运行示例
- 在 IDE 中打开项目并运行任意示例文件中的 com.rhyme.sidekick.cmdx.navigation.main 函数（test/navigation/CLINavSample.kt 包含一个 runMosaicBlocking 的 com.rhyme.sidekick.cmdx.navigation.main）。
- 或者在 Kotlin 控制台直接调用新增的便捷函数：
  - com.rhyme.sidekick.cmdx.navigation.runSimpleSample() — 演示 CLINavigator
  - com.rhyme.sidekick.cmdx.navigation.runRuntimeSample() — 演示 NavController 的导航流程
  - com.rhyme.sidekick.cmdx.navigation.runRuntimeObserverSample() — 演示生命周期观察者

## 后续建议
- ViewModel/Store 的作用域管理（在 NavEntry 上挂载）
- Compose NavHost 集成（订阅 NavController.backStack）
- 引入 kotlinx-serialization 支持类型安全的参数传递

---

如需我将 README 翻译为英文、添加更多示例，或把示例改成可通过 Gradle 直接运行的可执行 module，回复“继续”。

以下是实现思路的详细讨论，以及基于 Kotlin 的核心实现。

---

### 一、 核心思路讨论

要在非 Android 环境下复刻这套机制，我们需要剥离对 `android.os.Bundle` 和 `androidx.lifecycle` 的依赖，但保留其“声明式路由”和“栈式管理”的精髓。

#### 1. 核心组件设计
*   **NavDestination (目标)**: 定义“目的地”。它可以是一个 Class、一个 ID 或一个 Route 字符串。
*   **NavEntry (条目)**: 对应 Android 的 `NavBackStackEntry`。它是目的地在 BackStack 中的**实例**，持有参数（Arguments）和唯一的 ID。
*   **BackStack (回退栈)**: 维护一个 `NavEntry` 列表。
*   **NavController (控制器)**: 核心大脑。负责 `navigate`、`pop` 操作，并维护当前的 `backStack` 状态。
*   **NavGraph (导航图)**: 目的地的集合，规定了哪些 Route 是合法的。

#### 2. 参数传递
Android 使用 `Bundle`（Parcelable）。在 JVM 上，我们可以直接使用 `Map<String, Any?>` 或者利用 Kotlin 的 `Type-Safe Routing`（使用插件或反射将 Data Class 序列化为 Route 字符串）。本实现将采用通用的 `Map` 方案。

#### 3. 生命周期管理 (Lifecycle)
这是仿制中最关键的部分。每个 `NavEntry` 应该有自己的状态（CREATED, STARTED, RESUMED, DESTROYED）。当新页面压栈时，旧页面状态下降；当页面出栈时，旧页面状态上升。

#### 4. 状态监听
使用 Kotlin 的 `StateFlow` 或 `SharedFlow` 来暴露当前的 BackStack 给 UI 层（如 Compose Desktop），从而触发界面重绘。

---

### 五、 后续扩展建议

1.  **ViewModel Scoping**: 模仿 `hiltViewModel()` 或 `navGraphViewModels()`。可以给 `NavEntry` 添加一个 `Store` 对象，用于存放该页面关联的 Data 模型，在 `NavEntry` 被 `DESTROYED` 时清理该 `Store`。
2.  **Compose 适配**: 如果你在用 Compose Multiplatform，可以写一个 `NavHost` Composable，订阅 `navController.backStack`，通过 `Crossfade` 或 `AnimatedContent` 切换 `currentEntry.destination.content`。
3.  **Type-Safe Args**: 引入 `kotlinx-serialization`，支持像 `navController.navigate(Profile(id = 1))` 这样的强类型导航。