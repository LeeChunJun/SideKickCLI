package com.rhyme.sidekick.quickgrok.domain.repository

import com.rhyme.sidekick.quickgrok.data.source.QuickGrokDataSource
import com.rhyme.sidekick.quickgrok.domain.model.ProjectInfo
import com.rhyme.sidekick.quickgrok.domain.model.SearchRequest
import com.rhyme.sidekick.quickgrok.domain.model.SearchResultItem

/**
 * QuickGrok仓库
 */
class QuickGrokRepository(
    private val localDataSource: QuickGrokDataSource,
    private val remoteDataSource: QuickGrokDataSource
) {

    /**
     * 获取所有项目列表
     */
    suspend fun getProjects(): Result<List<ProjectInfo>> {
        return remoteDataSource.getProjects()
    }

    /**
     * 执行代码搜索
     */
    suspend fun search(request: SearchRequest): Result<List<SearchResultItem>> {
        return remoteDataSource.search(request)
    }

    /**
     * Ping检查服务是否可用
     */
    suspend fun ping(): Result<Boolean> {
        return remoteDataSource.ping()
    }
}
