---
name: jetpack-compose-dev
description: Jetpack Compose 项目创建技能。
  本技能提供了一套标准化的方法来快速创建基于 Kotlin Multiplatform 和 Jetpack Compose 的项目模块。该模板遵循 Clean Architecture 原则，采用 MVI (Model-View-Intent) 架构模式，确保代码的可维护性和可扩展性。
---

# Jetpack Compose Dev Skill
# updated: 2026-Q2
# stack: Kotlin/Compose/MVI

## 使用场景

当你需要：
1. 在现有项目中添加一个新的功能模块
2. 创建具有 UI 界面的新功能
3. 构建跨平台的桌面应用程序模块
4. 实现 MVVM/MVI 架构的组件

## 快速开始

### 1. 创建新模块目录结构

```bash
mkdir -p feature/{module_name}/src/{commonMain,jvmMain}/{kotlin,resources}
mkdir -p feature/{module_name}/src/commonMain/kotlin/com/rhyme/sidekick/{module_name}/{data,domain,presentation}
mkdir -p feature/{module_name}/src/jvmMain/kotlin/com/rhyme/sidekick/{module_name}/{data,presentation}
```

### 2. 创建 build.gradle.kts 文件

参考 [build.gradle.kts](references/build-configuration.md) 中的配置模板。

### 3. 更新 settings.gradle.kts

在根目录的 `settings.gradle.kts` 文件中添加新模块：

```kotlin
include(":feature:{module_name}")
```

### 4. 实现核心组件

按照以下顺序实现各个组件：
1. Domain 层：数据模型、仓库接口、用例
2. Data 层：数据源实现
3. Presentation 层：ViewModel 和 UI 状态
4. JVM 主入口：应用启动代码

## 架构说明

本模板采用 Clean Architecture + MVI 架构：

- **Domain Layer**: 包含业务逻辑、实体和用例
- **Data Layer**: 实现数据访问和存储
- **Presentation Layer**: 处理 UI 状态和用户交互
- **UI Layer**: Jetpack Compose 界面实现

详细架构说明请参考 [architecture.md](references/architecture.md)

## 代码模板

核心代码模板请参考 [code-templates.md](references/code-templates.md)

## 依赖管理

所有依赖版本都在根目录的 `gradle/libs.versions.toml` 中统一管理。新增依赖时请在此文件中声明版本。

依赖库版本号及其冲突解决请参考 [dependency-resolution.md](references/dependency-resolution.md)

## 脚本命令

创建模块脚本：
脚本命令参考 [commands-reference.md](scripts/commands-reference.md)
Windows 平台请执行 [create-module.ps1](scripts/create-module.ps1)
Mac/Linux 平台请执行 [create-module.ps1](scripts/create-module.sh)

## 运行和测试

### 运行模块
```bash
./gradlew :feature:{module_name}:run
```

### 运行测试
```bash
./gradlew :feature:{module_name}:test
```

## 注意事项

1. 保持各层之间的清晰边界
2. 使用协程进行异步操作
3. 遵循不可变数据原则
4. 合理使用 StateFlow 管理状态
5. 确保平台特定代码正确分离

## 故障排除

如果遇到问题，请参考：
- [常见问题解答](references/troubleshooting.md)
- [依赖冲突解决](references/dependency-resolution.md)
- [快速上手指南](references/quick-start.md)

## 示例参考

完整的示例实现可以参考项目中的 dictionary 模块：
- [Dictionary Module](../../feature/dictionary/)