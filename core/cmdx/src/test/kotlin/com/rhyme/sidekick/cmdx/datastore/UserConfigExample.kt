package com.rhyme.sidekick.cmdx.datastore

import com.rhyme.sidekick.cmdx.datastore.core.CorruptionException
import com.rhyme.sidekick.cmdx.datastore.core.DataStoreFactory
import com.rhyme.sidekick.cmdx.datastore.core.ReplaceFileCorruptionHandler
import com.rhyme.sidekick.cmdx.datastore.core.Serializer
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable

/**
 * 用户配置数据模型
 */
data class UserConfig(
    val userId: String = "Rhyme001",
    val theme: String = "light",
    val language: String = "zh",
    val autoSave: Boolean = true,
    val fontSize: Int = 14
) : Serializable {
    companion object {
        private const val serialVersionUID = 1L
    }
}

/**
 * UserConfig 的序列化器实现
 */
object UserConfigSerializer : Serializer<UserConfig> {
    override val defaultValue: UserConfig = UserConfig()

    override suspend fun readFrom(input: InputStream): UserConfig {
        return try {
            ObjectInputStream(input).use { stream ->
                stream.readObject() as UserConfig
            }
        } catch (ex: Exception) {
            throw CorruptionException("Failed to deserialize UserConfig", ex)
        }
    }

    override suspend fun writeTo(t: UserConfig, output: OutputStream) {
        try {
            ObjectOutputStream(output).use { stream ->
                stream.writeObject(t)
                stream.flush()
            }
        } catch (ex: Exception) {
            throw CorruptionException("Failed to serialize UserConfig", ex)
        }
    }
}

/**
 * 自定义对象 DataStore 使用示例
 */
object UserConfigExample {
    @JvmStatic
    fun main(args: Array<String>) = runBlocking {

        val fileCreator = {
            val baseDir = File(System.getProperty("user.home"), ".datastore")
            val datastoreDir = File(baseDir, "datastore")
            File(datastoreDir, "user_config.pb")
        }

        // 创建 DataStore 实例（用于存储用户配置）
        val configStore = DataStoreFactory.create(
            serializer = UserConfigSerializer,
            produceFile = fileCreator,
            corruptionHandler = ReplaceFileCorruptionHandler { ex ->
                println("警告：检测到数据损坏，已恢复默认配置")
                println("原因: ${ex.message}")
                UserConfig()
            }
        )
        
        println("=== 自定义对象 DataStore 使用示例 ===\n")
        
        // 示例 1: 初始化配置
        println("1. 初始化用户配置:")
        val initialConfig = configStore.updateData { _ ->
            UserConfig(
                userId = "user123",
                theme = "dark",
                language = "en",
                autoSave = false,
                fontSize = 16
            )
        }
        println("   $initialConfig\n")
        
        // 示例 2: 读取配置
        println("2. 读取当前配置:")
        val currentConfig = configStore.data.first()
        println("   ID: ${currentConfig.userId}")
        println("   主题: ${currentConfig.theme}")
        println("   语言: ${currentConfig.language}")
        println("   保存: ${currentConfig.autoSave}")
        println("   字体大小: ${currentConfig.fontSize}\n")
        
        // 示例 3: 更新配置（原子操作）
        println("3. 切换主题:")
        val updated = configStore.updateData { config ->
            config.copy(theme = if (config.theme == "dark") "light" else "dark")
        }
        println("   新主题: ${updated.theme}\n")
        
        // 示例 4: 条件更新
        println("4. 根据条件更新:")
        val result = configStore.updateData { config ->
            if (config.fontSize < 18) {
                config.copy(fontSize = config.fontSize + 2)
            } else {
                config
            }
        }
        println("   更新后字体大小: ${result.fontSize}\n")
        
        // 示例 5: 完整更新
        println("5. 完整更新配置:")
        configStore.updateData { _ ->
            UserConfig(
                userId = "Rhythm233",
                theme = "light",
                language = "zh",
                autoSave = false,
                fontSize = 19
            )
        }
        val finalConfig = configStore.data.first()
        println("   $finalConfig\n")
        
        // 清理测试文件
        fileCreator().delete()
        println("✓ 示例执行完成")
    }
}
