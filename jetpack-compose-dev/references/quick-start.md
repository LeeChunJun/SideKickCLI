# 快速上手指南

## 5分钟创建新模块

本指南将帮助你在 5 分钟内创建一个新的 Jetpack Compose 模块。

### 步骤 1: 确定模块名称

选择一个有意义的模块名称，例如：`calculator`、`notes`、`weather`

**命名规范**:
- 使用小写字母
- 多个单词用连字符或驼峰命名
- 避免使用保留字

### 步骤 2: 创建目录结构

在项目根目录执行以下命令（以 `calculator` 为例）：

```bash
# Windows PowerShell
mkdir -p feature\calculator\src\commonMain\kotlin\com\rhyme\sidekick\calculator\data\source
mkdir -p feature\calculator\src\commonMain\kotlin\com\rhyme\sidekick\calculator\domain\model
mkdir -p feature\calculator\src\commonMain\kotlin\com\rhyme\sidekick\calculator\domain\repository
mkdir -p feature\calculator\src\commonMain\kotlin\com\rhyme\sidekick\calculator\domain\usecase
mkdir -p feature\calculator\src\commonMain\kotlin\com\rhyme\sidekick\calculator\presentation
mkdir -p feature\calculator\src\jvmMain\kotlin\com\rhyme\sidekick\calculator
mkdir -p feature\calculator\src\jvmMain\kotlin\com\rhyme\sidekick\calculator\data
mkdir -p feature\calculator\src\jvmMain\kotlin\com\rhyme\sidekick\calculator\presentation

# Linux/Mac
mkdir -p feature/calculator/src/{commonMain,jvmMain}/{kotlin,resources}
mkdir -p feature/calculator/src/commonMain/kotlin/com/rhyme/sidekick/calculator/{data,domain,presentation}
mkdir -p feature/calculator/src/jvmMain/kotlin/com/rhyme/sidekick/calculator/{data,presentation}
```

### 步骤 3: 创建 build.gradle.kts

在 `feature/calculator/` 目录下创建 `build.gradle.kts`：

```kotlin
plugins {
    id("buildsrc.convention.kotlin-multiplatform")
    alias(libs.plugins.kotlinPluginSerialization)
    alias(libs.plugins.composePlugin)
    alias(libs.plugins.kotlinCompose)
}

group = "com.rhyme.sidekick"
version = "0.0.1"

repositories {
    google()
    mavenCentral()
    maven(url = "https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":core:cmdx"))
                implementation(libs.bundles.kotlinxEcosystem)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(compose.material3)
            }
        }
        val jvmTest by getting
    }
}
```

### 步骤 4: 注册模块

编辑根目录的 `settings.gradle.kts`，添加：

```kotlin
include(":feature:calculator")
```

### 步骤 5: 创建核心文件

#### 5.1 创建数据模型

文件: `feature/calculator/src/commonMain/kotlin/com/rhyme/sidekick/calculator/domain/model/Calculation.kt`

```kotlin
package com.rhyme.sidekick.calculator.domain.model

data class Calculation(
    val expression: String,
    val result: Double,
    val timestamp: Long = System.currentTimeMillis()
)
```

#### 5.2 创建仓库接口

文件: `feature/calculator/src/commonMain/kotlin/com/rhyme/sidekick/calculator/domain/repository/CalculatorRepository.kt`

```kotlin
package com.rhyme.sidekick.calculator.domain.repository

import com.rhyme.sidekick.calculator.domain.model.Calculation

interface CalculatorRepository {
    suspend fun calculate(expression: String): Result<Double>
    suspend fun getHistory(): List<Calculation>
    suspend fun clearHistory()
}
```

#### 5.3 创建用例

文件: `feature/calculator/src/commonMain/kotlin/com/rhyme/sidekick/calculator/domain/usecase/CalculateUseCase.kt`

```kotlin
package com.rhyme.sidekick.calculator.domain.usecase

import com.rhyme.sidekick.calculator.domain.repository.CalculatorRepository

class CalculateUseCase(
    private val repository: CalculatorRepository
) {
    suspend operator fun invoke(expression: String): Result<Double> {
        return repository.calculate(expression)
    }
}
```

文件: `feature/calculator/src/commonMain/kotlin/com/rhyme/sidekick/calculator/domain/usecase/GetHistoryUseCase.kt`

