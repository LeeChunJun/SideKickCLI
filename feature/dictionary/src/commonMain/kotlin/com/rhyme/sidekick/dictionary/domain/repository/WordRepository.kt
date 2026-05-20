package com.rhyme.sidekick.dictionary.domain.repository

import com.rhyme.sidekick.dictionary.data.source.WordDataSource
import com.rhyme.sidekick.dictionary.data.source.WordDetailDataSource
import com.rhyme.sidekick.dictionary.domain.model.Word

/**
 * 单词仓库 - 协调本地和远程数据源
 */
class WordRepository(
    private val localDataSource: WordDataSource,
    private val remoteDataSource: WordDetailDataSource
) {
    /**
     * 搜索单词列表
     */
    suspend fun searchWords(query: String): List<String> {
        return localDataSource.searchWords(query)
    }
    
    /**
     * 获取单词详情（优先从远程API获取）
     */
    suspend fun getWordDetail(word: String): Result<Word> {
        return remoteDataSource.getWordDetail(word)
    }
}
