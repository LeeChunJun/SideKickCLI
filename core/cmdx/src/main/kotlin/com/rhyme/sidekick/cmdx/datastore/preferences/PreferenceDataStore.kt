package com.rhyme.sidekick.cmdx.datastore.preferences

import com.rhyme.sidekick.cmdx.datastore.core.DataMigration
import com.rhyme.sidekick.cmdx.datastore.core.DataStore
import com.rhyme.sidekick.cmdx.datastore.core.DataStoreFactory
import com.rhyme.sidekick.cmdx.datastore.core.ReplaceFileCorruptionHandler
import java.io.File

/**
 * PreferenceDataStore 工厂类，专门用于创建 Preferences 类型的 DataStore
 */
object PreferenceDataStoreFactory {
    /**
     * 创建一个 Preferences DataStore 实例
     * 
     * 使用示例：
     * val store = PreferenceDataStoreFactory.create(
     *     produceFile = { File("prefs.preferences_pb") }
     * )
     */
    fun create(
        corruptionHandler: ReplaceFileCorruptionHandler<Preferences>? = null,
        migrations: List<DataMigration<Preferences>> = emptyList(),
        produceFile: () -> File
    ): DataStore<Preferences> {
        val delegate = DataStoreFactory.create(
            serializer = PreferencesSerializer,
            corruptionHandler = corruptionHandler,
            migrations = migrations,
            produceFile = {
                val file = produceFile()
                check(file.extension == "preferences_pb") {
                    "文件扩展名必须是 .preferences_pb"
                }
                file
            }
        )
        return PreferenceDataStore(delegate)
    }
}

/**
 * PreferenceDataStore 包装器，确保数据被冻结
 */
class PreferenceDataStore(
    private val delegate: DataStore<Preferences>
) : DataStore<Preferences> by delegate {
    override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences {
        return delegate.updateData {
            val transformed = transform(it)
            (transformed as MutablePreferences).freeze()
            transformed
        }
    }
}