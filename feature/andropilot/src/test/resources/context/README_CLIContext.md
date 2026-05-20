# CLIContext 服务获取和注入功能说明

## 概述

CLIContext 是 SideKickCLI 框架的核心组件，提供了统一的上下文管理和依赖注入功能。它通过 `CLIServiceManager` 实现了服务管理功能。

核心接口：
- **CLIContext** - 定义应用上下文的接口，包含配置、资源、数据库、导航器等核心组件
- **CLIServiceManager** - 服务管理器，负责服务的注册、获取和懒加载

核心实现：
- **CLIContextImpl** - CLIContext 的默认实现，提供完整的服务管理功能

核心功能：
- `getService(clazz: KClass<T>): T` - 立即获取服务实例
- `inject(clazz: KClass<T>): Lazy<T>` - 懒加载注入服务实例
- `registerService()` - 注册服务实例
- `registerServiceFactory()` - 注册服务工厂（懒加载创建）

## 实现方式

### CLIContextImpl（标准实现）

位置：`core/cmdx/src/main/kotlin/com/rhyme/sidekick/cmdx/framework/CLIContextImpl.kt`

特点：
- 实现 CLIContext 接口，提供完整的上下文功能
- 内置 CLIServiceManager 用于服务管理
- 支持服务实例注册（`registerService`）
- 支持服务工厂注册（`registerServiceFactory`，懒加载创建）
- 提供类型安全的 reified 方法
- 适合所有应用场景

## 使用示例

### CLIContextImpl 基本用法

```kotlin
// 创建上下文
val context = CLIContextImpl(
    config = config,
    resources = resources,
    preferences = preferences,
    database = database,
    navigator = navigator,
    lifecycleOwner = lifecycleOwner,
    viewModelStoreOwner = viewModelStoreOwner,
    coroutineScope = coroutineScope
)

// 方式1：注册服务实例（推荐 reified 版本）
context.registerService<MyService>(MyServiceImpl())

// 方式2：注册服务工厂（懒加载）
context.registerServiceFactory<DatabaseService> { 
    DatabaseServiceImpl(config.databaseUrl) 
}

// 立即获取服务（推荐 reified 版本）
val service = context.getService<MyService>()
service.doSomething()

// 懒加载注入（推荐 reified 版本）
val lazyService by context.inject<DatabaseService>()
// 只有在第一次访问时才会创建实例
lazyService.query()
```

## API 说明

### CLIContextImpl API

#### 注册服务

```kotlin
// 使用 reified 类型参数（推荐）
context.registerService<MyService>(MyServiceImpl())

// 或者显式传递 KClass
context.registerService(MyService::class, MyServiceImpl())
```

## 最佳实践

1. **优先使用 reified 版本**：
   - 注册服务：`context.registerService<MyService>(instance)`
   - 获取服务：`context.getService<MyService>()`
   - 注入服务：`val service by context.inject<MyService>()`
   - 避免手动传递 KClass 参数，减少代码冗余

2. **服务注册时机**：
   - 在应用启动时注册所有服务
   - 避免在运行时动态注册服务
   - 使用工厂模式延迟创建重量级服务

3. **懒加载 vs 立即加载**：
   - 对于重量级服务（如数据库连接、网络客户端），使用 `inject` 进行懒加载
   - 对于轻量级或必需的服务，使用 `getService`
   - 服务工厂天然支持懒加载，只在第一次获取时创建实例

4. **单例保证**：
   - 同一个类型的服务只能注册一个实例
   - 服务工厂创建的实例会被缓存，后续获取返回同一实例
   - 确保服务实例的线程安全性

5. **错误处理**：
   - 获取未注册的服务会抛出 `IllegalArgumentException`
   - 在测试中使用 `assertFailsWith` 验证异常行为

## 注意事项

1. **服务必须先注册才能获取**：调用 `getService` 或 `inject` 前确保服务已注册
2. **同一个类型的服务只能注册一个实例**：重复注册会覆盖之前的实例
3. **懒加载的服务在第一次访问时才会创建**：使用服务工厂时注意初始化时机
4. **线程安全**：确保服务实例在多线程环境下的安全性
5. **依赖顺序**：如果服务之间有依赖关系，注意注册顺序
6. **资源释放**：对于需要清理的资源，实现适当的关闭逻辑

## 架构说明

CLIContext 基于 `CLIServiceManager` 构建，后者是核心服务管理组件：

```
CLIContext (接口)
    ├── config: CLIConfig
    ├── resources: CLIResources
    ├── preferences: CLIPreferences
    ├── database: CLIRoomDatabase
    ├── navigator: CLINavigator
    ├── lifecycleOwner: CLILifecycleOwner
    ├── viewModelStoreOwner: CLIViewModelStoreOwner
    ├── coroutineScope: CoroutineScope
    └── serviceManager: CLIServiceManager (服务管理)
        ├── serviceRegistry (服务实例缓存)
        └── serviceFactories (服务工厂)

CLIContextImpl (实现类)
    └── 实现 CLIContext 接口
        └── 提供便捷的服务管理方法
            ├── registerService() - 注册服务实例
            ├── registerServiceFactory() - 注册服务工厂
            ├── getService() - 获取服务实例
            └── inject() - 懒加载注入服务
```

`CLIServiceManager` 提供：
- 服务实例注册和检索
- 服务工厂注册（懒加载）
- 单例保证（同一类型只创建一个实例）
- 类型安全的 API（支持 reified 类型参数）

`CLIContextImpl` 提供：
- 完整的 CLIContext 实现
- 内置 CLIServiceManager 实例
- 便捷的 reified 方法封装
- 统一的应用上下文管理