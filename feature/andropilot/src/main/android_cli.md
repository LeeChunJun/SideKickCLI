# Android CLI 命令大全

本文档根据当前 `Android CLI 概览` 整理了可执行的 Android CLI 命令，并按功能分组。
所有命令均以 `android` 开头。

---

## 1. 通用全局选项

- `-h`, `--help`
  - 用法：`android <command> -h`
  - 说明：显示指定工具或命令的帮助信息。
  - 示例：`android -h`、`android create -h`

- `--sdk`
  - 用法：`android --sdk=<path-to-sdk> <command>`
  - 说明：指定要用于后续命令的 Android SDK 路径，临时替换默认 SDK。
  - 示例：`android --sdk=<path/to/sdk> sdk list`

---

## 2. 环境与初始化

- `init`
  - 用法：`android init`
  - 说明：为智能体安装 `android-cli` 技能，设置 Android CLI 环境。

- `info`
  - 用法：`android info`
  - 说明：显示当前默认 Android SDK 的路径。

- `update`
  - 用法：`android update`
  - 说明：更新 Android CLI 到最新版本。

---

## 3. 项目创建与描述

- `create`
  - 用法：`android create [--dry-run] [--verbose] [--name=<application-name>] [--output=<dest-path>] [<template-name>]`
  - 说明：从模板初始化新项目。
  - 关键参数：
    - `-o, --output`：目标项目目录路径。
    - `--dry-run`：模拟创建过程，不保存文件。
    - `--verbose`：启用详细输出。
    - `--name=<application-name>`：项目目录名称。
    - `<template-name>`：用于创建项目的模板名称，默认 `empty-activity-agp-9`。
  - 示例：`android create --dry-run --verbose empty-activity-agp-9`

- `create list`
  - 用法：`android create list`
  - 说明：列出所有可用于创建新项目的模板。

- `describe`
  - 用法：`android describe [--project_dir=<project-directory>]`
  - 说明：分析 Android 项目并生成描述性元数据，输出项目结构和构建工件位置。
  - 参数：
    - `--project_dir`：要描述的项目目录，省略时使用当前目录。
  - 示例：`android describe --project_dir=/path/to/your/project`

---

## 4. SDK 管理

- `sdk install`
  - 用法：`android sdk install <package[@version]> [--beta] [--canary] [--force]`
  - 说明：安装指定的 SDK 软件包。
  - 参数：
    - `package[@version]`：要安装的软件包列表，支持指定版本。
  - 选项：
    - `--beta`：包含 Beta 版软件包。
    - `--canary`：包含 Canary 版软件包。
    - `--force`：强制降级到旧版本。
  - 示例：
    - `android sdk install platforms/android-34 build-tools/34.0.0`
    - `android sdk install platforms/android-34@2`
    - `android sdk install --canary system-images/android-35/google_apis/x86_6`
    - `android sdk install --force platforms/android-33@1`

- `sdk list`
  - 用法：`android sdk list <package-pattern>`
  - 说明：列出已安装和可用的 SDK 软件包。
  - 参数：
    - `<package-pattern>`：按模式过滤软件包，支持正则表达式。
  - 选项：
    - `--all`：显示所有已安装和可用的软件包。
    - `--all-versions`：显示每个软件包的所有版本。
    - `--beta`：包含 Beta 版软件包。
    - `--canary`：包含 Canary 版软件包。

- `sdk remove`
  - 用法：`android sdk remove <package-name>`
  - 说明：从 SDK 中移除指定软件包。
  - 参数：
    - `<package-name>`：要移除的软件包名称。
  - 示例：`android sdk remove build-tools/36.1.0`

- `sdk update`
  - 用法：`android sdk update [--beta] [--canary] [<package-name>]`
  - 说明：更新一个或所有软件包到渠道中的最新版本。
  - 参数：
    - `<package-name>`：要更新的软件包名称。
  - 选项：
    - `--beta`：包含 Beta 版软件包。
    - `--canary`：包含 Canary 版软件包。
    - `--force`：强制降级到旧版本。

---

## 5. 模拟器管理

- `emulator create`
  - 用法：`android emulator create [--list-profiles] [--profile=<profile-name>]`
  - 说明：创建虚拟设备。
  - 选项：
    - `--list-profiles`：列出可用于创建设备的配置文件。
    - `--profile=<profile-name>`：使用指定配置文件创建设备，省略时创建 `medium_phone`。

- `emulator list`
  - 用法：`android emulator list`
  - 说明：列出可用的虚拟设备。

- `emulator start`
  - 用法：`android emulator start <device-name>`
  - 说明：启动指定虚拟设备。
  - 参数：
    - `<device-name>`：要启动的设备名称，例如 `medium_phone`。
  - 示例：`android emulator start medium_phone`

