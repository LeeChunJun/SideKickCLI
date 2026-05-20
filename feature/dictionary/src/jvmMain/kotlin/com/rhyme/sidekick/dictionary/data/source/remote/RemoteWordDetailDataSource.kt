package com.rhyme.sidekick.dictionary.data.source.remote

import com.rhyme.sidekick.dictionary.data.source.WordDetailDataSource
import com.rhyme.sidekick.dictionary.domain.model.Word
import com.rhyme.sidekick.dictionary.domain.model.WordDefinition
import com.rhyme.sidekick.dictionary.domain.model.WordMeaning
import com.rhyme.sidekick.network.dictionary.DictionaryApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 远程单词详情数据源 - 使用Dictionary API
 */
class RemoteWordDetailDataSource : WordDetailDataSource {
    
    private val apiService = DictionaryApiService()
    
    override suspend fun getWordDetail(word: String): Result<Word> {
        return withContext(Dispatchers.IO) {
            try {
                val entries = apiService.lookupWord(word)
                
                if (entries.isEmpty()) {
                    Result.failure(Exception("Word '$word' not found"))
                } else {
                    // 取第一个结果
                    val entry = entries.first()
                    val wordModel = Word(
                        text = entry.word,
                        phonetic = entry.phonetic ?: entry.phonetics.firstOrNull()?.text,
                        audio = entry.phonetics.firstOrNull { it.audio != null }?.audio,
                        origin = entry.origin,
                        meanings = entry.meanings.map { meaning ->
                            WordMeaning(
                                partOfSpeech = meaning.partOfSpeech,
                                definitions = meaning.definitions.map { def ->
                                    WordDefinition(
                                        definition = def.definition,
                                        example = def.example,
                                        synonyms = def.synonyms,
                                        antonyms = def.antonyms
                                    )
                                }
                            )
                        }
                    )
                    Result.success(wordModel)
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
