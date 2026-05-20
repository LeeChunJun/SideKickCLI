# 使用 cmdx ViewModel 库的改造说明

## 概述

字典模块已更新为使用项目内部的 `core:cmdx` 库中的 ViewModel 实现，替代了之前的 AndroidX Lifecycle ViewModel。

## 主要变更

### 1. 依赖变更

**移除：**
```kotlin
implementation(libs.androidx.lifecycle.viewmodel)
```

**添加：**
```kotlin
implementation(project(":core:cmdx"))
```

### 2. ViewModel 基类变更

**之前：**
```kotlin
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

class DictionaryViewModel(
    private val searchWordsUseCase: SearchWordsUseCase,
    private val getWordDetailUseCase: GetWordDetailUseCase
) : ViewModel() {
    // ...
}
```

**现在：**
```kotlin
// commonMain - 抽象基类
import com.rhyme.sidekick.cmdx.viewmodel.core.ViewModel
import com.rhyme.sidekick.cmdx.viewmodel.common.viewModelScope

abstract class BaseDictionaryViewModel(
    protected val searchWordsUseCase: SearchWordsUseCase,
    protected val getWordDetailUseCase: GetWordDetailUseCase
) : ViewModel() {
    // MVI 逻辑...
}

// jvmMain - 具体实现
class DictionaryViewModel : BaseDictionaryViewModel(
    searchWordsUseCase = SearchWordsUseCase(
        WordRepository(
            LocalWordDataSource(),
            RemoteWordDetailDataSource()
        )
    ),
    getWordDetailUseCase = GetWordDetailUseCase(
        WordRepository(
            LocalWordDataSource(),
            RemoteWordDetailDataSource()
        )
    )
)
```

### 3. UI 层获取 ViewModel 的方式变更

**之前：**
```kotlin
val viewModel = remember { DictionaryDI.createViewModel() }
```

**现在：**
```kotlin
// 在根节点提供 CLIViewModelStoreOwner
@Composable
fun DictionaryRoot(content: @Composable () -> Unit) {
    val owner = remember { CLIViewModelStoreOwner() }
    
    CompositionLocalProvider(
        LocalCLIViewModelStoreOwner provides owner,
        content = content
    )
}

// 在子组件中使用 cliViewModel 获取实例
@Composable
fun DictionaryApp() {
    val viewModel: DictionaryViewModel = cliViewModel("dictionary") { ViewModelStore() }
    val uiState by viewModel.uiState.collectAsState()
    // ...
}
```

### 4. 移除的文件

- `feature/dictionary/src/jvmMain/kotlin/com/rhyme/sidekick/dictionary/di/DictionaryDI.kt` - 不再需要手动DI

## cmdx ViewModel 的特点

### 优势

1. **项目内部统一**: 使用项目自己的ViewModel库，保持技术栈一致
2. **轻量级**: 不依赖AndroidX，更适合Kotlin Multiplatform项目
3. **功能完整**: 提供与AndroidX类似的功能
   - ViewModel基类
   - ViewModelStore管理
   - viewModelScope协程支持
   - Compose集成（cliViewModel）

### 注意事项

1. **反射创建**: cmdx的`NewInstanceFactory`使用反射创建ViewModel，因此需要无参构造函数
2. **跨平台适配**: 
   - `BaseDictionaryViewModel` 放在 commonMain，包含MVI逻辑
   - `DictionaryViewModel` 具体实现放在 jvmMain，负责创建平台特定的数据源
3. **生命周期**: 通过`CLIViewModelStoreOwner`管理ViewModel的生命周期

## MVI 架构保持不变

尽管更换了ViewModel库，但MVI架构模式完全保持不变：

- ✅ **State**: `DictionaryUiState` - 密封接口定义UI状态
- ✅ **Event**: `DictionaryUiEvent` - 密封接口定义用户事件  
- ✅ **ViewModel**: `DictionaryViewModel` - 处理业务逻辑和状态管理
- ✅ **单向数据流**: Event → ViewModel → State → UI

## 迁移指南

如果你需要在其他模块中使用cmdx的ViewModel，请参考以下步骤：

### 1. 添加依赖

```kotlin
// build.gradle.kts
kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":core:cmdx"))
            }
        }
    }
}
```

### 2. 继承cmdx ViewModel

```kotlin
import com.rhyme.sidekick.cmdx.viewmodel.core.ViewModel
import com.rhyme.sidekick.cmdx.viewmodel.common.viewModelScope

class MyViewModel : ViewModel() {
    // 使用 viewModelScope 启动协程
    init {
        viewModelScope.launch {
            // 异步操作
        }
    }
    
    override fun onCleared() {
        // 清理资源
    }
}
```

### 3. 在Compose中使用

```kotlin
@Composable
fun MyScreen() {
    // 提供 ViewModelStoreOwner
    val owner = remember { CLIViewModelStoreOwner() }
    
    CompositionLocalProvider(
        LocalCLIViewModelStoreOwner provides owner
    ) {
        // 使用 cliViewModel 获取实例
        val viewModel: MyViewModel = cliViewModel("my_key") { ViewModelStore() }
        
        // 观察状态
        val state by viewModel.stateFlow.collectAsState()
        
        // UI 代码
    }
}
```

## 相关文档

- [cmdx ViewModel 测试示例](../../core/cmdx/src/test/kotlin/com/rhyme/sidekick/cmdx/viewmodel/)
- [ARCHITECTURE.md](ARCHITECTURE.md) - 完整的架构说明

## 总结

通过使用cmdx库的ViewModel实现，字典模块现在：
- ✅ 使用项目内部统一的ViewModel解决方案
- ✅ 保持了完整的MVI架构模式
- ✅ 简化了依赖管理（移除了AndroidX依赖）
- ✅ 更好地适配Kotlin Multiplatform环境