```kotlin
package com.rhyme.sidekick.calculator.domain.usecase

import com.rhyme.sidekick.calculator.domain.model.Calculation
import com.rhyme.sidekick.calculator.domain.repository.CalculatorRepository

class GetHistoryUseCase(
    private val repository: CalculatorRepository
) {
    suspend operator fun invoke(): List<Calculation> {
        return repository.getHistory()
    }
}
```

#### 5.4 创建数据源实现

文件: `feature/calculator/src/jvmMain/kotlin/com/rhyme/sidekick/calculator/data/CalculatorDataSource.kt`

```kotlin
package com.rhyme.sidekick.calculator.data

import com.rhyme.sidekick.calculator.domain.model.Calculation
import com.rhyme.sidekick.calculator.domain.repository.CalculatorRepository
import javax.script.ScriptEngineManager

class CalculatorDataSource : CalculatorRepository {
    
    private val history = mutableListOf<Calculation>()
    private val engine = ScriptEngineManager().getEngineByName("JavaScript")
    
    override suspend fun calculate(expression: String): Result<Double> {
        return try {
            val result = engine.eval(expression).toString().toDouble()
            val calculation = Calculation(expression, result)
            history.add(0, calculation)
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getHistory(): List<Calculation> {
        return history.toList()
    }
    
    override suspend fun clearHistory() {
        history.clear()
    }
}
```

#### 5.5 创建 UI Contract

文件: `feature/calculator/src/commonMain/kotlin/com/rhyme/sidekick/calculator/presentation/CalculatorContract.kt`

```kotlin
package com.rhyme.sidekick.calculator.presentation

import com.rhyme.sidekick.calculator.domain.model.Calculation

sealed interface CalculatorUiState {
    object Initial : CalculatorUiState
    data class Success(
        val currentExpression: String = "",
        val currentResult: String = "",
        val history: List<Calculation> = emptyList()
    ) : CalculatorUiState
    data class Error(val message: String) : CalculatorUiState
}

sealed interface CalculatorUiEvent {
    data class ExpressionChanged(val expression: String) : CalculatorUiEvent
    object Calculate : CalculatorUiEvent
    object Clear : CalculatorUiEvent
    object LoadHistory : CalculatorUiEvent
}
```

#### 5.6 创建 ViewModel

文件: `feature/calculator/src/commonMain/kotlin/com/rhyme/sidekick/calculator/presentation/CalculatorViewModel.kt`

```kotlin
package com.rhyme.sidekick.calculator.presentation

import com.rhyme.sidekick.cmdx.viewmodel.common.viewModelScope
import com.rhyme.sidekick.cmdx.viewmodel.core.ViewModel
import com.rhyme.sidekick.calculator.domain.usecase.CalculateUseCase
import com.rhyme.sidekick.calculator.domain.usecase.GetHistoryUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

abstract class BaseCalculatorViewModel(
    protected val calculateUseCase: CalculateUseCase,
    protected val getHistoryUseCase: GetHistoryUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<CalculatorUiState>(CalculatorUiState.Initial)
    val uiState: StateFlow<CalculatorUiState> = _uiState.asStateFlow()
    
    fun onEvent(event: CalculatorUiEvent) {
        when (event) {
            is CalculatorUiEvent.ExpressionChanged -> {
                updateExpression(event.expression)
            }
            is CalculatorUiEvent.Calculate -> {
                calculate()
            }
            is CalculatorUiEvent.Clear -> {
                clear()
            }
            is CalculatorUiEvent.LoadHistory -> {
                loadHistory()
            }
        }
    }
    
    private fun updateExpression(expression: String) {
        val currentState = _uiState.value
        if (currentState is CalculatorUiState.Success) {
            _uiState.value = currentState.copy(currentExpression = expression)
        } else {
            _uiState.value = CalculatorUiState.Success(currentExpression = expression)
        }
    }
    
    private fun calculate() {
        viewModelScope.launch {
            val currentState = _uiState.value
            val expression = if (currentState is CalculatorUiState.Success) {
                currentState.currentExpression
            } else {
                ""
            }
            
            if (expression.isBlank()) return@launch
            
            calculateUseCase(expression).onSuccess { result ->
                _uiState.value = CalculatorUiState.Success(
                    currentExpression = expression,
                    currentResult = result.toString()
                )
                loadHistory()
            }.onFailure { error ->
                _uiState.value = CalculatorUiState.Error(error.message ?: "计算失败")
            }
        }
    }
    
    private fun clear() {
        _uiState.value = CalculatorUiState.Success()
    }
    
    private fun loadHistory() {
        viewModelScope.launch {
            val history = getHistoryUseCase()
            val currentState = _uiState.value
            if (currentState is CalculatorUiState.Success) {
                _uiState.value = currentState.copy(history = history)
            }
        }
    }
}
```

