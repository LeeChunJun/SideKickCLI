package com.rhyme.sidekick.cmdx.datastore.core

import java.io.File

/**
 * DataStore 工厂类，用于创建 DataStore 实例
 *
 * 使用示例：
 * val store = DataStoreFactory.create(
 *     serializer = MySerializer(),
 *     produceFile = { File("data.pb") }
 * )
 */
object DataStoreFactory {
    fun <T> create(
        serializer: Serializer<T>,
        corruptionHandler: ReplaceFileCorruptionHandler<T>? = null,
        migrations: List<DataMigration<T>> = emptyList(),
        produceFile: () -> File
    ): DataStore<T> = SingleProcessDataStore(
        produceFile = produceFile,
        serializer = serializer,
        corruptionHandler = corruptionHandler ?: NoOpCorruptionHandler(),
        migrations = migrations
    )
}