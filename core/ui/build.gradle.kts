plugins {
    // Apply the shared build logic from a convention plugin.
    // The shared code is located in `buildSrc/src/main/kotlin/kotlin-jvm.gradle.kts`.
    id("buildsrc.convention.kotlin-jvm")
    // Apply Kotlin Serialization plugin from `gradle/libs.versions.toml`.
    alias(libs.plugins.kotlinPluginSerialization)

    // mosaic 需要的Compose支持库
    alias(libs.plugins.kotlinCompose)
}

group = "com.rhyme.sidekick"
version = "0.0.1"

repositories {
    google()
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))

    // mosaic UI 库
    implementation(libs.mosaicRuntime)
    implementation(libs.mosaicAnimation)
    implementation(libs.mosaicTerminal)
    implementation(libs.mosaicTty)
    implementation(libs.mosaicTtyTerminal)

    // Apply the kotlinx bundle of dependencies from the version catalog (`gradle/libs.versions.toml`).
    implementation(libs.bundles.kotlinxEcosystem)
}