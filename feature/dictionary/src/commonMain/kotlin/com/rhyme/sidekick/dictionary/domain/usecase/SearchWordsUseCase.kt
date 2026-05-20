package com.rhyme.sidekick.dictionary.domain.usecase

import com.rhyme.sidekick.dictionary.domain.repository.WordRepository

/**
 * 搜索单词用例
 */
class SearchWordsUseCase(
    private val repository: WordRepository
) {
    suspend operator fun invoke(query: String): List<String> {
        return repository.searchWords(query)
    }
}
