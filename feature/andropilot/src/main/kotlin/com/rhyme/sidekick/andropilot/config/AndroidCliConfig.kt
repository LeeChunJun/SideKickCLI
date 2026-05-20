package com.rhyme.sidekick.andropilot.config

/**
 * Android CLI配置
 * 管理android.exe的路径和其他配置项
 */
object AndroidCliConfig {
    
    /**
     * android.exe的完整路径
     * 
     * 可以通过以下方式配置:
     * 1. 环境变量: ANDROID_CLI_PATH
     * 2. 系统属性: android.cli.path
     * 3. 默认路径: D:\AppsData\deploy\android-dev\scripts\android.exe
     */
    val androidExePath: String by lazy {
        // 优先级1: 环境变量
        System.getenv("ANDROID_CLI_PATH")
            // 优先级2: 系统属性
            ?: System.getProperty("android.cli.path")
            // 优先级3: 默认路径
            ?: "D:\\AppsData\\deploy\\android-dev\\scripts\\android.exe"
    }
    
    /**
     * 命令执行超时时间(毫秒)
     */
    const val COMMAND_TIMEOUT_MS = 30000L
    
    /**
     * 是否启用调试日志
     */
    const val DEBUG_MODE = false
}
