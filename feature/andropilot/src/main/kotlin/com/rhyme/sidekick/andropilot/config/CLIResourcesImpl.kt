package com.rhyme.sidekick.andropilot.config

import com.rhyme.sidekick.cmdx.framework.CLIConfig
import com.rhyme.sidekick.cmdx.framework.CLIResources
import kotlinx.serialization.json.Json
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.exists
import kotlin.io.path.readText

/**
 * 项目名：SideKickCLI
 * 包名：com.rhyme.sidekick.andropilot.config
 * 描述：App 资源实现类
 *
 * 提供多语言字符串、图标和颜色访问，并支持从 strings.json 和 icons.json 加载。
 * 支持多种图标风格和多语言，类似 Android Resources 的使用方式。
 *
 * @author 李春俊-Rhyme
 * @since 10:30 2026/4/23 星期四
 * @version 2.0
 */
class CLIResourcesImpl(
    private val config: CLIConfig,
    private val stringsFile: Path = loadStringPath(),
    private val iconsFile: Path = loadIconsPath()
) : CLIResources {

    companion object {
        /**
         * 获取默认资源文件路径（resources 目录下的 string.json）
         */
        fun loadStringPath(): Path {
            // 尝试从 classpath 加载资源文件并转换为 Path
            val resourceUrl = CLIResourcesImpl::class.java.classLoader.getResource("strings.json")
            return if (resourceUrl != null) {
                try {
                    // 尝试直接转换为 Path（适用于文件系统中的资源）
                    Paths.get(resourceUrl.toURI())
                } catch (e: Exception) {
                    // 如果无法直接转换（如在 JAR 中），则创建临时文件并返回其 Path
                    val tempFile = File.createTempFile("strings", ".json")
                    tempFile.deleteOnExit()
                    resourceUrl.openStream().use { input ->
                        tempFile.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                    tempFile.toPath()
                }
            } else {
                // 如果找不到资源文件，返回当前目录的 strings.json 路径
                File("strings.json").toPath()
            }
        }

        /**
         * 获取默认资源文件路径（resources 目录下的 icons.json）
         */
        fun loadIconsPath(): Path {
            // 尝试从 classpath 加载资源文件并转换为 Path
            val resourceUrl = CLIResourcesImpl::class.java.classLoader.getResource("icons.json")
            return if (resourceUrl != null) {
                try {
                    // 尝试直接转换为 Path（适用于文件系统中的资源）
                    Paths.get(resourceUrl.toURI())
                } catch (e: Exception) {
                    // 如果无法直接转换（如在 JAR 中），则创建临时文件并返回其 Path
                    val tempFile = File.createTempFile("icons", ".json")
                    tempFile.deleteOnExit()
                    resourceUrl.openStream().use { input ->
                        tempFile.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                    tempFile.toPath()
                }
            } else {
                // 如果找不到资源文件，返回当前目录的 icons.json 路径
                File("icons.json").toPath()
            }
        }
    }

    private val json = Json { ignoreUnknownKeys = true }
    private var stringsData: Map<String, Map<String, String>> = mapOf()
    private var iconsData: Map<String, Map<String, String>> = mapOf()
    private var currentLanguage: String = "zh-CN"
    private var currentIconStyle: String = "classic"

    init {
        loadStrings()
        loadIcons()

        // 从配置初始化语言和图标风格
        currentLanguage = try {
            config.resource.currentLanguage
        } catch (_: Throwable) {
            "zh-CN"
        }

        currentIconStyle = try {
            config.resource.defaultIcon
        } catch (_: Throwable) {
            "classic"
        }
    }

    /**
     * 加载字符串资源（strings.json）
     */
    private fun loadStrings() {
        try {
            if (stringsFile.exists()) {
                val content = stringsFile.readText()
                stringsData = json.decodeFromString<Map<String, Map<String, String>>>(content)
            } else {
                throw IllegalStateException("Strings file not found: ${stringsFile.toAbsolutePath()}")
            }
        } catch (e: Exception) {
            println("Error loading strings: ${e.message}")
            stringsData = mapOf()
        }
    }

    /**
     * 加载图标资源（icons.json）
     */
    private fun loadIcons() {
        try {
            if (iconsFile.exists()) {
                val content = iconsFile.readText()
                iconsData = json.decodeFromString<Map<String, Map<String, String>>>(content)
            } else {
                throw IllegalStateException("Icons file not found: ${iconsFile.toAbsolutePath()}")
            }
        } catch (e: Exception) {
            println("Error loading icons: ${e.message}")
            iconsData = mapOf()
        }
    }

    /**
     * 获取字符串资源，支持回退到英文
     */
    override fun getString(key: String): String {
        return stringsData[currentLanguage]?.get(key)
            ?: stringsData["en"]?.get(key)
            ?: key
    }

    /**
     * 获取图标资源（使用当前图标风格）
     */
    override fun getIcon(key: String): String {
        return getIcon(key, currentIconStyle)
    }

    /**
     * 获取特定风格的图标资源
     */
    override fun getIcon(key: String, iconStyle: String): String {
        return iconsData[iconStyle]?.get(key)
            ?: iconsData[currentIconStyle]?.get(key)
            ?: iconsData["classic"]?.get(key)
            ?: key
    }

    /**
     * 获取属性资源（支持嵌套属性访问）
     *
     * e.g. 获取图标或者字符串
     * val menuIcon = resources.getProperty("icons.menu.home")
     * val name = resources.getProperty("app.name")
     */
    override fun getProperty(key: String): String {
        val keys = key.split(".")
        return when (keys.firstOrNull()) {
            "icon", "icons" -> getIcon(keys.drop(1).joinToString("."))
            else -> getString(key)
        }
    }

    /**
     * 获取文件资源
     *
     * e.g. 从 classpath 或文件系统获取文件
     * val filePath: Path = resources.getFile("config/settings.json")
     */
    override fun getFile(path: String): Path {
        val resourcePath = "/$path"
        val inputStream = javaClass.getResourceAsStream(resourcePath)

        return if (inputStream != null) {
            val tempFile = Files.createTempFile("resource_", "_tmp")
            tempFile.toFile().deleteOnExit()
            inputStream.use { input ->
                Files.write(tempFile, input.readAllBytes())
            }
            tempFile
        } else {
            val filePath = Path.of(path)
            if (filePath.exists()) {
                filePath
            } else {
                throw IllegalArgumentException("File not found: $path")
            }
        }
    }

    /**
     * 切换语言
     */
    override fun setLanguage(language: String) {
        if (stringsData.containsKey(language)) {
            currentLanguage = language
        } else {
            println("Warning: Language '$language' not supported, falling back to current language")
        }
    }

    /**
     * 获取所有支持的语言
     */
    override fun getSupportedLanguages(): Set<String> {
        return stringsData.keys
    }

    /**
     * 获取当前语言
     */
    override fun getCurrentLanguage(): String {
        return currentLanguage
    }

    /**
     * 切换图标风格
     */
    override fun setIconStyle(style: String) {
        if (iconsData.containsKey(style)) {
            currentIconStyle = style
        } else {
            println("Warning: Icon style '$style' not supported, falling back to current style")
        }
    }

    /**
     * 获取所有支持的图标风格
     */
    override fun getSupportedIconStyles(): Set<String> {
        return iconsData.keys
    }

    /**
     * 获取当前图标风格
     */
    override fun getCurrentIconStyle(): String {
        return currentIconStyle
    }
}