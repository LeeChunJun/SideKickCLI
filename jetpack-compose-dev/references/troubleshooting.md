# 常见问题解答

## 构建相关问题

### 1. Gradle 同步失败

**问题**: 添加新模块后，Gradle 同步失败

**解决方案**:
```bash
# 清理并重新构建
./gradlew clean
./gradlew build

# 如果仍然失败，删除 .gradle 文件夹后重试
rm -rf .gradle
./gradlew build
```

### 2. 依赖解析失败

**问题**: 无法解析某些依赖

**解决方案**:
1. 检查 `gradle/libs.versions.toml` 中的版本是否正确
2. 确认 repositories 配置包含所需的仓库源
3. 检查网络连接是否正常
4. 尝试刷新依赖：`./gradlew --refresh-dependencies`

### 3. Kotlin 版本冲突

**问题**: 不同模块使用了不同的 Kotlin 版本

**解决方案**:
确保所有模块都使用版本目录中定义的 Kotlin 版本：
```kotlin
plugins {
    kotlin("multiplatform") version libs.versions.kotlin.get()
}
```

## Compose 相关问题

### 4. Compose 编译器不匹配

**问题**: Compose 编译器与 Kotlin 版本不兼容

**解决方案**:
参考官方兼容性表格，确保版本匹配：
- Kotlin 2.1.0 对应 Compose Compiler 2.1.0
- 在 `libs.versions.toml` 中统一版本

### 5. @Composable 函数无法识别

**问题**: IDE 提示 @Composable 注解无法识别

**解决方案**:
1. 确认已应用 Compose 插件：
   ```kotlin
   plugins {
       alias(libs.plugins.composePlugin)
       alias(libs.plugins.kotlinCompose)
   }
   ```
2. 重新导入 Gradle 项目
3. 使缓存无效并重启 IDE

### 6. MaterialTheme 找不到

**问题**: 无法导入 MaterialTheme

**解决方案**:
确保在 `build.gradle.kts` 中添加了 Material3 依赖：
```kotlin
kotlin {
    sourceSets {
        val jvmMain by getting {
            dependencies {
                implementation(compose.material3)
            }
        }
    }
}
```

## ViewModel 相关问题

### 7. cliViewModel 无法使用

**问题**: 无法调用 `cliViewModel` 函数

**解决方案**:
1. 确认依赖了 `:core:cmdx` 模块
2. 检查是否正确设置了 `CompositionLocalProvider`：
   ```kotlin
   @Composable
   fun AppRoot(content: @Composable () -> Unit) {
       val owner = remember { CLIViewModelStoreOwner() }
       
       CompositionLocalProvider(
           LocalCLIViewModelStoreOwner provides owner,
           content = content
       )
   }
   ```

### 8. ViewModel 状态不更新

**问题**: UI 没有响应状态变化

**解决方案**:
1. 确认使用了 `collectAsState()` 观察状态：
   ```kotlin
   val uiState by viewModel.uiState.collectAsState()
   ```
2. 检查是否正确更新了 StateFlow：
   ```kotlin
   _uiState.value = NewState
   ```
3. 确保在协程作用域中更新状态

### 9. viewModelScope 不可用

**问题**: 无法访问 `viewModelScope`

**解决方案**:
确认 ViewModel 继承自正确的基类：
```kotlin
import com.rhyme.sidekick.cmdx.viewmodel.core.ViewModel

class MyViewModel : ViewModel() {
    // viewModelScope 现在可用
}
```

## 架构相关问题

### 10. 循环依赖

**问题**: 模块之间存在循环依赖

**解决方案**:
1. 重新设计模块边界
2. 提取共享代码到独立的 common 模块
3. 使用接口抽象打破循环

### 11. 平台特定代码混淆

**问题**: commonMain 和 jvmMain 的代码职责不清

**解决方案**:
- **commonMain**: 放置所有平台通用的代码（业务逻辑、接口定义）
- **jvmMain**: 仅放置 JVM 特定的实现（UI、平台 API 调用）
- 在 commonMain 中定义接口，在 jvmMain 中实现

### 12. UseCase 是否必要

