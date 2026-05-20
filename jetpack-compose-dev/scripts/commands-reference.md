# 脚本命令参考

本文件包含创建和管理 Jetpack Compose 模块时常用的脚本命令。

## 模块创建脚本

### create-module.sh (Linux/Mac)

```bash
#!/bin/bash

# 检查参数
if [ -z "$1" ]; then
    echo "用法: ./create-module.sh <module-name>"
    echo "示例: ./create-module.sh calculator"
    exit 1
fi

MODULE_NAME=$1
MODULE_PATH="feature/$MODULE_NAME"
PACKAGE_PATH="com/rhyme/sidekick/$MODULE_NAME"

echo "创建模块: $MODULE_NAME"

# 创建目录结构
mkdir -p $MODULE_PATH/src/commonMain/kotlin/$PACKAGE_PATH/data/source
mkdir -p $MODULE_PATH/src/commonMain/kotlin/$PACKAGE_PATH/domain/model
mkdir -p $MODULE_PATH/src/commonMain/kotlin/$PACKAGE_PATH/domain/repository
mkdir -p $MODULE_PATH/src/commonMain/kotlin/$PACKAGE_PATH/domain/usecase
mkdir -p $MODULE_PATH/src/commonMain/kotlin/$PACKAGE_PATH/presentation
mkdir -p $MODULE_PATH/src/jvmMain/kotlin/$PACKAGE_PATH
mkdir -p $MODULE_PATH/src/jvmMain/kotlin/$PACKAGE_PATH/data
mkdir -p $MODULE_PATH/src/jvmMain/kotlin/$PACKAGE_PATH/presentation
mkdir -p $MODULE_PATH/src/commonTest/kotlin/$PACKAGE_PATH
mkdir -p $MODULE_PATH/src/jvmTest/kotlin/$PACKAGE_PATH

# 创建 build.gradle.kts
cat > $MODULE_PATH/build.gradle.kts << 'EOF'
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
EOF

# 添加到 settings.gradle.kts
if ! grep -q "include(\":feature:$MODULE_NAME\")" settings.gradle.kts; then
    echo "" >> settings.gradle.kts
    echo "include(\":feature:$MODULE_NAME\")" >> settings.gradle.kts
    echo "已添加到 settings.gradle.kts"
fi

echo "模块 $MODULE_NAME 创建完成！"
echo "目录: $MODULE_PATH"
echo ""
echo "下一步："
echo "1. 在 domain/model 中创建数据模型"
echo "2. 在 domain/repository 中定义仓库接口"
echo "3. 在 domain/usecase 中创建用例"
echo "4. 在 data/source 中实现数据源"
echo "5. 在 presentation 中创建 Contract 和 ViewModel"
echo "6. 在 jvmMain 中创建主应用入口"
echo ""
echo "运行模块: ./gradlew :feature:$MODULE_NAME:run"
```

### create-module.ps1 (Windows PowerShell)

```powershell
param(
    [Parameter(Mandatory=$true)]
    [string]$ModuleName
)

$ModulePath = "feature\$ModuleName"
$PackagePath = "com\rhyme\sidekick\$ModuleName"

Write-Host "创建模块: $ModuleName"

# 创建目录结构
New-Item -ItemType Directory -Force -Path "$ModulePath\src\commonMain\kotlin\$PackagePath\data\source"
New-Item -ItemType Directory -Force -Path "$ModulePath\src\commonMain\kotlin\$PackagePath\domain\model"
New-Item -ItemType Directory -Force -Path "$ModulePath\src\commonMain\kotlin\$PackagePath\domain\repository"
New-Item -ItemType Directory -Force -Path "$ModulePath\src\commonMain\kotlin\$PackagePath\domain\usecase"
New-Item -ItemType Directory -Force -Path "$ModulePath\src\commonMain\kotlin\$PackagePath\presentation"
New-Item -ItemType Directory -Force -Path "$ModulePath\src\jvmMain\kotlin\$PackagePath"
New-Item -ItemType Directory -Force -Path "$ModulePath\src\jvmMain\kotlin\$PackagePath\data"
New-Item -ItemType Directory -Force -Path "$ModulePath\src\jvmMain\kotlin\$PackagePath\presentation"
New-Item -ItemType Directory -Force -Path "$ModulePath\src\commonTest\kotlin\$PackagePath"
New-Item -ItemType Directory -Force -Path "$ModulePath\src\jvmTest\kotlin\$PackagePath"

# 创建 build.gradle.kts
$BuildGradle = @'
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
'@

Set-Content -Path "$ModulePath\build.gradle.kts" -Value $BuildGradle

# 添加到 settings.gradle.kts
$SettingsFile = "settings.gradle.kts"
$IncludeStatement = "include(`":feature:$ModuleName`")"

