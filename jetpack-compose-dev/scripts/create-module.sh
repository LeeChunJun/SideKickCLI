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