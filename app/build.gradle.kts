plugins {
    // Apply the shared build logic from a convention plugin.
    // The shared code is located in `buildSrc/src/main/kotlin/kotlin-jvm.gradle.kts`.
    id("buildsrc.convention.kotlin-jvm")

    // Apply the Application plugin to add support for building an executable JVM application.
    application
}

dependencies {
    // Project "app" depends on project "utils". (Project paths are separated with ":", so ":utils" refers to the top-level "utils" project.)
//    implementation(project(":feature:andropilot"))
    implementation(project(":feature:dictionary"))
    implementation(project(":feature:quickgrok"))
//    implementation(project(":feature:home"))
}

application {
    // Define the Fully Qualified Name for the application main class
    // (Note that Kotlin compiles `App.kt` to a class with FQN `com.example.app.AppKt`.)
    mainClass = "com.rhyme.sidekick.app.EntryKt"
    
    // 设置 UTF-8 编码以支持中文显示
    applicationDefaultJvmArgs = listOf(
        "-Dfile.encoding=UTF-8",
        "-Xmx512m"  // 最大堆内存 512MB。对于大型项目，可以增加 JVM 堆内存：
    )
}
