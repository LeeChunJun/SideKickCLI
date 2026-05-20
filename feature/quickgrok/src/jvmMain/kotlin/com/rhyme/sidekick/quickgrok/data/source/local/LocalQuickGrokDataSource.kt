package com.rhyme.sidekick.quickgrok.data.source.local

import com.rhyme.sidekick.quickgrok.data.source.QuickGrokDataSource
import com.rhyme.sidekick.quickgrok.domain.model.ProjectInfo
import com.rhyme.sidekick.quickgrok.domain.model.SearchRequest
import com.rhyme.sidekick.quickgrok.domain.model.SearchResultItem

/**
 * 本地QuickGrok数据源 - 用于缓存和离线支持（当前为占位实现）
 * 
 * TODO: 未来可以实现：
 * 1. 缓存项目列表
 * 2. 缓存搜索结果
 * 3. 离线搜索支持
 */
class LocalQuickGrokDataSource : QuickGrokDataSource {
    
    // 简单的内存缓存
    private var cachedProjects: List<ProjectInfo>? = null
    
    override suspend fun getProjects(): Result<List<ProjectInfo>> {
        // 返回缓存的项目列表（如果有）
        return cachedProjects?.let {
            Result.success(it)
        } ?: Result.failure(Exception("No cached projects available"))
    }
    
    override suspend fun search(request: SearchRequest): Result<List<SearchResultItem>> {
        // 本地数据源不支持搜索，应该使用远程数据源
        return Result.failure(Exception("Local search not supported. Use remote data source."))
    }
    
    override suspend fun ping(): Result<Boolean> {
        // 本地数据源始终可用
        return Result.success(true)
    }
    
    /**
     * 更新缓存的项目列表
     */
    fun updateCachedProjects(projects: List<ProjectInfo>) {
        cachedProjects = projects
    }
}
