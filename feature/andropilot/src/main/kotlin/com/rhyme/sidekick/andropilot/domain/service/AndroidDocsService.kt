package com.rhyme.sidekick.andropilot.domain.service

import com.rhyme.sidekick.andropilot.data.repository.AndroidDocsRepository
import com.rhyme.sidekick.andropilot.domain.model.AndroidDocDetail
import com.rhyme.sidekick.andropilot.domain.model.AndroidDocSearchResult

/**
 * Android文档服务
 * 提供文档搜索和获取的业务逻辑
 */
class AndroidDocsService {
    
    private val repository = AndroidDocsRepository()

    /**
     * 搜索文档
     * @param query 搜索关键词
     * @return 搜索结果列表
     */
    suspend fun searchDocuments(query: String): Result<List<AndroidDocSearchResult>> {
        if (query.isBlank()) {
            return Result.failure(IllegalArgumentException("搜索关键词不能为空"))
        }
        
        return repository.searchDocs(query.trim())
    }

    /**
     * 获取文档详情
     * @param kbUrl 知识库URL
     * @return 文档详情
     */
    suspend fun getDocumentDetail(kbUrl: String): Result<AndroidDocDetail> {
        if (kbUrl.isBlank()) {
            return Result.failure(IllegalArgumentException("文档URL不能为空"))
        }
        
        if (!kbUrl.startsWith("kb://")) {
            return Result.failure(IllegalArgumentException("无效的文档URL格式,应以kb://开头"))
        }
        
        return repository.fetchDocDetail(kbUrl.trim())
    }
}
