package com.rhyme.sidekick.cmdx.viewmodel.core

import java.io.Closeable
import java.util.concurrent.ConcurrentHashMap

/**
 * ViewModel 基类
 */
abstract class ViewModel {
    // 用于存放各种需要随 ViewModel 销毁的东西（比如 viewModelScope）
    private val bagOfTags = ConcurrentHashMap<String, Any>()
    @Volatile private var cleared = false

    protected open fun onCleared() {}

    fun clear() {
        cleared = true
        synchronized(bagOfTags) {
            for (value in bagOfTags.values) {
                closeWithRuntimeException(value)
            }
        }
        onCleared()
    }

    fun <T : Any> setTagIfAbsent(key: String, newValue: T): T {
        @Suppress("UNCHECKED_CAST")
        return bagOfTags.putIfAbsent(key, newValue) as T? ?: newValue
    }

    private fun closeWithRuntimeException(obj: Any) {
        if (obj is Closeable) {
            try {
                obj.close()
            } catch (e: Exception) {
                throw RuntimeException(e)
            }
        }
    }
}