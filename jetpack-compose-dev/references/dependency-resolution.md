# 依赖库版本参考

## Kotlin 生态系统

### 核心 Kotlin
- **Kotlin**: 2.1.0
- **Kotlin Reflect**: 2.2.21

### Kotlinx 库
- **kotlinx-datetime**: 0.6.1 - 日期时间处理
- **kotlinx-serialization-json**: 1.7.3 - JSON 序列化
- **kotlinx-coroutines-core**: 1.9.0 - 协程支持

这些库被打包为 `libs.bundles.kotlinxEcosystem`，可以在 build.gradle.kts 中一次性引入。

## Compose Multiplatform

### Compose BOM
- **Compose BOM**: 1.9.3

### Compose 组件
- **compose-material3**: Material Design 3 组件库
- **compose-desktop-current-jvm**: 当前平台的 Compose Desktop 实现

### Compose 插件
- **org.jetbrains.compose**: 1.9.3 - JetBrains Compose Multiplatform Gradle 插件
- **org.jetbrains.kotlin.plugin.compose**: 2.1.0 - Kotlin Compose 编译器插件

## Mosaic（终端 UI）

如果需要创建终端界面应用：

- **mosaic-runtime**: 0.18.0
- **mosaic-animation**: 0.18.0
- **mosaic-terminal**: 0.18.0
- **mosaic-tty**: 0.18.0
- **mosaic-tty-terminal**: 0.18.0
- **mosaic-testing**: 0.18.0

**注意**: Mosaic 0.18.0 对 `CompositionLocalProvider` 的 vararg 参数有限制，使用时需注意兼容性。

## 网络请求

### OkHttp
- **okhttp**: 4.12.0 - HTTP 客户端

### Retrofit
- **retrofit**: 2.9.0 - REST API 客户端
- **retrofit-converter-gson**: 2.9.0 - Gson 转换器

### Gson
- **gson**: 2.10.1 - JSON 解析库

## 日志

### Log4j2
- **log4j-api**: 2.20.0
- **log4j-core**: 2.20.0

这些库被打包为 `libs.bundles.log4jEcosystem`。

## 数据库

### SQLite
- **sqlite-jdbc**: 3.50.3.0 - SQLite JDBC 驱动

## Gradle 插件

### Kotlin 插件
- **kotlin-multiplatform**: 2.1.0
- **kotlin-plugin-serialization**: 2.1.0

### 其他插件
- **org.gradle.toolchains.foojay-resolver-convention**: 0.8.0 - JDK 自动下载

## 自定义模块依赖

项目内部的核心模块：

- **:core:network** - 网络请求封装
- **:core:cmdx** - CLI ViewModel 和生命周期管理
- **:core:ui** - 通用 UI 组件
- **:core:logger** - 日志工具
- **:core:util** - 通用工具类

## 添加新依赖的步骤

1. 在 `gradle/libs.versions.toml` 的 `[versions]` 部分添加版本号：
   ```toml
   [versions]
   myLibrary = "1.0.0"
   ```

2. 在 `[libraries]` 部分声明库：
   ```toml
   [libraries]
   myLibrary = { module = "com.example:my-library", version.ref = "myLibrary" }
   ```

3. （可选）在 `[bundles]` 部分创建依赖包：
   ```toml
   [bundles]
   myBundle = ["myLibrary", "anotherLibrary"]
   ```

4. 在模块的 `build.gradle.kts` 中使用：
   ```kotlin
   dependencies {
       implementation(libs.myLibrary)
       // 或
       implementation(libs.bundles.myBundle)
   }
   ```

## 依赖冲突解决

如果遇到依赖冲突：

1. 检查 `libs.versions.toml` 中的版本定义
2. 使用 `./gradlew :module:dependencies` 查看依赖树
3. 使用 `exclude` 排除冲突的依赖：
   ```kotlin
   implementation(libs.someLibrary) {
       exclude(group = "conflicting-group", module = "conflicting-module")
   }
   ```

## 平台特定依赖

对于仅在某些平台需要的依赖：

```kotlin
kotlin {
    sourceSets {
        val jvmMain by getting {
            dependencies {
                // 仅 JVM 平台需要
                implementation("some.jvm.library")
            }
        }
        
        val commonMain by getting {
            dependencies {
                // 所有平台都需要
                implementation("common.library")
            }
        }
    }
}
```