文件: `feature/calculator/src/jvmMain/kotlin/com/rhyme/sidekick/calculator/presentation/CalculatorViewModel.kt`

```kotlin
package com.rhyme.sidekick.calculator.presentation

import com.rhyme.sidekick.calculator.data.CalculatorDataSource
import com.rhyme.sidekick.calculator.domain.usecase.CalculateUseCase
import com.rhyme.sidekick.calculator.domain.usecase.GetHistoryUseCase

class CalculatorViewModel : BaseCalculatorViewModel(
    calculateUseCase = CalculateUseCase(CalculatorDataSource()),
    getHistoryUseCase = GetHistoryUseCase(CalculatorDataSource())
)
```

#### 5.7 创建主应用

文件: `feature/calculator/src/jvmMain/kotlin/com/rhyme/sidekick/calculator/CalculatorApp.kt`

```kotlin
package com.rhyme.sidekick.calculator

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.rhyme.sidekick.cmdx.viewmodel.common.CLIViewModelStoreOwner
import com.rhyme.sidekick.cmdx.viewmodel.common.LocalCLIViewModelStoreOwner
import com.rhyme.sidekick.cmdx.viewmodel.common.cliViewModel
import com.rhyme.sidekick.cmdx.viewmodel.core.ViewModelStore
import com.rhyme.sidekick.calculator.presentation.CalculatorUiEvent
import com.rhyme.sidekick.calculator.presentation.CalculatorUiState
import com.rhyme.sidekick.calculator.presentation.CalculatorViewModel

fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "Calculator") {
        MaterialTheme {
            CalculatorRoot {
                CalculatorApp()
            }
        }
    }
}

@Composable
fun CalculatorRoot(content: @Composable () -> Unit) {
    val owner = remember { CLIViewModelStoreOwner() }
    
    CompositionLocalProvider(
        LocalCLIViewModelStoreOwner provides owner,
        content = content
    )
}

@Composable
fun CalculatorApp() {
    val viewModel: CalculatorViewModel = cliViewModel("calculator") { ViewModelStore() }
    val uiState by viewModel.uiState.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Calculator",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        
        CalculatorInputSection(
            uiState = uiState,
            onEvent = viewModel::onEvent
        )
        
        CalculatorHistorySection(
            uiState = uiState
        )
    }
}

@Composable
fun CalculatorInputSection(
    uiState: CalculatorUiState,
    onEvent: (CalculatorUiEvent) -> Unit
) {
    val expression = if (uiState is CalculatorUiState.Success) {
        uiState.currentExpression
    } else {
        ""
    }
    
    val result = if (uiState is CalculatorUiState.Success) {
        uiState.currentResult
    } else {
        ""
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = expression,
                onValueChange = { onEvent(CalculatorUiEvent.ExpressionChanged(it)) },
                label = { Text("Expression") },
                placeholder = { Text("e.g., 2 + 3 * 4") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            if (result.isNotEmpty()) {
                Text(
                    text = "= $result",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.align(Alignment.End)
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { onEvent(CalculatorUiEvent.Calculate) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Calculate")
                }
                
                OutlinedButton(
                    onClick = { onEvent(CalculatorUiEvent.Clear) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Clear")
                }
            }
        }
    }
}

@Composable
fun CalculatorHistorySection(
    uiState: CalculatorUiState
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "History",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            val history = if (uiState is CalculatorUiState.Success) {
                uiState.history
            } else {
                emptyList()
            }
            
            if (history.isEmpty()) {
                Text(
                    text = "No calculations yet",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 300.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(history) { calculation ->
                        Text(
                            text = "${calculation.expression} = ${calculation.result}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}
```

### 步骤 6: 运行模块

```bash
./gradlew :feature:calculator:run
```

## 完成！

现在你已经成功创建了一个可以运行的计算器模块。接下来可以：

1. 添加更多功能（如科学计算）
2. 美化 UI 界面
3. 添加单元测试
4. 持久化历史记录到数据库

## 下一步

- 阅读 [架构设计参考](references/architecture.md) 深入了解架构
- 查看 [代码模板](references/code-templates.md) 获取更多模板
- 参考 dictionary 模块学习更复杂的实现