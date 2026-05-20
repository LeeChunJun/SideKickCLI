package com.rhyme.sidekick.cmdx.datastore.core

import androidx.annotation.GuardedBy
import java.io.File
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * 为单进程 DataStore 创建属性委托。应该在文件顶部仅调用一次，
 * 所有 DataStore 的使用都应该引用同一个实例。
 *
 * 仅限在单个应用、单个类加载器、单个进程中使用。
 *
 * 使用示例：
 * ```
 * object MyDataStore {
 *    val dataStore by dataStore(name = "mydata.pb", serializer = { CustomSerializer })
 * }
 *
 * class SomeClass {
 *    suspend fun update() = MyDataStore.dataStore.updateData {...}
 * }
 * ```
 *
 * @param name 数据文件名。数据将存储在 "datastore/" 子目录中
 * @param baseDir 基础目录，默认为用户主目录下的 .datastore
 * @param corruptionHandler 当 DataStore 读取数据时遇到 [CorruptionException] 时调用。
 * 当序列化器无法反序列化数据时会抛出此异常
 * @param produceMigrations 生成数据迁移列表。数据迁移在首次访问数据前运行。
 * 每个生产器和迁移可能运行多次（由于其他迁移失败或磁盘写入失败）
 * @param serializer 数据序列化器提供函数
 *
 * @return 管理单例 DataStore 的属性委托
 */
inline fun <reified T> dataStore(
    name: String,
    baseDir: File = File(System.getProperty("user.home"), ".datastore"),
    corruptionHandler: ReplaceFileCorruptionHandler<T>? = null,
    noinline produceMigrations: () -> List<DataMigration<T>> = { listOf() },
    noinline serializer: () -> Serializer<T>
): ReadOnlyProperty<Any?, DataStore<T>> {
    return DataStoreSingletonDelegate(name, baseDir, corruptionHandler, produceMigrations, serializer)
}

/**
 * 委托类，用于管理 DataStore 单例。
 */
class DataStoreSingletonDelegate<T> constructor(
    private val name: String,
    private val baseDir: File,
    private val corruptionHandler: ReplaceFileCorruptionHandler<T>?,
    private val produceMigrations: () -> List<DataMigration<T>>,
    private val serializerProvider: () -> Serializer<T>
) : ReadOnlyProperty<Any?, DataStore<T>> {

    private val lock = Any()

    @GuardedBy("lock")
    @Volatile
    private var INSTANCE: DataStore<T>? = null

    override fun getValue(thisRef: Any?, property: KProperty<*>): DataStore<T> {
        return INSTANCE ?: synchronized(lock) {
            if (INSTANCE == null) {
                INSTANCE = DataStoreFactory.create(
                    serializer = serializerProvider(),
                    corruptionHandler = corruptionHandler,
                    migrations = produceMigrations(),
                ) {
                    val datastoreDir = File(baseDir, "datastore")
                    File(datastoreDir, name)
                }
            }
            INSTANCE!!
        }
    }
}
