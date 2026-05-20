package com.rhyme.sidekick.cmdx.framework

import java.nio.file.Path

/**
 * 项目名：SideKickCLI
 * 包名：com.rhyme.sidekick.framework
 * 描述：App 资源
 *
 * 提供与 Android 类似的资源访问能力：字符串、图标、颜色、文件访问以及多语言切换。
 * 支持多种图标风格（classic、modern、minimal）和多语言（中文、繁体、英文）。
 *
 * 使用示例（类似Android的使用方式）：
 * - getString("app.name") - 获取字符串资源
 * - getIcon("menu.home") - 获取图标资源（使用当前图标风格）
 * - getIcon("action.save", "classic") - 获取特定风格的图标
 * - getProperty("icons.menu.home") - 获取图标
 * - getProperty("app.name") - 获取字符串
 * - getFile("config/settings.json") - 获取资源文件
 *
 * @author 李春俊-Rhyme
 * @since 15:50 2026/4/23 星期四
 * @version 2.0
 */
interface CLIResources {
    // 字符串资源
    fun getString(key: String): String

    // 图标资源
    fun getIcon(key: String): String
    fun getIcon(key: String, iconStyle: String): String

    // 属性资源
    fun getProperty(key: String): String
    fun getFile(path: String): Path

    // 语言相关
    fun setLanguage(language: String)
    fun getSupportedLanguages(): Set<String>
    fun getCurrentLanguage(): String

    // 图标风格相关
    fun setIconStyle(style: String)
    fun getSupportedIconStyles(): Set<String>
    fun getCurrentIconStyle(): String
}