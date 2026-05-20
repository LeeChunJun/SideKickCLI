package com.rhyme.sidekick.cmdx.datastore.core

/**
 * 处理 DataStore 读取时遇到的数据损坏问题的接口
 */
interface CorruptionHandler<T> {
    /**
     * 当读取数据时遇到 CorruptionException 时调用
     * @param ex 损坏异常
     * @return 新的数据，将被写入文件
     */
    suspend fun handleCorruption(ex: CorruptionException): T
}

/**
 * 用指定的数据替换损坏的数据
 */
class ReplaceFileCorruptionHandler<T>(
    val onCorruption: suspend (ex: CorruptionException) -> T
) : CorruptionHandler<T> {
    override suspend fun handleCorruption(ex: CorruptionException): T = onCorruption(ex)
}

/**
 * 无操作的损坏处理器，直接抛出异常
 */
class NoOpCorruptionHandler<T> : CorruptionHandler<T> {
    override suspend fun handleCorruption(ex: CorruptionException): T {
        throw ex
    }
}

/**
 * 数据损坏异常
 */
class CorruptionException(message: String, cause: Throwable? = null) :
    Exception(message, cause)
