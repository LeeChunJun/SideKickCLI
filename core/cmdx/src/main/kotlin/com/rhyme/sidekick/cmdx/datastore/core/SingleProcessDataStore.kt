package com.rhyme.sidekick.cmdx.datastore.core

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.SyncFailedException

/**
 * DataStore 单进程实现
 * 核心原理：通过 Mutex 保证序列化访问，通过磁盘文件保证持久化
 */
class SingleProcessDataStore<T>(
    private val produceFile: () -> File,
    private val serializer: Serializer<T>,
    private val corruptionHandler: CorruptionHandler<T> = NoOpCorruptionHandler(),
    private val migrations: List<DataMigration<T>> = emptyList()
) : DataStore<T> {

    private val lock = Mutex()
    private var cachedData: T? = null
    private var initialized = false
    private val file: File by lazy { produceFile() }

    override val data: Flow<T> = flow {
        lock.withLock {
            if (!initialized) {
                cachedData = readData()
                runMigrations()
                initialized = true
            }
            emit(cachedData!!)
        }
    }

    override suspend fun updateData(transform: suspend (t: T) -> T): T = lock.withLock {
        // 确保初始化完成
        if (!initialized) {
            cachedData = readData()
            runMigrations()
            initialized = true
        }

        val currentData = cachedData ?: readData()
        val newData = transform(currentData)

        // 只有数据变化才写入磁盘
        if (newData != currentData) {
            writeData(newData)
        }

        cachedData = newData
        newData
    }

    private suspend fun readData(): T {
        return withContext(Dispatchers.IO) {
            try {
                FileInputStream(file).use { stream ->
                    serializer.readFrom(stream)
                }
            } catch (ex: FileNotFoundException) {
                println("警告：序列化读取数据时文件未找到: ${ex.message}")
                serializer.defaultValue
            } catch (ex: CorruptionException) {
                val recoveredData = corruptionHandler.handleCorruption(ex)
                try {
                    writeData(recoveredData)
                } catch (writeEx: IOException) {
                    System.err.println("警告：无法写入恢复后的数据: ${writeEx.message}")
                }
                recoveredData
            }
        }
    }

    private suspend fun writeData(data: T) {
        try {
            file.parentFile?.mkdirs()
            val tmpFile = File(file.absolutePath + ".tmp")

            FileOutputStream(tmpFile).use { stream ->
                serializer.writeTo(data, stream)
                stream.flush()
                try {
                    stream.fd.sync()
                } catch (ex: SyncFailedException) {
                    println("警告: 文件同步失败,但数据已写入: ${ex.message}")
                }
            }

            // 原子操作：临时文件重命名为目标文件
            if (file.exists()) {
                if (!file.delete()) {
                    throw IOException("无法删除已存在的文件 $file，无法完成更新")
                }
            }

            if (!tmpFile.renameTo(file)) {
                throw IOException(
                    "无法重命名文件 $tmpFile 为 $file。" +
                            "\n临时文件是否存在: ${tmpFile.exists()}" +
                            "\n临时文件大小: ${tmpFile.length()} bytes" +
                            "\n目标目录是否存在: ${file.parentFile?.exists()}" +
                            "\n目标目录可写: ${file.parentFile?.canWrite()}"
                )
            }
        } catch (ex: IOException) {
            throw ex
        }
    }

    private suspend fun runMigrations() {
        migrations.forEach { migration ->
            val current = cachedData ?: readData()
            if (migration.shouldMigrate(current)) {
                val migrated = migration.migrate(current)
                writeData(migrated)
                cachedData = migrated
            }
        }
    }
}