package com.rhyme.sidekick.cmdx.room.runtime

import kotlinx.coroutines.flow.MutableSharedFlow

/**
 * 失效追踪器
 * 负责在数据变动时通知 Flow 重新读取。
 */
object InvalidationTracker {
    private val _tableChanges = MutableSharedFlow<String>(extraBufferCapacity = 64)
    val tableChanges = _tableChanges

    suspend fun notifyChange(tableName: String) {
        _tableChanges.emit(tableName)
    }
}