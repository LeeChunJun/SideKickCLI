# Dictionary Module - 快速参考

## 📁 核心文件位置

### MVI Contract
- **State & Event**: `feature/dictionary/src/commonMain/.../presentation/DictionaryContract.kt`

### ViewModel
- **基类 (commonMain)**: `feature/dictionary/src/commonMain/.../presentation/DictionaryViewModel.kt`
- **实现 (jvmMain)**: `feature/dictionary/src/jvmMain/.../presentation/DictionaryViewModel.jvm.kt`

### UI
- **主界面**: `feature/dictionary/src/jvmMain/.../DictionaryApp.kt`

## 🔑 关键API

### State类型
```kotlin
sealed interface DictionaryUiState {
    object Initial          // 初始状态
    object Loading          // 加载中
    data class Success(...) // 成功（包含搜索结果和选中单词）
    data class Error(...)   // 错误
}
```

### Event类型
```kotlin
sealed interface DictionaryUiEvent {
    data class SearchQueryChanged(val query: String)  // 搜索文本变化
    object SearchWords                                 // 执行搜索
    data class SelectWord(val word: String)           // 选择单词
    object ClearSelection                              // 清除选择
}
```

### 获取ViewModel
```kotlin
@Composable
fun DictionaryApp() {
    val viewModel: DictionaryViewModel = cliViewModel("dictionary") { ViewModelStore() }
    val uiState by viewModel.uiState.collectAsState()
    
    // 发送事件
    viewModel.onEvent(DictionaryUiEvent.SearchWords)
}
```

## 🎯 常用操作

### 1. 搜索单词
```kotlin
// 更新搜索文本
viewModel.onEvent(DictionaryUiEvent.SearchQueryChanged("hello"))

// 执行搜索
viewModel.onEvent(DictionaryUiEvent.SearchWords)
```

### 2. 选择单词查看详情
```kotlin
viewModel.onEvent(DictionaryUiEvent.SelectWord("hello"))
```

### 3. 观察状态
```kotlin
val uiState by viewModel.uiState.collectAsState()

when (uiState) {
    is DictionaryUiState.Success -> {
        val results = uiState.searchResults
        val selectedWord = uiState.selectedWord
    }
    is DictionaryUiState.Error -> {
        val message = uiState.message
    }
    // ...
}
```

## 🏗️ 架构层次

```
UI Layer (DictionaryApp.kt)
    ↓ uses cliViewModel
Presentation Layer (DictionaryViewModel)
    ↓ calls UseCase
Domain Layer (UseCases + Repository)
    ↓ accesses DataSource
Data Layer (Local + Remote)
```

## 📦 依赖关系

```kotlin
// build.gradle.kts
implementation(project(":core:cmdx"))      // ViewModel
implementation(project(":core:network"))   // API Service
implementation(libs.bundles.kotlinxEcosystem)
```

## 🚀 运行命令

```bash
./gradlew :feature:dictionary:run
```

## 💡 提示

- ✅ ViewModel通过反射创建，需要无参构造函数
- ✅ 数据源实现在jvmMain，ViewModel分两层（基类+实现）
- ✅ 使用cliViewModel时需要在根节点提供CLIViewModelStoreOwner
- ✅ StateFlow自动响应式更新UI
