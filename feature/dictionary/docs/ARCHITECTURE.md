# Dictionary Module - MVI Architecture

## 架构概述

字典模块采用了清晰的三层架构和MVI（Model-View-Intent）模式：

```
┌─────────────────────────────────────────┐
│          Presentation Layer             │
│  (DictionaryViewModel + UI State/Event) │
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│           Domain Layer                  │
│    (Repository + UseCases)              │
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│            Data Layer                   │
│  (Local DataSource + Remote DataSource) │
└─────────────────────────────────────────┘
```

## 目录结构

```
feature/dictionary/src/
├── commonMain/kotlin/com/rhyme/sidekick/dictionary/
│   ├── domain/
│   │   ├── model/
│   │   │   └── Word.kt                    # 单词实体类
│   │   ├── repository/
│   │   │   └── WordRepository.kt          # 仓库接口实现
│   │   └── usecase/
│   │       ├── SearchWordsUseCase.kt      # 搜索单词用例
│   │       └── GetWordDetailUseCase.kt    # 获取单词详情用例
│   ├── data/
│   │   └── source/
│   │       └── WordDataSource.kt          # 数据源接口
│   └── presentation/
│       ├── DictionaryContract.kt          # MVI: State & Event
│       └── DictionaryViewModel.kt         # ViewModel基类 (commonMain)
│
└── jvmMain/kotlin/com/rhyme/sidekick/dictionary/
    ├── data/
    │   └── source/
    │       ├── local/
    │       │   └── LocalWordDataSource.kt     # 本地数据源(english.txt)
    │       └── remote/
    │           └── RemoteWordDetailDataSource.kt  # 远程数据源(API)
    ├── presentation/
    │   └── DictionaryViewModel.jvm.kt     # ViewModel实现 (jvmMain)
    └── DictionaryApp.kt                   # UI入口 (使用 cliViewModel)
```

## 核心组件说明

### 1. Data Layer (数据层)

#### LocalWordDataSource
- 从 `resources/english.txt` 文件读取单词列表
- 支持模糊搜索和精确匹配
- 使用懒加载，首次访问时加载所有单词到内存

#### RemoteWordDetailDataSource
- 使用 `DictionaryApiService` 查询单词详情
- 返回完整的单词信息：音标、释义、例句、同义词等
- 基于 Free Dictionary API (https://api.dictionaryapi.dev/)

### 2. Domain Layer (领域层)

#### WordRepository
- 协调本地和远程数据源
- 提供统一的业务逻辑接口

#### UseCases
- **SearchWordsUseCase**: 搜索单词列表
- **GetWordDetailUseCase**: 获取单词详细信息

### 3. Presentation Layer (表示层) - MVI模式

#### DictionaryUiState (状态)
```kotlin
sealed interface DictionaryUiState {
    object Initial : DictionaryUiState           // 初始状态
    object Loading : DictionaryUiState           // 加载中
    data class Success(...) : DictionaryUiState  // 成功状态
    data class Error(val message: String) : DictionaryUiState  // 错误状态
}
```

#### DictionaryUiEvent (事件)
```kotlin
sealed interface DictionaryUiEvent {
    data class SearchQueryChanged(val query: String) : DictionaryUiEvent  // 搜索文本变化
    object SearchWords : DictionaryUiEvent                                // 执行搜索
    data class SelectWord(val word: String) : DictionaryUiEvent          // 选择单词
    object ClearSelection : DictionaryUiEvent                            // 清除选择
}
```

#### DictionaryViewModel
- **commonMain**: `BaseDictionaryViewModel` - 抽象基类，包含MVI逻辑
- **jvmMain**: `DictionaryViewModel` - 具体实现，创建平台特定数据源
- 继承自 `com.rhyme.sidekick.cmdx.viewmodel.core.ViewModel`
- 管理UI状态和业务逻辑
- 处理用户事件
- 调用UseCase执行业务操作
- 使用Kotlin Flow进行响应式状态管理
- 通过反射创建（无参构造函数），支持跨平台架构

### 4. UI Layer (界面层)

使用Jetpack Compose构建响应式界面：
- **DictionaryRoot**: 提供CLIViewModelStoreOwner的CompositionLocal
- **cliViewModel**: 从cmdx库获取ViewModel实例
- **SearchSection**: 搜索输入区域
- **SearchResultsList**: 搜索结果列表
- **WordDetailPanel**: 单词详情展示
- 支持左右分栏布局

## 数据流

```
用户操作 → UI Event → ViewModel → UseCase → Repository → DataSource
                                                        ↓
UI更新 ← State Flow ← ViewModel ← UseCase ← Repository ← Response
```

## 特性

✅ **清晰的分层架构**: Data → Domain → Presentation → UI  
✅ **MVI模式**: 单向数据流，状态可预测  
✅ **响应式编程**: 使用Kotlin Flow和StateFlow  
✅ **cmdx ViewModel集成**: 使用项目内部的ViewModel库  
✅ **异步处理**: 协程处理IO操作  
✅ **错误处理**: Result类型封装可能的错误  
✅ **资源管理**: 从本地文件和网络API获取数据  

## 运行应用

```bash
./gradlew :feature:dictionary:run
```

## 使用的技术栈

- **Kotlin Multiplatform**: 跨平台支持
- **Jetpack Compose Desktop**: 声明式UI
- **cmdx ViewModel**: 项目内部的ViewModel库（core:cmdx）
- **Kotlin Coroutines**: 异步编程
- **Kotlin Flow**: 响应式数据流
- **Retrofit + OkHttp**: 网络请求
- **Gson**: JSON序列化

## 示例数据

- **english.txt**: 包含23万+英语单词列表
- **sample_for_hello.json**: "hello"单词的完整API响应示例

## 扩展建议

1. **缓存机制**: 添加单词详情缓存，减少网络请求
2. **离线模式**: 将常用单词存储到SQLite数据库
3. **收藏功能**: 允许用户收藏常用单词
4. **历史记录**: 记录查询历史
5. **发音播放**: 集成音频播放功能
6. **单元测试**: 为ViewModel和UseCase添加测试
