package com.rhyme.sidekick.network.opengrok

import com.google.gson.annotations.SerializedName

/**
 * 响应模型
 */

/**
 * 文件注释响应
 */
data class AnnotationResponse(
    @SerializedName("revision") val revision: String? = null,
    @SerializedName("author") val author: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("version") val version: String? = null
)

/**
 * 文件定义响应
 */
data class FileDefsResponse(
    @SerializedName("type") val type: String? = null,
    @SerializedName("signature") val signature: String? = null,
    @SerializedName("text") val text: String? = null,
    @SerializedName("symbol") val symbol: String? = null,
    @SerializedName("lineStart") val lineStart: Int? = null,
    @SerializedName("lineEnd") val lineEnd: Int? = null,
    @SerializedName("line") val line: Int? = null,
    @SerializedName("namespace") val namespace: String? = null
)

/**
 * 历史记录响应
 */
data class HistoryResponse(
    @SerializedName("entries") val entries: List<HistoryEntry> = emptyList(),
    @SerializedName("start") val start: Int? = null,
    @SerializedName("count") val count: Int? = null,
    @SerializedName("total") val total: Int? = null
)

data class HistoryEntry(
    @SerializedName("revision") val revision: String? = null,
    @SerializedName("date") val date: Long? = null,
    @SerializedName("author") val author: String? = null,
    @SerializedName("tags") val tags: Any? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("files") val files: List<String> = emptyList()
)

/**
 * 目录列表响应
 */
data class ListResponse(
    @SerializedName("path") val path: String? = null,
    @SerializedName("numLines") val numLines: Long? = null,
    @SerializedName("loc") val loc: Long? = null,
    @SerializedName("date") val date: Long? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("pathDescription") val pathDescription: String? = null,
    @SerializedName("isDirectory") val isDirectory: Boolean? = null,
    @SerializedName("size") val size: Long? = null
)

/**
 * 搜索响应
 */
data class SearchResponse(
    @SerializedName("time") val time: Long? = null,
    @SerializedName("resultCount") val resultCount: Int? = null,
    @SerializedName("startDocument") val startDocument: Int? = null,
    @SerializedName("endDocument") val endDocument: Int? = null,
    @SerializedName("results") val results: Map<String, List<SearchResult>> = emptyMap()
)

data class SearchResult(
    @SerializedName("line") val line: String? = null,
    @SerializedName("lineNumber") val lineNumber: String? = null,
    @SerializedName("tag") val tag: String? = null
)

/**
 * 建议响应
 */
data class SuggestResponse(
    @SerializedName("time") val time: Long? = null,
    @SerializedName("suggestions") val suggestions: List<Suggestion> = emptyList(),
    @SerializedName("identifier") val identifier: String? = null,
    @SerializedName("queryText") val queryText: String? = null,
    @SerializedName("partialResult") val partialResult: Boolean? = null
)

data class Suggestion(
    @SerializedName("phrase") val phrase: String? = null,
    @SerializedName("projects") val projects: List<String> = emptyList(),
    @SerializedName("score") val score: Int? = null
)

/**
 * 消息相关模型
 */
data class MessageRequest(
    @SerializedName("tags") val tags: List<String> = emptyList(),
    @SerializedName("messageLevel") val messageLevel: String? = null,
    @SerializedName("text") val text: String? = null,
    @SerializedName("duration") val duration: String? = null
)

data class MessageResponse(
    @SerializedName("expired") val expired: Boolean? = null,
    @SerializedName("created") val created: String? = null,
    @SerializedName("tags") val tags: List<String> = emptyList(),
    @SerializedName("expiration") val expiration: String? = null,
    @SerializedName("cssClass") val cssClass: String? = null,
    @SerializedName("text") val text: String? = null
)

/**
 * 路径描述相关模型
 */
data class PathDescription(
    @SerializedName("path") val path: String? = null,
    @SerializedName("description") val description: String? = null
)

/**
 * 建议器配置相关模型
 */
data class SuggestConfig(
    @SerializedName("enabled") val enabled: Boolean? = null,
    @SerializedName("maxResults") val maxResults: Int? = null,
    @SerializedName("minChars") val minChars: Int? = null,
    @SerializedName("allowedProjects") val allowedProjects: List<String>? = null,
    @SerializedName("maxProjects") val maxProjects: Int? = null,
    @SerializedName("allowedFields") val allowedFields: List<String> = emptyList(),
    @SerializedName("allowComplexQueries") val allowComplexQueries: Boolean? = null,
    @SerializedName("allowMostPopular") val allowMostPopular: Boolean? = null,
    @SerializedName("showScores") val showScores: Boolean? = null,
    @SerializedName("showProjects") val showProjects: Boolean? = null,
    @SerializedName("showTime") val showTime: Boolean? = null,
    @SerializedName("rebuildCronConfig") val rebuildCronConfig: String? = null,
    @SerializedName("buildTerminationTime") val buildTerminationTime: Int? = null,
    @SerializedName("timeThreshold") val timeThreshold: Int? = null
)

/**
 * 流行度数据相关模型
 */
data class PopularityDataItem(
    @SerializedName("project") val project: String? = null,
    @SerializedName("field") val field: String? = null,
    @SerializedName("token") val token: String? = null,
    @SerializedName("increment") val increment: Int? = null
)

data class PopularityEntry(
    val token: String? = null,
    val count: Int? = null
)

/**
 * 系统信息相关模型
 */
data class VersionInfo(
    val version: String? = null
)

data class IndexTimeInfo(
    val indexTime: String? = null
)

/**
 * 项目仓库信息
 */
data class RepositoryInfo(
    val path: String? = null,
    val type: String? = null,
    val working: Boolean? = null,
    val remote: Boolean? = null,
    val parent: String? = null,
    val branch: String? = null,
    val currentVersion: String? = null,
    val handleRenamedFiles: Boolean? = null,
    val historyEnabled: Boolean? = null,
    val annotationCacheEnabled: Boolean? = null
)

/**
 * 异步任务状态
 */
data class TaskStatus(
    val status: String? = null,
    val result: Any? = null
)