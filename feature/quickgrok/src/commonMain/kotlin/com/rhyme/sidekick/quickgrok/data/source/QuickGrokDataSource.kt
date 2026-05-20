package com.rhyme.sidekick.quickgrok.data.source

import com.rhyme.sidekick.quickgrok.domain.model.ProjectInfo
import com.rhyme.sidekick.quickgrok.domain.model.SearchRequest
import com.rhyme.sidekick.quickgrok.domain.model.SearchResultItem

/**
 * QuickGrok数据源接口
 */
interface QuickGrokDataSource {
    /**
     * 获取所有项目列表
     */
    suspend fun getProjects(): Result<List<ProjectInfo>>
    
    /**
     * 执行代码搜索
     */
    suspend fun search(request: SearchRequest): Result<List<SearchResultItem>>
    
    /**
     * Ping检查服务是否可用
     */
    suspend fun ping(): Result<Boolean>
}
