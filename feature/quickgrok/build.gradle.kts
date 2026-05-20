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
                implementation(project(":core:network"))
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
                implementation(compose.materialIconsExtended)
            }
        }
        val jvmTest by getting
    }
}
