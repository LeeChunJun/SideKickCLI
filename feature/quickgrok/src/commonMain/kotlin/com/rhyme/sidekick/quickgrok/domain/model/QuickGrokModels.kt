package com.rhyme.sidekick.quickgrok.domain.model

/**
 * 搜索过滤器类型
 */
enum class SearchFilterType {
    FULL_SEARCH,      // 全文搜索
    DEFINITION,       // 定义
    SYMBOL,           // 符号
    FILE_PATH,        // 文件路径
    HISTORY           // 历史
}

/**
 * 搜索结果项
 */
data class SearchResultItem(
    val filePath: String,
    val projectName: String,
    val lines: List<CodeLine>
)

/**
 * 代码行
 */
data class CodeLine(
    val lineNumber: String,
    val content: String,
    val tag: String? = null
)

/**
 * 项目信息
 */
data class ProjectInfo(
    val name: String,
    val isSelected: Boolean = false
)

/**
 * 搜索请求
 */
data class SearchRequest(
    val query: String = "",
    val filterType: SearchFilterType = SearchFilterType.FULL_SEARCH,
    val projects: List<String> = emptyList(),
    val maxResults: Int = 100,
    val start: Int = 0,
    val sort: String = "relevancy"
)
