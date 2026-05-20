# 架构设计参考

## Clean Architecture + MVI 架构

本项目采用 Clean Architecture（整洁架构）结合 MVI（Model-View-Intent）模式，确保代码的可测试性、可维护性和可扩展性。

### 架构图

```
┌─────────────────────────────────────────┐
│              UI Layer                    │
│         (Jetpack Compose)                │
├─────────────────────────────────────────┤
│          Presentation Layer              │
│     (ViewModel, State, Events)           │
├─────────────────────────────────────────┤
│            Domain Layer                  │
│   (Use Cases, Repository Interfaces)     │
├─────────────────────────────────────────┤
│             Data Layer                   │
│    (Repository Implementations, APIs)    │
└─────────────────────────────────────────┘
```

### 各层职责

#### 1. Domain Layer（领域层）

位于 `src/commonMain/kotlin/com/rhyme/sidekick/{module}/domain/`

**包含：**
- **Models**: 业务实体和数据模型
- **Repositories**: 数据访问接口定义
- **UseCases**: 具体的业务逻辑用例

**特点：**
- 不依赖任何其他层
- 纯 Kotlin 代码，可在所有平台运行
- 定义核心业务规则

**示例结构：**
```
domain/
├── model/          # 数据模型
│   └── Word.kt
├── repository/     # 仓库接口
│   └── DictionaryRepository.kt
└── usecase/        # 用例
    ├── SearchWordsUseCase.kt
    └── GetWordDetailUseCase.kt
```

#### 2. Data Layer（数据层）

位于 `src/commonMain/kotlin/com/rhyme/sidekick/{module}/data/` 和 `src/jvmMain/kotlin/com/rhyme/sidekick/{module}/data/`

**包含：**
- **Data Sources**: 数据来源实现（网络、数据库、文件等）
- **Repository Implementations**: 仓库接口的具体实现
- **DTOs**: 数据传输对象

**特点：**
- 实现 Domain 层定义的接口
- 处理数据的获取、缓存和转换
- 可能包含平台特定代码

**示例结构：**
```
data/
└── source/
    └── DictionaryDataSource.kt
```

#### 3. Presentation Layer（表示层）

位于 `src/commonMain/kotlin/com/rhyme/sidekick/{module}/presentation/`

**包含：**
- **ViewModel**: 管理 UI 状态和业务逻辑
- **Contract**: UI 状态和事件的定义
- **Mappers**: 数据映射器（可选）

**特点：**
- 使用 MVI 模式管理状态
- 通过 StateFlow 暴露 UI 状态
- 处理用户事件并更新状态

**MVI 模式说明：**

```
┌──────────┐     Event      ┌──────────────┐
│   View   │ ─────────────> │  ViewModel   │
│          │ <───────────── │              │
│          │     State      │              │
└──────────┘                └──────────────┘
```

- **Intent (Event)**: 用户操作或意图（如点击按钮、输入文本）
- **Model (State)**: UI 的当前状态（如加载中、成功、错误）
- **View**: 根据状态渲染 UI

**示例结构：**
```
presentation/
├── DictionaryContract.kt    # UI 状态和事件定义
└── DictionaryViewModel.kt   # ViewModel 实现
```

#### 4. UI Layer（UI 层）

位于 `src/jvmMain/kotlin/com/rhyme/sidekick/{module}/`

**包含：**
- **Composable Functions**: Jetpack Compose UI 组件
- **Main Entry Point**: 应用入口点
- **Theme**: 主题配置（可选）

**特点：**
- 平台特定代码（JVM）
- 声明式 UI 构建
- 响应状态变化自动更新界面

### 数据流示例

以字典模块的搜索功能为例：

1. **用户操作**: 用户在搜索框输入单词并点击搜索按钮
2. **事件触发**: UI 层调用 `viewModel.onEvent(DictionaryUiEvent.SearchWords)`
3. **ViewModel 处理**: 
   - 从 StateFlow 获取当前查询字符串
   - 设置 Loading 状态
   - 调用 UseCase 执行业务逻辑
4. **UseCase 执行**: 调用 Repository 获取数据
5. **Repository 实现**: 从数据源（如 API）获取数据
6. **状态更新**: ViewModel 更新 StateFlow 为 Success 或 Error 状态
7. **UI 响应**: Composable 函数观察到状态变化，重新渲染界面

### 关键设计原则

1. **单向数据流**: 数据只能从上层流向下层，事件从下层流向上层
2. **不可变性**: 状态对象应该是不可变的
3. **关注点分离**: 每一层只关心自己的职责
4. **依赖倒置**: 高层模块不依赖低层模块的具体实现
5. **平台抽象**: 通用代码放在 commonMain，平台特定代码放在对应源集

### 协程使用

所有异步操作都使用 Kotlin 协程：

```kotlin
viewModelScope.launch {
    try {
        val result = useCase.execute()
        _uiState.value = UiState.Success(result)
    } catch (e: Exception) {
        _uiState.value = UiState.Error(e.message)
    }
}
```

### 状态管理最佳实践

1. 使用 `MutableStateFlow` 作为内部状态容器
2. 使用 `asStateFlow()` 暴露只读状态
3. 在 UI 层使用 `collectAsState()` 观察状态变化
4. 状态类应该密封（sealed），便于 exhaustive when 表达式

### 测试策略

- **Domain Layer**: 单元测试，无需模拟
- **Data Layer**: 单元测试，模拟外部依赖
- **Presentation Layer**: 单元测试，模拟 UseCases
- **UI Layer**: 集成测试或手动测试