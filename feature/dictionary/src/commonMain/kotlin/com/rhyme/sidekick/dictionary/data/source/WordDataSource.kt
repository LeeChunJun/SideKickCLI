package com.rhyme.sidekick.dictionary.data.source

import com.rhyme.sidekick.dictionary.domain.model.Word

/**
 * 单词数据源接口
 */
interface WordDataSource {
    /**
     * 搜索单词列表（支持模糊匹配）
     */
    suspend fun searchWords(query: String): List<String>
    
    /**
     * 获取所有单词列表
     */
    suspend fun getAllWords(): List<String>
}

/**
 * 单词详情数据源接口
 */
interface WordDetailDataSource {
    /**
     * 查询单词详情
     */
    suspend fun getWordDetail(word: String): Result<Word>
}