- `emulator stop`
  - 用法：`android emulator stop <device-serial-number>`
  - 说明：停止指定虚拟设备。
  - 参数：
    - `<device-serial-number>`：要停止的设备序列号。
  - 示例：`android emulator stop emulator-5554`

---

## 6. 设备部署与运行

- `run`
  - 用法：`android run [--debug] [--activity=<activity-name>] [--device=<serial-number>] [--type=<param>] --apks=<apk-paths>`
  - 说明：将应用部署到已连接设备或模拟器，需提供 APK 路径。
  - 参数：
    - `--apks`：要安装的 APK 文件路径列表，以英文逗号分隔。
  - 选项：
    - `--activity`：安装后要启动的 activity 名称。
    - `--debug`：以调试模式部署应用。
    - `--device`：目标设备或模拟器序列号。
    - `--type`：要启动的组件类型，支持 `ACTIVITY`、`WATCH_FACE`、`TILE`、`COMPLICATION`、`DECLARATIVE_WATCH_FACE`。
  - 示例：
    - `android run --apks=app/build/outputs/apk/debug/app-debug.apk`
    - `android run --apks=base.apk,density-hdpi.apk,lang-en.apk`
    - `android run --apks=app-debug.apk --type=SERVICE --activity=.sync.DataSyncService`
    - `android run --apks=app-debug.apk --device=emulator-5554`

---

## 7. 屏幕与 UI 交互

- `screen capture`
  - 用法：`android screen capture [--output] [--annotate]`
  - 说明：截取已连接设备的屏幕截图。
  - 选项：
    - `-o, --output`：保存屏幕截图的文件位置，省略时输出 PNG 数据到 stdout。
    - `-a, --annotate`：在检测到的界面元素周围绘制标签框。
  - 示例：`android screen capture --output=ui.png`

- `screen resolve`
  - 用法：`android screen resolve --screenshot=<path> --string=<string>`
  - 说明：将带注释的屏幕截图中的视觉标签转换为实际屏幕坐标。
  - 参数：
    - `--screenshot`：带注释屏幕截图的路径。
    - `--string`：包含 `#<number>` 占位符的字符串，将替换为实际坐标。
  - 示例：
    - `android screen resolve --screenshot=ui.png --string="input tap #5"`
    - 输出：`input tap 500 1000`

---

## 8. Android 技能管理

- `skills add`
  - 用法：`android skills add [--all] [--agent=<agent-name>] [--skill=<skill-name>]`
  - 说明：将 Android 技能安装到检测到的智能体目录中。
  - 选项：
    - `--all`：一次添加所有 Android 技能。
    - `--agent`：指定智能体列表，逗号分隔。
    - `--skill`：指定要安装的技能名称。
  - 示例：`android skills add --agent='gemini' edge-to-edge`

- `skills find`
  - 用法：`android skills find <string>`
  - 说明：查找与给定字符串匹配的技能。
  - 参数：
    - `string`：用于匹配技能说明的搜索字符串。
  - 示例：`android skills find 'performance'`

- `skills list`
  - 用法：`android skills list [--long]`
  - 说明：列出可用的技能。
  - 选项：
    - `--long`：显示每个技能的额外信息，包括说明和安装智能体。

- `skills remove`
  - 用法：`android skills remove [--agent] --skill=<skill-name>`
  - 说明：移除技能。
  - 参数：
    - `--skill`：要移除的技能名称。
  - 选项：
    - `--agent`：指定要移除技能的智能体列表。
  - 示例：`android skills remove --agent='gemini' --skill=edge-to-edge`

---

## 9. 文档与知识库访问

- `docs search`
  - 用法：`android docs search <query>`
  - 说明：搜索 Android 知识库中与查询相关的文档。

- `docs fetch`
  - 用法：`android docs fetch <kb-url>`
  - 说明：从知识库中获取指定文档并输出到终端。
  - 示例：
    - `android docs search 'How do I improve my app performance?'`
    - `android docs fetch kb://android/topic/performance/overview`

---

## 10. 特别说明

- `layout`
  - 用法：`android layout [--pretty] [--output] [--diff]`
  - 说明：以 JSON 格式返回当前活跃 Android 应用的界面布局。
  - 选项：
    - `-p, --pretty`：格式化输出以便阅读。
    - `-o, --output`：保存布局树到文件。
    - `-d, --diff`：仅返回自上次快照以来发生更改的布局元素。
  - 示例：`android layout --output=./hierarchy.json`

---

## 11. 其他说明

- 当前 Windows 上的 `android emulator` 命令已停用。
- 如果出现问题或希望反馈，请使用文档中提供的 `报告 bug` 方式。
