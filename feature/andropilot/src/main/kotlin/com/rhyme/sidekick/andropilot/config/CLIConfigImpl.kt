package com.rhyme.sidekick.andropilot.config

import com.rhyme.sidekick.cmdx.framework.AppInfo
import com.rhyme.sidekick.cmdx.framework.CLIConfig
import com.rhyme.sidekick.cmdx.framework.CacheConfig
import com.rhyme.sidekick.cmdx.framework.ResourceConfig
import com.rhyme.sidekick.cmdx.framework.WorkspaceConfig
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File
import java.nio.file.Path
import java.nio.file.Files
import java.nio.file.Paths

/**
 * 项目名：SideKickCLI
 * 包名：com.rhyme.sidekick.andropilot.config
 * 描述：App 配置实现类，支持从外部传入配置文件或使用默认配置
 *
 * @author 李春俊-Rhyme
 * @since 09:38 2026/4/23 星期四
 * @version 1.0
 */
class CLIConfigImpl internal constructor(
    override val app: AppInfo,
    override val resource: ResourceConfig,
    override val workspace: WorkspaceConfig,
    override val cache: CacheConfig
) : CLIConfig {

    companion object {

        /**
         * 从外部配置文件加载配置
         * @param configPath 配置文件路径，默认为 resources 下的 config.json
         * @return AppConfig 实例
         */
        fun load(configPath: Path? = null): CLIConfig {
            val path = configPath ?: getDefaultConfigPath()

            return if (Files.exists(path)) {
                try {
                    val jsonContent = Files.readString(path)
                    fromJson(jsonContent)
                } catch (e: Exception) {
                    println("Warning: 读取配置文件失败，使用默认配置。错误信息: ${e.message}")
                    createDefaultConfig()
                }
            } else {
                println("Warning: 配置文件不存在，使用默认配置")
                createDefaultConfig()
            }
        }

        /**
         * 从 JSON 字符串创建配置
         * @param jsonContent JSON 字符串内容
         * @return AppConfig 实例
         */
        private fun fromJson(jsonContent: String): CLIConfig {
            return try {
                Json.decodeFromString<AppConfigData>(jsonContent).toAppConfig()
            } catch (e: Exception) {
                println("Warning: 解析 JSON 配置失败，使用默认配置。错误信息: ${e.message}")
                createDefaultConfig()
            }
        }

        /**
         * 创建默认配置（基于 config.json 的内容）
         * @return AppConfig 实例
         */
        private fun createDefaultConfig(): CLIConfig {
            return CLIConfigImpl(
                app = AppInfo(
                    name = "Android CLI Copilot",
                    version = "1.0.0",
                    description = "Your Copilot for Android Command Line"
                ),
                resource = ResourceConfig(
                    defaultIcon = "classic",
                    availableIcons = listOf("classic", "modern", "minimal"),
                    currentLanguage = "zh-CN",
                    availableLanguages = listOf("zh-CN", "zh-TW", "en")
                ),
                workspace = WorkspaceConfig(
                    directory = "./workspace",
                    logLabel = "INFO"
                ),
                cache = CacheConfig(
                    directory = "./.cache",
                    maxSize = "100MB",
                    ttl = 86400
                )
            )
        }

        /**
         * 获取默认配置文件路径（resources 目录下的 config.json）
         */
        private fun getDefaultConfigPath(): Path {
            // 尝试从 classpath 加载资源文件并转换为 Path
            val resourceUrl = CLIConfigImpl::class.java.classLoader.getResource("config.json")
            return if (resourceUrl != null) {
                try {
                    // 尝试直接转换为 Path（适用于文件系统中的资源）
                    Paths.get(resourceUrl.toURI())
                } catch (e: Exception) {
                    // 如果无法直接转换（如在 JAR 中），则创建临时文件并返回其 Path
                    val tempFile = File.createTempFile("config", ".json")
                    tempFile.deleteOnExit()
                    resourceUrl.openStream().use { input ->
                        tempFile.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                    tempFile.toPath()
                }
            } else {
                // 如果找不到资源文件，返回当前目录的 config.json 路径
                File("config.json").toPath()
            }
        }
    }
}

/**
 * 内部数据类，用于 JSON 反序列化
 */
@Serializable
private data class AppConfigData(
    val app: AppInfo,
    val resource: ResourceConfig,
    val workspace: WorkspaceConfig,
    val cache: CacheConfig
) {
    fun toAppConfig(): CLIConfig {
        return CLIConfigImpl(
            app = this.app,
            resource = this.resource,
            workspace = this.workspace,
            cache = this.cache
        )
    }
}


