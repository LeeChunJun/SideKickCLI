package com.rhyme.sidekick.quickgrok.data.source.remote

import com.rhyme.sidekick.network.opengrok.OpenGrokApiService
import com.rhyme.sidekick.quickgrok.data.mapper.QuickGrokMapper
import com.rhyme.sidekick.quickgrok.data.source.QuickGrokDataSource
import com.rhyme.sidekick.quickgrok.domain.model.ProjectInfo
import com.rhyme.sidekick.quickgrok.domain.model.SearchFilterType
import com.rhyme.sidekick.quickgrok.domain.model.SearchRequest
import com.rhyme.sidekick.quickgrok.domain.model.SearchResultItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 远程OpenGrok数据源 - 调用OpenGrok API
 */
class RemoteQuickGrokDataSource(
    private val apiService: OpenGrokApiService = OpenGrokApiService()
) : QuickGrokDataSource {
    
    override suspend fun getProjects(): Result<List<ProjectInfo>> {
        return withContext(Dispatchers.IO) {
            try {
                val projects = QuickGrokMapper.mapProjects(apiService.getProjectsSync())
                Result.success(projects)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    override suspend fun search(request: SearchRequest): Result<List<SearchResultItem>> {
        return withContext(Dispatchers.IO) {
            try {
                // 根据过滤器类型设置不同的搜索参数
                val fullQuery = if (request.filterType == SearchFilterType.FULL_SEARCH) {
                    request.query
                } else null
                
                val defQuery = if (request.filterType == SearchFilterType.DEFINITION) {
                    request.query
                } else null
                
                val symbolQuery = if (request.filterType == SearchFilterType.SYMBOL) {
                    request.query
                } else null
                
                val pathQuery = if (request.filterType == SearchFilterType.FILE_PATH) {
                    request.query
                } else null
                
                val histQuery = if (request.filterType == SearchFilterType.HISTORY) {
                    request.query
                } else null
                
                val projectsParam = if (request.projects.isNotEmpty()) {
                    request.projects.joinToString(",")
                } else null
                
                val searchResponse = apiService.executeSearchSync(
                    full = fullQuery,
                    def = defQuery,
                    symbol = symbolQuery,
                    path = pathQuery,
                    hist = histQuery,
                    projects = projectsParam,
                    maxresults = request.maxResults,
                    start = request.start,
                    sort = request.sort
                )
                
                val results = QuickGrokMapper.mapSearchResults(searchResponse)
                Result.success(results)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    override suspend fun ping(): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val isSuccessful = apiService.pingSync()
                Result.success(isSuccessful)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
