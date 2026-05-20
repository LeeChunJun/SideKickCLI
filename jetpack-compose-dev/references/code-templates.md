# 代码模板参考

本文件提供创建新模块时所需的核心代码模板。

## 1. Domain Layer 模板

### 数据模型 (domain/model)

```kotlin
package com.rhyme.sidekick.{module}.domain.model

/**
 * 数据模型示例
 */
data class ExampleModel(
    val id: String,
    val name: String,
    val description: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
```

### 仓库接口 (domain/repository)

```kotlin
package com.rhyme.sidekick.{module}.domain.repository

import com.rhyme.sidekick.{module}.domain.model.ExampleModel

/**
 * 数据仓库接口定义
 */
interface ExampleRepository {
    /**
     * 获取所有数据
     */
    suspend fun getAll(): List<ExampleModel>
    
    /**
     * 根据ID获取数据
     */
    suspend fun getById(id: String): ExampleModel?
    
    /**
     * 保存数据
     */
    suspend fun save(item: ExampleModel)
    
    /**
     * 删除数据
     */
    suspend fun delete(id: String)
}
```

### 用例 (domain/usecase)

```kotlin
package com.rhyme.sidekick.{module}.domain.usecase

import com.rhyme.sidekick.{module}.domain.model.ExampleModel
import com.rhyme.sidekick.{module}.domain.repository.ExampleRepository

/**
 * 获取所有数据的用例
 */
class GetAllExamplesUseCase(
    private val repository: ExampleRepository
) {
    suspend operator fun invoke(): List<ExampleModel> {
        return repository.getAll()
    }
}

/**
 * 根据ID获取数据的用例
 */
class GetExampleByIdUseCase(
    private val repository: ExampleRepository
) {
    suspend operator fun invoke(id: String): ExampleModel? {
        return repository.getById(id)
    }
}
```

## 2. Data Layer 模板

### 数据源实现 (data/source)

```kotlin
package com.rhyme.sidekick.{module}.data.source

import com.rhyme.sidekick.{module}.domain.model.ExampleModel
import com.rhyme.sidekick.{module}.domain.repository.ExampleRepository

/**
 * 数据源实现示例
 */
class ExampleDataSource : ExampleRepository {
    
    // 模拟数据存储
    private val dataStore = mutableListOf<ExampleModel>()
    
    override suspend fun getAll(): List<ExampleModel> {
        return dataStore.toList()
    }
    
    override suspend fun getById(id: String): ExampleModel? {
        return dataStore.find { it.id == id }
    }
    
    override suspend fun save(item: ExampleModel) {
        val existingIndex = dataStore.indexOfFirst { it.id == item.id }
        if (existingIndex >= 0) {
            dataStore[existingIndex] = item
        } else {
            dataStore.add(item)
        }
    }
    
    override suspend fun delete(id: String) {
        dataStore.removeAll { it.id == id }
    }
}
```

### 网络数据源示例（如需要）

```kotlin
package com.rhyme.sidekick.{module}.data.source

import com.rhyme.sidekick.network.dictionary.DictionaryApiService
import com.rhyme.sidekick.{module}.domain.model.ExampleModel
import com.rhyme.sidekick.{module}.domain.repository.ExampleRepository

/**
 * 网络数据源实现
 */
class NetworkExampleDataSource(
    private val apiService: DictionaryApiService
) : ExampleRepository {
    
    override suspend fun getAll(): List<ExampleModel> {
        // 调用 API 获取数据
        // 转换为领域模型
        TODO("Implement network call")
    }
    
    override suspend fun getById(id: String): ExampleModel? {
        TODO("Implement network call")
    }
    
    override suspend fun save(item: ExampleModel) {
        TODO("Implement if needed")
    }
    
    override suspend fun delete(id: String) {
        TODO("Implement if needed")
    }
}
```

## 3. Presentation Layer 模板

### UI Contract (presentation/Contract.kt)

```kotlin
package com.rhyme.sidekick.{module}.presentation

import com.rhyme.sidekick.{module}.domain.model.ExampleModel

/**
 * UI状态
 */
sealed interface ExampleUiState {
    object Initial : ExampleUiState
    object Loading : ExampleUiState
    data class Success(
        val items: List<ExampleModel> = emptyList(),
        val selectedItem: ExampleModel? = null,
        val searchQuery: String = ""
    ) : ExampleUiState
    data class Error(val message: String) : ExampleUiState
}

/**
 * UI事件（用户操作）
 */
sealed interface ExampleUiEvent {
    data class SearchQueryChanged(val query: String) : ExampleUiEvent
    object Search : ExampleUiEvent
    data class SelectItem(val item: ExampleModel) : ExampleUiEvent
    object ClearSelection : ExampleUiEvent
    object Refresh : ExampleUiEvent
}
```

### ViewModel (presentation/ViewModel.kt)

