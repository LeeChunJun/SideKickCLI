package com.rhyme.sidekick.network.dictionary

import com.google.gson.annotations.SerializedName

/**
 * 词典API响应数据模型
 */
data class WordEntry(
    val word: String,
    val phonetic: String? = null,
    val phonetics: List<Phonetic> = emptyList(),
    val origin: String? = null,
    val meanings: List<Meaning> = emptyList()
)

data class Phonetic(
    val text: String? = null,
    val audio: String? = null
)

data class Meaning(
    @SerializedName("partOfSpeech")
    val partOfSpeech: String,
    val definitions: List<Definition> = emptyList()
)

data class Definition(
    val definition: String,
    val example: String? = null,
    val synonyms: List<String> = emptyList(),
    val antonyms: List<String> = emptyList()
)
