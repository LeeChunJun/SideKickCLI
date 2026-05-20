@file:Suppress("EXTENSION_SHADOWED_BY_MEMBER")

package com.rhyme.sidekick.logger

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.apache.logging.log4j.Marker
import org.apache.logging.log4j.MarkerManager
import org.apache.logging.log4j.message.MessageFactory
import org.apache.logging.log4j.message.ParameterizedMessageFactory
import org.apache.logging.log4j.ThreadContext
import java.util.concurrent.ConcurrentHashMap

/**
 * 项目名：SideKickCLI
 * 包名：com.rhyme.sidekick.logger
 * 描述：日志工具类（封装 Log4j 2 提供统一日志接口）
 *
 * @author 李春俊-Rhyme
 * @since 15:40 2026/4/23 星期四
 * @version 1.0
 */
object LogUtil {

    // 默认的 MessageFactory
    private val defaultMessageFactory: MessageFactory = ParameterizedMessageFactory.INSTANCE

    // 缓存 Logger 实例
    private val loggers = ConcurrentHashMap<String, Logger>()

    // 缓存 Marker
    private val markers = ConcurrentHashMap<String, Marker>()

    /**
     * 获取或创建 Logger（内部使用）
     */
    internal fun getLogger(clazz: Class<*>): Logger {
        return getLogger(clazz.name)
    }

    internal fun getLogger(name: String): Logger {
        return loggers.computeIfAbsent(name) {
            LogManager.getLogger(name, defaultMessageFactory)
        }
    }

    /**
     * 获取或创建 Marker
     */
    internal fun getMarker(name: String): Marker {
        return markers.computeIfAbsent(name) {
            MarkerManager.getMarker(name)
        }
    }

    /**
     * 内部日志记录方法
     */
    internal fun logInternal(logger: Logger, level: LogLevel, message: String, vararg args: Any?) {
        when (level) {
            LogLevel.VERBOSE -> logger.trace(message, *args)
            LogLevel.DEBUG -> logger.debug(message, *args)
            LogLevel.INFO -> logger.info(message, *args)
            LogLevel.WARN -> logger.warn(message, *args)
            LogLevel.ERROR -> logger.error(message, *args)
        }
    }

    internal fun logInternal(logger: Logger, level: LogLevel, message: String, throwable: Throwable) {
        when (level) {
            LogLevel.VERBOSE -> logger.trace(message, throwable)
            LogLevel.DEBUG -> logger.debug(message, throwable)
            LogLevel.INFO -> logger.info(message, throwable)
            LogLevel.WARN -> logger.warn(message, throwable)
            LogLevel.ERROR -> logger.error(message, throwable)
        }
    }

    internal fun logInternal(logger: Logger, level: LogLevel, message: String, marker: Marker, vararg args: Any?) {
        when (level) {
            LogLevel.VERBOSE -> logger.trace(marker, message, *args)
            LogLevel.DEBUG -> logger.debug(marker, message, *args)
            LogLevel.INFO -> logger.info(marker, message, *args)
            LogLevel.WARN -> logger.warn(marker, message, *args)
            LogLevel.ERROR -> logger.error(marker, message, *args)
        }
    }

    /**
     * 线程上下文操作
     */
    fun putThreadContext(key: String, value: String) {
        ThreadContext.put(key, value)
    }

    fun getThreadContext(key: String): String? {
        return ThreadContext.get(key)
    }

    fun removeThreadContext(key: String) {
        ThreadContext.remove(key)
    }

    fun clearThreadContext() {
        ThreadContext.clearAll()
    }

    /**
     * 使用 MDC (Mapped Diagnostic Context) 简化版本
     */
    fun withMDC(context: Map<String, String>, block: () -> Unit) {
        val oldContext = ThreadContext.getImmutableContext()
        ThreadContext.clearMap()
        context.forEach { (key, value) ->
            ThreadContext.put(key, value)
        }
        try {
            block()
        } finally {
            ThreadContext.clearMap()
            oldContext.forEach { (key, value) ->
                ThreadContext.put(key, value)
            }
        }
    }
}

/**
 * 日志记录器接口 - 对外提供的统一日志接口
 */
interface AppLogger {
    fun verbose(message: String, vararg args: Any?)
    fun verbose(message: String, throwable: Throwable)
    fun verbose(marker: Marker, message: String, vararg args: Any?)

