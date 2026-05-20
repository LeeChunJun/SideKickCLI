package com.rhyme.sidekick.quickgrok.domain.usecase

import com.rhyme.sidekick.quickgrok.domain.model.SearchRequest
import com.rhyme.sidekick.quickgrok.domain.model.SearchResultItem
import com.rhyme.sidekick.quickgrok.domain.repository.QuickGrokRepository

/**
 * 执行搜索用例
 */
class SearchCodeUseCase(
    private val repository: QuickGrokRepository
) {
    suspend operator fun invoke(request: SearchRequest): Result<List<SearchResultItem>> {
        return repository.search(request)
    }
}
