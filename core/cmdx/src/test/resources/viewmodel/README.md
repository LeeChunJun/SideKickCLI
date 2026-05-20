# ViewModel 模块（TDD）文档

此文档说明 viewmodel 子模块的设计与在 Compose 中如何使用本库提供的 cliViewModel。

## 概览

- core: 核心类型（ViewModel / ViewModelStore / ViewModelProvider）。
- viewmodel: JVM/Compose 端的扩展，例如 ViewModelStoreOwner、viewModels 委托、以及 Compose 专用的 LocalCLIViewModelStoreOwner 与 cliViewModel。
- tdd: 演示与测试样例（包含非 Compose 的 MyWindowSample、UserViewModel，以及下文添加的 Compose 使用示例）。

## 在 Compose 中使用 cliViewModel

库提供了一个组合上下文（CompositionLocal）用于在 Compose 层级中提供一个 CLIViewModelStoreOwner：

- LocalCLIViewModelStoreOwner：组合本地（CompositionLocal）访问点。
- CLIViewModelStoreOwner：实际保存 ViewModelStore 的容器。
- cliViewModel<T>(key, scopeFactory)：一个 @Composable 的便捷函数，可在 Compose 中按 key 获取或创建指定类型的 ViewModel（基于本库的 ViewModelProvider）。

下面给出示例代码（见 ComposeSample.kt）：

1. 在应用根部创建并提供一个 CLIViewModelStoreOwner（例如 CLIRoot）。
2. 在任意子 Composable 中直接调用 cliViewModel 获取 ViewModel。

注意：示例基于 kotlinx.coroutines.flow 来读取 ViewModel 中的状态。确保在真实项目中引入相应的 Compose 与 Coroutines 依赖。

---

示例关键点：

- 使用 `LocalCLIViewModelStoreOwner provides owner` 在根节点注入 store owner。
- 使用 `val vm: UserViewModel = cliViewModel("user") { ViewModelStore() }` 在 Composable 中获取 ViewModel 实例。
- 使用 `collectAsState(initial = ...)` 将 StateFlow 转为 Compose 状态以驱动 UI。

更多参考请查看 tdd/ComposeSample.kt。