**问题**: 简单的功能是否需要创建 UseCase

**解决方案**:
- 简单 CRUD 操作可以直接在 ViewModel 中调用 Repository
- 复杂业务逻辑应该封装在 UseCase 中
- 考虑未来可能的复用性和测试便利性

## 运行相关问题

### 13. 模块无法运行

**问题**: 执行 `./gradlew :feature:module:run` 时出错

**解决方案**:
1. 确认主函数正确定义：
   ```kotlin
   fun main() = application {
       Window(onCloseRequest = ::exitApplication) {
           // ...
       }
   }
   ```
2. 检查是否有编译错误
3. 确认所有依赖都已正确配置

### 14. 窗口无法显示

**问题**: 程序运行但窗口不显示

**解决方案**:
1. 检查是否有异常抛出
2. 确认调用了 `application` 函数
3. 检查窗口大小和内容是否正确设置

### 15. 热重载不工作

**问题**: 修改代码后需要手动重启

**解决方案**:
JetBrains Compose Desktop 目前不支持完整的热重载，但可以：
1. 使用 IDE 的 "Run" 而不是 "Debug" 模式
2. 考虑使用 Compose for Web 进行快速原型开发

## 测试相关问题

### 16. 测试无法找到

**问题**: 编写的测试没有被执行

**解决方案**:
1. 确认测试文件放在正确的目录：`src/commonTest` 或 `src/jvmTest`
2. 检查测试函数是否有 `@Test` 注解
3. 确认测试依赖已添加：
   ```kotlin
   val commonTest by getting {
       dependencies {
           implementation(kotlin("test"))
       }
   }
   ```

### 17. 协程测试失败

**问题**: 涉及协程的测试失败

**解决方案**:
使用 `runTest` 包装协程测试：
```kotlin
import kotlinx.coroutines.test.runTest

@Test
fun testCoroutine() = runTest {
    // 测试代码
}
```

## 性能相关问题

### 18. UI 卡顿

**问题**: 界面响应缓慢

**解决方案**:
1. 避免在 Composable 函数中执行耗时操作
2. 使用 `remember` 缓存计算结果
3. 将大数据集分页加载
4. 使用 `LazyColumn` 代替普通 `Column` 展示列表

### 19. 内存泄漏

**问题**: 应用占用内存持续增长

**解决方案**:
1. 确保在 ViewModel 中正确使用 `viewModelScope`
2. 避免在 Composable 中创建长期存活的协程
3. 及时取消不再需要的协程
4. 使用 LeakCanary 等工具检测泄漏

## 其他问题

### 20. IDE 索引缓慢

**问题**: IDEA 索引项目非常慢

**解决方案**:
1. 增加 IDE 内存分配
2. 排除不必要的目录（如 build 文件夹）
3. 禁用不需要的插件
4. 使用 "Invalidate Caches / Restart"

### 21. Git 忽略文件

**问题**: 哪些文件应该被 Git 忽略

**解决方案**:
确保 `.gitignore` 包含：
```
.gradle/
build/
.idea/
*.iml
.local/
```

### 22. 多平台构建失败

**问题**: 尝试添加其他平台（如 JS、Native）时失败

**解决方案**:
1. 确保所有依赖都支持目标平台
2. 检查平台特定的配置
3. 逐步添加平台，逐一解决兼容性问题

### 23. Gradle下载失败

**问题**: 项目一开始打开时，遇到下载gradle-xx-yy.zip失败

**解决方案**:
1. 将本地gradle-wrapper.properties文件的distributionUrl更改为腾讯或者阿里的库
2. 注意gradle-wrapper.properties文件存在distributionSha256Sum时，需要同步更改

## 获取帮助

如果以上方法都无法解决问题：

1. 查看官方文档：
   - [Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform.html)
   - [Compose Multiplatform](https://www.jetbrains.com/lp/compose-multiplatform/)

2. 检查项目示例：
   - 参考 dictionary 模块的实现

3. 搜索社区：
   - Kotlin Slack
   - Stack Overflow
   - JetBrains YouTrack

4. 提交 Issue：
   - 提供详细的错误信息
   - 附上相关的代码片段
   - 说明复现步骤
