# 构建配置参考

## build.gradle.kts 模板

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
                // 添加通用依赖
                implementation(project(":core:network"))  // 如果需要网络功能
                implementation(project(":core:cmdx"))     // 如果需要 ViewModel 支持
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

## 插件说明

- `buildsrc.convention.kotlin-multiplatform`: 自定义的多平台 Kotlin 插件
- `kotlinPluginSerialization`: Kotlin 序列化支持
- `composePlugin`: JetBrains Compose Multiplatform 插件
- `kotlinCompose`: Kotlin Compose 编译器插件

## 依赖项说明

### 核心依赖
- `project(":core:network")`: 网络请求相关功能
- `project(":core:cmdx")`: CLI ViewModel 支持和生命周期管理
- `libs.bundles.kotlinxEcosystem`: Kotlin 生态系统库（日期时间、序列化、协程）

### UI 依赖
- `compose.desktop.currentOs`: 当前操作系统的 Compose Desktop 实现
- `compose.material3`: Material Design 3 组件库

## 版本目录引用

所有依赖版本都在根目录的 `gradle/libs.versions.toml` 文件中定义：

```toml
[versions]
kotlin = "2.1.0"
composeBom = "1.9.3"
# ... 其他版本定义

[libraries]
kotlinxCoroutines = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-core", version.ref = "kotlinxCoroutines" }
composeMaterial3 = { group = "org.jetbrains.compose.material3", name = "material3", version.ref = "composeBom" }
# ... 其他库定义

[bundles]
kotlinxEcosystem = ["kotlinxDatetime", "kotlinxSerialization", "kotlinxCoroutines"]
```