if (!(Select-String -Path $SettingsFile -Pattern $IncludeStatement -Quiet)) {
    Add-Content -Path $SettingsFile -Value ""
    Add-Content -Path $SettingsFile -Value $IncludeStatement
    Write-Host "已添加到 settings.gradle.kts"
}

Write-Host "模块 $ModuleName 创建完成！"
Write-Host "目录: $ModulePath"
Write-Host ""
Write-Host "下一步："
Write-Host "1. 在 domain/model 中创建数据模型"
Write-Host "2. 在 domain/repository 中定义仓库接口"
Write-Host "3. 在 domain/usecase 中创建用例"
Write-Host "4. 在 data/source 中实现数据源"
Write-Host "5. 在 presentation 中创建 Contract 和 ViewModel"
Write-Host "6. 在 jvmMain 中创建主应用入口"
Write-Host ""
Write-Host "运行模块: .\gradlew :feature:$ModuleName`:run"
```

## 常用 Gradle 命令

### 构建相关

```bash
# 清理构建
./gradlew clean

# 构建所有模块
./gradlew build

# 构建特定模块
./gradlew :feature:module-name:build

# 跳过测试构建
./gradlew build -x test
```

### 运行相关

```bash
# 运行模块
./gradlew :feature:module-name:run

# 以调试模式运行
./gradlew :feature:module-name:run --debug-jvm
```

### 测试相关

```bash
# 运行所有测试
./gradlew test

# 运行特定模块的测试
./gradlew :feature:module-name:test

# 运行 JVM 测试
./gradlew :feature:module-name:jvmTest

# 运行通用测试
./gradlew :feature:module-name:commonTest

# 查看测试报告
# 报告位于: feature/module-name/build/reports/tests/
```

### 依赖相关

```bash
# 查看依赖树
./gradlew :feature:module-name:dependencies

# 查看特定配置的依赖
./gradlew :feature:module-name:dependencies --configuration jvmRuntimeClasspath

# 刷新依赖
./gradlew --refresh-dependencies

# 诊断依赖冲突
./gradlew :feature:module-name:dependencyInsight --dependency <dependency-name>
```

### 任务相关

```bash
# 列出所有任务
./gradlew tasks

# 列出特定模块的任务
./gradlew :feature:module-name:tasks

# 查看任务详情
./gradlew help --task :feature:module-name:run
```

## 实用工具脚本

### check-module.sh - 检查模块结构

```bash
#!/bin/bash

if [ -z "$1" ]; then
    echo "用法: ./check-module.sh <module-name>"
    exit 1
fi

MODULE_PATH="feature/$1"

echo "检查模块: $1"
echo "========================"

# 检查必要文件
FILES=(
    "$MODULE_PATH/build.gradle.kts"
    "$MODULE_PATH/src/commonMain"
    "$MODULE_PATH/src/jvmMain"
)

for file in "${FILES[@]}"; do
    if [ -e "$file" ]; then
        echo "✓ $file 存在"
    else
        echo "✗ $file 缺失"
    fi
done

echo ""
echo "目录结构:"
tree -L 3 "$MODULE_PATH/src" 2>/dev/null || find "$MODULE_PATH/src" -type d | head -20
```

### run-all-modules.sh - 运行所有模块

```bash
#!/bin/bash

echo "查找所有 feature 模块..."
echo ""

