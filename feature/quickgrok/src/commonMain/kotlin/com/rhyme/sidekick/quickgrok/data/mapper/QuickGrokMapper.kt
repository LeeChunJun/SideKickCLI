package com.rhyme.sidekick.quickgrok.data.mapper

import com.rhyme.sidekick.network.opengrok.SearchResponse
import com.rhyme.sidekick.network.opengrok.SearchResult
import com.rhyme.sidekick.quickgrok.domain.model.CodeLine
import com.rhyme.sidekick.quickgrok.domain.model.ProjectInfo
import com.rhyme.sidekick.quickgrok.domain.model.SearchResultItem

/**
 * OpenGrok网络数据到领域模型的映射器
 */
object QuickGrokMapper {
    
    /**
     * 将项目列表字符串转换为ProjectInfo列表
     */
    fun mapProjects(projectNames: List<String>): List<ProjectInfo> {
        return projectNames.map { projectName ->
            ProjectInfo(name = projectName, isSelected = false)
        }
    }
    
    /**
     * 将搜索响应转换为SearchResultItem列表
     */
    fun mapSearchResults(searchResponse: SearchResponse?): List<SearchResultItem> {
        if (searchResponse == null) return emptyList()
        
        return searchResponse.results.map { (filePath, searchResults) ->
            SearchResultItem(
                filePath = filePath,
                projectName = extractProjectName(filePath),
                lines = searchResults.map { result ->
                    mapSearchResult(result)
                }
            )
        }
    }
    
    /**
     * 将单个搜索结果转换为CodeLine
     */
    private fun mapSearchResult(result: SearchResult): CodeLine {
        return CodeLine(
            lineNumber = result.lineNumber ?: "",
            content = result.line ?: "",
            tag = result.tag
        )
    }
    
    /**
     * 从文件路径提取项目名称
     * 例如: /project-name/src/file.java -> project-name
     */
    private fun extractProjectName(filePath: String): String {
        // 移除开头的斜杠
        val cleanPath = filePath.trimStart('/')
        // 取第一个目录作为项目名称
        val parts = cleanPath.split('/')
        return if (parts.isNotEmpty()) parts[0] else "unknown"
    }
}
