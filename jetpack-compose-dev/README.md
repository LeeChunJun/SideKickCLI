# Jetpack Compose 项目创建技能包

## 📦 技能包结构

```
jetpack-compose-dev/
├── SKILL.md                      # 主技能文档（AI Agent 使用指南）
├── README.md                     # 本文件（人类阅读）
├── scripts/                      # 自动化脚本
│   ├── create-module.sh         # Linux/Mac 模块创建脚本
│   ├── create-module.ps1        # Windows PowerShell 模块创建脚本
│   └── commands-reference.md    # 命令参考文档
├── references/                   # 详细参考文档
│   ├── architecture.md          # 架构设计说明
│   ├── build-configuration.md   # 构建配置模板
│   ├── code-templates.md        # 代码模板
│   ├── dependency-resolution.md # 依赖库版本说明及冲突解决
│   ├── quick-start.md           # 5分钟快速上手
│   └── troubleshooting.md       # 常见问题解答
└── assets/                       # 资源文件（图片、音频等）
    └── (预留，可按需添加)
```

## 🎯 适用人群

- **AI Agent**: 通过 `SKILL.md` 了解如何快速创建 Jetpack Compose 模块
- **开发者**: 通过 `references/` 中的文档学习架构和最佳实践
- **初学者**: 通过 `quick-start.md` 快速入门

## 🚀 快速开始

### 方式一：使用自动化脚本（推荐）

**Linux/Mac:**
```bash
cd jetpack-compose-dev/scripts
chmod +x create-module.sh
./create-module.sh my-module-name
```

**Windows PowerShell:**
```powershell
cd jetpack-compose-dev\scripts
.\create-module.ps1 -ModuleName my-module-name
```

### 方式二：手动创建

参考 `references/quick-start.md` 中的详细步骤。

## 📚 文档导航

### 核心文档

1. **[SKILL.md](SKILL.md)** - AI Agent 的主要参考文档
   - 技能概述和使用场景
   - 快速开始指南
   - 架构说明
   - 运行和测试方法

2. **[快速上手](references/quick-start.md)** - 5分钟创建可运行模块
   - 完整的示例代码
   - 逐步指导
   - 计算器应用实例

### 技术文档

1. **[架构设计](references/architecture.md)** - Clean Architecture + MVI
   - 分层架构详解
   - 数据流说明
   - 设计原则

2. **[构建配置](references/build-configuration.md)** - Gradle 配置模板
   - build.gradle.kts 模板
   - 插件说明
   - 依赖管理

3. **[代码模板](references/code-templates.md)** - 各层代码示例
   - Domain 层模板
   - Data 层模板
   - Presentation 层模板
   - UI 层模板

4. **[依赖管理](references/dependency-resolution.md)** - 版本和依赖说明
   - Kotlin 生态系统
   - Compose Multiplatform
   - 网络、日志、数据库库
   - 添加新依赖的步骤

### 辅助文档

1. **[命令参考](scripts/commands-reference.md)** - 常用命令速查
   - Gradle 命令
   - Git 命令
   - 故障排查命令

2. **[常见问题](references/troubleshooting.md)** - 问题解决方案
   - 构建问题
   - Compose 问题
   - ViewModel 问题
   - 性能问题

3. **[依赖冲突解决](references/dependency-resolution.md)** - 依赖问题处理
   - 版本管理
   - 冲突检测
   - 解决方案

## 💡 使用场景

### 场景 1: AI Agent 自动创建模块

AI Agent 读取 `SKILL.md`，根据用户需求自动生成：
- 完整的目录结构
- 配置文件
- 基础代码框架
- 示例实现

### 场景 2: 开发者学习架构

开发者阅读 `references/architecture.md` 和 `references/code-templates.md`，理解：
- Clean Architecture 原则
- MVI 模式实现
- 最佳实践

### 场景 3: 快速原型开发

使用 `scripts/create-module.sh` 快速搭建模块框架，然后：
- 填充业务逻辑
- 实现 UI 界面
- 运行和测试

## 🏗️ 架构概览

本项目采用 **Clean Architecture + MVI** 架构：

```
┌─────────────────────┐
│   UI Layer          │  ← Jetpack Compose
│   (jvmMain)         │
├─────────────────────┤
│ Presentation Layer  │  ← ViewModel, State, Events
│   (commonMain)      │
├─────────────────────┤
│   Domain Layer      │  ← UseCases, Repositories, Models
│   (commonMain)      │
├─────────────────────┤
│    Data Layer       │  ← DataSources, Implementations
│   (common+jvmMain)  │
└─────────────────────┘
```

### 关键特性

- ✅ **跨平台支持**: Kotlin Multiplatform
- ✅ **响应式 UI**: Jetpack Compose Desktop
- ✅ **状态管理**: MVI 模式 + StateFlow
- ✅ **协程支持**: Kotlin Coroutines
- ✅ **依赖注入**: 构造函数注入
- ✅ **可测试性**: 清晰的分层边界

## 📋 前置要求

- **JDK**: 21 或更高版本
- **Gradle**: 8.0+（项目自带 Gradle Wrapper）
- **IDE**: IntelliJ IDEA（推荐）或 Android Studio
- **Kotlin**: 2.1.0

## 🔧 配置 IDE

1. 打开项目根目录
2. IDEA 会自动识别 Gradle 项目
3. 等待 Gradle 同步完成
4. 确保 JDK 21 已配置

## 📝 创建新模块的步骤

1. **确定模块名称**（小写，如 `calculator`）
2. **运行创建脚本**或手动创建目录结构
3. **更新 settings.gradle.kts** 注册模块
4. **实现 Domain 层**（Models, Repositories, UseCases）
5. **实现 Data 层**（DataSources）
6. **实现 Presentation 层**（Contract, ViewModel）
7. **实现 UI 层**（Composable Functions, Main）
8. **运行测试**: `./gradlew :feature:module-name:test`
9. **运行应用**: `./gradlew :feature:module-name:run`

## 🎓 学习路径

### 初级
1. 阅读 [快速上手](references/quick-start.md)
2. 运行创建脚本生成示例模块
3. 修改代码观察效果
4. 阅读 [代码模板](references/code-templates.md)

### 中级
1. 深入理解 [架构设计](references/architecture.md)
2. 学习 [依赖管理](references/dependency-resolution.md)
3. 参考 dictionary 模块的完整实现
4. 编写单元测试

### 高级
1. 优化性能和用户体验
2. 添加更多功能模块
3. 实现复杂的业务逻辑
4. 贡献最佳实践到文档

## 📖 示例模块

项目中已有的示例模块：

- **dictionary**: 词典查询应用
  - 位置: `feature/dictionary/`
  - 功能: 搜索单词、显示释义
  - 特点: 完整的 MVVM 实现，网络请求

参考这些模块学习实际项目的组织方式。

## 🤝 贡献指南

如果你改进了这个技能包：

1. 更新相关文档
2. 添加示例代码
3. 补充常见问题
4. 提交 Pull Request

## 📞 获取帮助

遇到问题？

1. 查看 [常见问题](references/troubleshooting.md)
2. 检查 [命令参考](scripts/commands-reference.md)
3. 参考 dictionary 模块实现
4. 查阅官方文档：
   - [Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform.html)
   - [Compose Multiplatform](https://www.jetbrains.com/lp/compose-multiplatform/)

## 📄 许可证

本技能包遵循项目的许可证协议。

## 🎉 开始使用

现在就创建一个新模块吧：

```bash
./scripts/create-module.sh my-awesome-module
```

祝你编码愉快！🚀