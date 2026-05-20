package com.rhyme.sidekick.cmdx.datastore.core

import kotlinx.coroutines.flow.Flow

/**
 * DataStore 提供安全、持久化的方式存储小量数据
 * 支持原子性、一致性、隔离性、持久性(ACID)保证
 */
interface DataStore<T> {
    /**
     * 提供对最新持久化数据的高效访问
     * @return 代表当前数据状态的 Flow
     */
    val data: Flow<T>

    /**
     * 以原子读-修改-写的方式更新数据
     * 当数据持久化到磁盘后，协程才会完成
     * @param transform 数据转换函数
     * @return 转换后的数据
     */
    suspend fun updateData(transform: suspend (t: T) -> T): T
}