    fun debug(message: String, vararg args: Any?)
    fun debug(message: String, throwable: Throwable)
    fun debug(marker: Marker, message: String, vararg args: Any?)

    fun info(message: String, vararg args: Any?)
    fun info(message: String, throwable: Throwable)
    fun info(marker: Marker, message: String, vararg args: Any?)

    fun warn(message: String, vararg args: Any?)
    fun warn(message: String, throwable: Throwable)
    fun warn(marker: Marker, message: String, vararg args: Any?)

    fun error(message: String, vararg args: Any?)
    fun error(message: String, throwable: Throwable)
    fun error(marker: Marker, message: String, vararg args: Any?)

    fun log(level: LogLevel, message: String, vararg args: Any?)
    fun log(level: LogLevel, message: String, throwable: Throwable)
    fun log(level: LogLevel, message: String, marker: Marker, vararg args: Any?)
}

/**
 * Logger 实现类（内部使用）
 */
private class AppLoggerImpl(
    private val name: String,
    private val logger: Logger
) : AppLogger {

    override fun verbose(message: String, vararg args: Any?) {
        LogUtil.logInternal(logger, LogLevel.VERBOSE, "[$name] $message", *args)
    }

    override fun verbose(message: String, throwable: Throwable) {
        LogUtil.logInternal(logger, LogLevel.VERBOSE, "[$name] $message", throwable)
    }

    override fun verbose(marker: Marker, message: String, vararg args: Any?) {
        LogUtil.logInternal(logger, LogLevel.VERBOSE, "[$name] $message", marker, *args)
    }

    override fun debug(message: String, vararg args: Any?) {
        LogUtil.logInternal(logger, LogLevel.DEBUG, "[$name] $message", *args)
    }

    override fun debug(message: String, throwable: Throwable) {
        LogUtil.logInternal(logger, LogLevel.DEBUG, "[$name] $message", throwable)
    }

    override fun debug(marker: Marker, message: String, vararg args: Any?) {
        LogUtil.logInternal(logger, LogLevel.DEBUG, "[$name] $message", marker, *args)
    }

    override fun info(message: String, vararg args: Any?) {
        LogUtil.logInternal(logger, LogLevel.INFO, "[$name] $message", *args)
    }

    override fun info(message: String, throwable: Throwable) {
        LogUtil.logInternal(logger, LogLevel.INFO, "[$name] $message", throwable)
    }

    override fun info(marker: Marker, message: String, vararg args: Any?) {
        LogUtil.logInternal(logger, LogLevel.INFO, "[$name] $message", marker, *args)
    }

    override fun warn(message: String, vararg args: Any?) {
        LogUtil.logInternal(logger, LogLevel.WARN, "[$name] $message", *args)
    }

    override fun warn(message: String, throwable: Throwable) {
        LogUtil.logInternal(logger, LogLevel.WARN, "[$name] $message", throwable)
    }

    override fun warn(marker: Marker, message: String, vararg args: Any?) {
        LogUtil.logInternal(logger, LogLevel.WARN, "[$name] $message", marker, *args)
    }

    override fun error(message: String, vararg args: Any?) {
        LogUtil.logInternal(logger, LogLevel.ERROR, "[$name] $message", *args)
    }

    override fun error(message: String, throwable: Throwable) {
        LogUtil.logInternal(logger, LogLevel.ERROR, "[$name] $message", throwable)
    }

    override fun error(marker: Marker, message: String, vararg args: Any?) {
        LogUtil.logInternal(logger, LogLevel.ERROR, "[$name] $message", marker, *args)
    }

    override fun log(level: LogLevel, message: String, vararg args: Any?) {
        LogUtil.logInternal(logger, level, "[$name] $message", *args)
    }

    override fun log(level: LogLevel, message: String, throwable: Throwable) {
        LogUtil.logInternal(logger, level, "[$name] $message", throwable)
    }

    override fun log(level: LogLevel, message: String, marker: Marker, vararg args: Any?) {
        LogUtil.logInternal(logger, level, "[$name] $message", marker, *args)
    }
}

/**
 * 扩展函数简化使用
 */
fun Class<*>.getLogger(): AppLogger {
    return AppLoggerImpl(this.name, LogUtil.getLogger(this))
}

fun Any.getLogger(): AppLogger {
    return this.javaClass.getLogger()
}

fun String.getLogger(): AppLogger {
    val logger = LogUtil.getLogger(this)
    return AppLoggerImpl(this, logger)
}