for dir in feature/*/; do
    if [ -f "$dir/build.gradle.kts" ]; then
        MODULE_NAME=$(basename "$dir")
        echo "模块: $MODULE_NAME"
        
        # 检查是否有 main 函数
        if grep -r "fun main()" "$dir/src/jvmMain" >/dev/null 2>&1; then
            echo "  ✓ 有主函数"
            echo "  运行: ./gradlew :feature:$MODULE_NAME:run"
        else
            echo "  ✗ 没有主函数"
        fi
        echo ""
    fi
done
```

## Git 相关命令

### 初始化新模块到 Git

```bash
# 添加新模块文件
git add feature/module-name/

# 更新 settings.gradle.kts
git add settings.gradle.kts

# 提交
git commit -m "Add new module: module-name"
```

### 忽略构建产物

确保 `.gitignore` 包含：

```gitignore
# Gradle
.gradle/
*/build/
!/.gradle

# IDE
.idea/
*.iml
*.iws
*.ipr

# OS
.DS_Store
Thumbs.db

# Local configurations
local.properties
```

## 故障排查命令

### 查看编译错误详情

```bash
# 查看详细日志
./gradlew :feature:module-name:build --stacktrace

# 查看更详细的日志
./gradlew :feature:module-name:build --info

# 查看调试日志
./gradlew :feature:module-name:build --debug
```

### 清理并重新构建

```bash
# 完全清理
./gradlew clean

# 删除构建缓存
rm -rf .gradle/buildOutputCleanup/

# 重新构建
./gradlew build
```

### 检查 Kotlin 版本

```bash
# 查看项目中使用的 Kotlin 版本
./gradlew :feature:module-name:properties | grep kotlin
```

## CI/CD 相关命令

### 本地模拟 CI 构建

```bash
# 清理、构建、测试（模拟 CI 流程）
./gradlew clean build test

# 生成覆盖率报告（如果配置了 JaCoCo）
./gradlew jacocoTestReport
```

### 发布相关

```bash
# 发布到本地 Maven
./gradlew :feature:module-name:publishToMavenLocal

# 查看所有发布任务
./gradlew tasks --group=publishing
```

## 性能优化命令

### 分析构建性能

```bash
# 生成构建分析报告
./gradlew :feature:module-name:build --profile

# 查看报告
# 报告位于: build/reports/profile/
```

### 启用构建缓存

在 `gradle.properties` 中添加：

```properties
org.gradle.caching=true
org.gradle.parallel=true
org.gradle.configureondemand=true
org.gradle.jvmargs=-Xmx2g -XX:MaxMetaspaceSize=512m
```

## 环境变量设置

### 设置 Gradle 属性

```bash
# Linux/Mac
export GRADLE_OPTS="-Xmx2g -XX:MaxMetaspaceSize=512m"

# Windows PowerShell
$env:GRADLE_OPTS="-Xmx2g -XX:MaxMetaspaceSize=512m"
```

### 使用特定 JDK

```bash
# 设置 JAVA_HOME
export JAVA_HOME=/path/to/jdk-21

# 验证
java -version
```

## 快速参考卡片

```
┌─────────────────────────────────────────────┐
│          常用命令快速参考                      │
├─────────────────────────────────────────────┤
│ 创建模块:   ./create-module.sh <name>        │
│ 构建模块:   ./gradlew :feature:<name>:build  │
│ 运行模块:   ./gradlew :feature:<name>:run    │
│ 测试模块:   ./gradlew :feature:<name>:test   │
│ 清理:       ./gradlew clean                  │
│ 查看依赖:   ./gradlew :f:<n>:dependencies    │
│ 完整构建:   ./gradlew clean build test       │
└─────────────────────────────────────────────┘
```

## 提示和技巧

1. **使用 Tab 补全**: Gradle 支持任务名称自动补全
2. **并行构建**: 使用 `-parallel` 标志加速多模块构建
3. **持续构建**: 使用 `--continuous` 或 `-t` 进行监听式构建
4. **干运行**: 使用 `--dry-run` 预览将要执行的任务
5. **排除任务**: 使用 `-x <task>` 跳过特定任务

示例：

```bash
# 持续构建（文件变化时自动重新构建）
./gradlew :feature:module-name:build -t

# 并行构建
./gradlew build --parallel

# 干运行
./gradlew :feature:module-name:run --dry-run
```