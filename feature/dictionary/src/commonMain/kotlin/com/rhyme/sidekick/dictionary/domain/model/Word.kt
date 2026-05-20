package com.rhyme.sidekick.dictionary.domain.model

/**
 * 单词实体类
 */
data class Word(
    val text: String,
    val phonetic: String? = null,
    val audio: String? = null,
    val origin: String? = null,
    val meanings: List<WordMeaning> = emptyList()
)

/**
 * 单词释义
 */
data class WordMeaning(
    val partOfSpeech: String,
    val definitions: List<WordDefinition> = emptyList()
)

/**
 * 单词定义
 */
data class WordDefinition(
    val definition: String,
    val example: String? = null,
    val synonyms: List<String> = emptyList(),
    val antonyms: List<String> = emptyList()
)
