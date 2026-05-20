package com.rhyme.sidekick.network.dictionary

import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * 词典API接口
 */
interface DictionaryApi {
    @GET("api/v2/entries/en/{word}")
    fun lookupWord(@Path("word") word: String): Call<List<WordEntry>>
}

/**
 * 词典API服务
 */
class DictionaryApiService {
    
    private val api: DictionaryApi
    
    init {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.dictionaryapi.dev/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        
        api = retrofit.create(DictionaryApi::class.java)
    }
    
    /**
     * 查询单词详情(同步方法)
     * @param word 要查询的单词
     * @return 单词详情列表,可能为空
     */
    fun lookupWord(word: String): List<WordEntry> {
        return try {
            val response = api.lookupWord(word).execute()
            if (response.isSuccessful) {
                response.body() ?: emptyList()
            } else {
                println("Error looking up word '$word': HTTP ${response.code()}")
                emptyList()
            }
        } catch (e: Exception) {
            println("Error looking up word '$word': ${e.message}")
            emptyList()
        }
    }
}
