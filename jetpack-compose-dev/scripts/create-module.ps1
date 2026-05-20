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