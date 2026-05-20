package com.rhyme.sidekick.dictionary.data.source.local

import com.rhyme.sidekick.dictionary.data.source.WordDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 本地单词数据源 - 从english.txt文件读取
 */
class LocalWordDataSource : WordDataSource {
    
    private var allWords: List<String>? = null
    
    override suspend fun searchWords(query: String): List<String> {
        return withContext(Dispatchers.IO) {
            val words = getAllWords()
            if (query.isBlank()) {
                words.take(50) // 默认返回前50个单词
            } else {
                words.filter { it.contains(query, ignoreCase = true) }
                    .take(100) // 限制返回数量
            }
        }
    }
    
    override suspend fun getAllWords(): List<String> {
        return withContext(Dispatchers.IO) {
            if (allWords == null) {
                allWords = loadWordsFromFile()
            }
            allWords!!
        }
    }
    
    private fun loadWordsFromFile(): List<String> {
        return try {
            val inputStream = javaClass.classLoader?.getResourceAsStream("english.txt")
            if (inputStream != null) {
                inputStream.bufferedReader().useLines { lines ->
                    lines.map { it.trim() }
                        .filter { it.isNotBlank() }
                        .filter { it.matches(Regex("^[a-zA-Z]+$")) } // 只保留纯字母单词
                        .distinct()
                        .sorted()
                        .toList()
                }
            } else {
                println("Warning: english.txt not found in resources")
                emptyList()
            }
        } catch (e: Exception) {
            println("Error loading english.txt: ${e.message}")
            emptyList()
        }
    }
}