```kotlin
package com.rhyme.sidekick.{module}.presentation

import com.rhyme.sidekick.cmdx.viewmodel.common.viewModelScope
import com.rhyme.sidekick.cmdx.viewmodel.core.ViewModel
import com.rhyme.sidekick.{module}.domain.usecase.GetAllExamplesUseCase
import com.rhyme.sidekick.{module}.domain.usecase.GetExampleByIdUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel - MVI模式
 */
abstract class BaseExampleViewModel(
    protected val getAllExamplesUseCase: GetAllExamplesUseCase,
    protected val getExampleByIdUseCase: GetExampleByIdUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<ExampleUiState>(ExampleUiState.Initial)
    val uiState: StateFlow<ExampleUiState> = _uiState.asStateFlow()
    
    /**
     * 处理UI事件
     */
    fun onEvent(event: ExampleUiEvent) {
        when (event) {
            is ExampleUiEvent.SearchQueryChanged -> {
                updateSearchQuery(event.query)
            }
            is ExampleUiEvent.Search -> {
                search()
            }
            is ExampleUiEvent.SelectItem -> {
                selectItem(event.item)
            }
            is ExampleUiEvent.ClearSelection -> {
                clearSelection()
            }
            is ExampleUiEvent.Refresh -> {
                refresh()
            }
        }
    }
    
    private fun updateSearchQuery(query: String) {
        val currentState = _uiState.value
        if (currentState is ExampleUiState.Success) {
            _uiState.value = currentState.copy(searchQuery = query)
        } else {
            _uiState.value = ExampleUiState.Success(searchQuery = query)
        }
    }
    
    private fun search() {
        viewModelScope.launch {
            _uiState.value = ExampleUiState.Loading
            
            try {
                val results = getAllExamplesUseCase()
                _uiState.value = ExampleUiState.Success(items = results)
            } catch (e: Exception) {
                _uiState.value = ExampleUiState.Error("搜索失败: ${e.message}")
            }
        }
    }
    
    private fun selectItem(item: ExampleModel) {
        val currentState = _uiState.value
        if (currentState is ExampleUiState.Success) {
            _uiState.value = currentState.copy(selectedItem = item)
        }
    }
    
    private fun clearSelection() {
        val currentState = _uiState.value
        if (currentState is ExampleUiState.Success) {
            _uiState.value = currentState.copy(selectedItem = null)
        }
    }
    
    private fun refresh() {
        search()
    }
}
```

## 4. JVM Main 入口模板

### 主应用文件 (jvmMain/{Module}App.kt)

```kotlin
package com.rhyme.sidekick.{module}

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.rhyme.sidekick.cmdx.viewmodel.common.CLIViewModelStoreOwner
import com.rhyme.sidekick.cmdx.viewmodel.common.LocalCLIViewModelStoreOwner
import com.rhyme.sidekick.cmdx.viewmodel.common.cliViewModel
import com.rhyme.sidekick.cmdx.viewmodel.core.ViewModelStore
import com.rhyme.sidekick.{module}.presentation.ExampleUiEvent
import com.rhyme.sidekick.{module}.presentation.ExampleUiState
import com.rhyme.sidekick.{module}.presentation.ExampleViewModel

fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "{Module Name}") {
        MaterialTheme {
            ExampleRoot {
                ExampleApp()
            }
        }
    }
}

@Composable
fun ExampleRoot(content: @Composable () -> Unit) {
    val owner = remember { CLIViewModelStoreOwner() }
    
    CompositionLocalProvider(
        LocalCLIViewModelStoreOwner provides owner,
        content = content
    )
}

@Composable
fun ExampleApp() {
    // 使用 cmdx 的 cliViewModel 获取 ViewModel 实例
    val viewModel: ExampleViewModel = cliViewModel("{module}") { ViewModelStore() }
    val uiState by viewModel.uiState.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 标题
        Text(
            text = "{Module Name}",
            style = MaterialTheme.typography.headlineMedium
        )
        
        // 内容区域
        ContentSection(
            uiState = uiState,
            onEvent = viewModel::onEvent
        )
    }
}

@Composable
fun ContentSection(
    uiState: ExampleUiState,
    onEvent: (ExampleUiEvent) -> Unit
) {
    when (uiState) {
        is ExampleUiState.Initial -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Text("Welcome to {Module Name}")
            }
        }
        
        is ExampleUiState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        
        is ExampleUiState.Success -> {
            // 展示成功状态的内容
            Text("Loaded ${uiState.items.size} items")
        }
        
        is ExampleUiState.Error -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Text(
                    text = uiState.message,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
```

### JVM 特定 ViewModel 实现（如需要）

```kotlin
package com.rhyme.sidekick.{module}.presentation

import com.rhyme.sidekick.{module}.data.source.ExampleDataSource
import com.rhyme.sidekick.{module}.domain.usecase.GetAllExamplesUseCase
import com.rhyme.sidekick.{module}.domain.usecase.GetExampleByIdUseCase

/**
 * JVM 平台的 ViewModel 实现
 */
class ExampleViewModel : BaseExampleViewModel(
    getAllExamplesUseCase = GetAllExamplesUseCase(ExampleDataSource()),
    getExampleByIdUseCase = GetExampleByIdUseCase(ExampleDataSource())
)
```

## 5. 测试模板

### 单元测试示例

```kotlin
package com.rhyme.sidekick.{module}.domain.usecase

import com.rhyme.sidekick.{module}.domain.model.ExampleModel
import com.rhyme.sidekick.{module}.domain.repository.ExampleRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class GetAllExamplesUseCaseTest {
    
    private class FakeRepository : ExampleRepository {
        override suspend fun getAll(): List<ExampleModel> {
            return listOf(
                ExampleModel("1", "Item 1"),
                ExampleModel("2", "Item 2")
            )
        }
        
        override suspend fun getById(id: String): ExampleModel? = null
        override suspend fun save(item: ExampleModel) {}
        override suspend fun delete(id: String) {}
    }
    
    @Test
    fun `test get all examples`() = runTest {
        val useCase = GetAllExamplesUseCase(FakeRepository())
        val result = useCase()
        
        assertEquals(2, result.size)
        assertEquals("Item 1", result[0].name)
    }
}
```

## 使用说明

1. 将 `{module}` 替换为你的模块名称（小写）
2. 将 `{Module}` 替换为你的模块名称（首字母大写）
3. 根据实际需求调整数据模型和业务逻辑
4. 删除不需要的部分（如不需要网络功能，可以删除网络数据源）
5. 确保包名与目录结构一致