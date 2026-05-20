package com.rhyme.sidekick.cmdx.framework

import kotlinx.serialization.Serializable

/**
 * 项目名：SideKickCLI
 * 包名：com.rhyme.sidekick.framework
 * 描述：App 配置
 *
 * @author 李春俊-Rhyme
 * @since 15:37 2026/4/23 星期四
 * @version 1.0
 */
interface CLIConfig {
    val app: AppInfo
    val resource: ResourceConfig
    val workspace: WorkspaceConfig
    val cache: CacheConfig
}

@Serializable
data class AppInfo(
    val name: String,
    val version: String,
    val description: String? = null
)

@Serializable
data class ResourceConfig(
    val defaultIcon: String,
    val availableIcons: List<String>,
    val currentLanguage: String,
    val availableLanguages: List<String>
)

@Serializable
data class WorkspaceConfig(
    val directory: String,
    val logLabel: String
)

@Serializable
data class CacheConfig(
    val directory: String,
    val maxSize: String,
    val ttl: Long
)