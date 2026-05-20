package com.rhyme.sidekick.logger

/**
 * 项目名：SideKickCLI
 * 包名：com.rhyme.sidekick.logger
 * 描述：日志级别
 *
 * @author 李春俊-Rhyme
 * @since 15:44 2026/4/23 星期四
 * @version 1.0
 */
enum class LogLevel(val priority: Int, val label: String) {
    VERBOSE(0, "VERBOSE"),
    DEBUG(1, "DEBUG"),
    INFO(2, "INFO"),
    WARN(3, "WARN"),
    ERROR(4, "ERROR")
}