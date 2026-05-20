package com.rhyme.sidekick.dictionary.domain.usecase

import com.rhyme.sidekick.dictionary.domain.model.Word
import com.rhyme.sidekick.dictionary.domain.repository.WordRepository

/**
 * 获取单词详情用例
 */
class GetWordDetailUseCase(
    private val repository: WordRepository
) {
    suspend operator fun invoke(word: String): Result<Word> {
        return repository.getWordDetail(word)
    }
}
