package com.rhyme.sidekick.cmdx.datastore

import com.rhyme.sidekick.cmdx.datastore.preferences.PreferenceDataStoreFactory
import com.rhyme.sidekick.cmdx.datastore.preferences.booleanPreferencesKey
import com.rhyme.sidekick.cmdx.datastore.preferences.edit
import com.rhyme.sidekick.cmdx.datastore.preferences.intPreferencesKey
import com.rhyme.sidekick.cmdx.datastore.preferences.stringPreferencesKey
import com.rhyme.sidekick.cmdx.datastore.preferences.stringSetPreferencesKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.io.File

/**
 * Preferences DataStore 使用示例
 */
object PreferenceExample {
    // 定义 Preference Keys
    private val USERNAME_KEY = stringPreferencesKey("username")
    private val AGE_KEY = intPreferencesKey("age")
    private val IS_DARK_MODE_KEY = booleanPreferencesKey("is_dark_mode")
    private val TAGS_KEY = stringSetPreferencesKey("tags")
    
    @JvmStatic
    fun main(args: Array<String>) = runBlocking {
        val fileCreator = {
            val baseDir = File(System.getProperty("user.home"), ".datastore")
            val datastoreDir = File(baseDir, "datastore")
            File(datastoreDir, "prefs.preferences_pb") // 文件扩展名必须是 .preferences_pb
        }

        // 创建 DataStore 实例
        val dataStore = PreferenceDataStoreFactory.create(
            produceFile = fileCreator
        )
        
        println("=== DataStore Preferences 使用示例 ===\n")
        
        // 示例 1: 写入数据
        println("1. 写入数据:")
        dataStore.edit { prefs ->
            prefs[USERNAME_KEY] = "张三"
            prefs[AGE_KEY] = 25
            prefs[IS_DARK_MODE_KEY] = true
            prefs[TAGS_KEY] = setOf("开发者", "技术爱好者", "Kotlin使用者")
        }
        println("   ✓ 写入完成\n")
        
        // 示例 2: 读取数据
        println("2. 读取数据:")
        val currentPrefs = dataStore.data.first()
        println("   用户名: ${currentPrefs[USERNAME_KEY]}")
        println("   年龄: ${currentPrefs[AGE_KEY]}")
        println("   深色模式: ${currentPrefs[IS_DARK_MODE_KEY]}")
        println("   标签: ${currentPrefs[TAGS_KEY]}\n")
        
        // 示例 3: 更新单个字段
        println("3. 更新单个字段:")
        dataStore.edit { prefs ->
            prefs[AGE_KEY] = 26
        }
        val updatedPrefs = dataStore.data.first()
        println("   年龄已更新为: ${updatedPrefs[AGE_KEY]}\n")
        
        // 示例 4: 条件更新
        println("4. 条件更新:")
        val result = dataStore.edit { prefs ->
            val currentAge = prefs[AGE_KEY] ?: 0
            if (currentAge < 30) {
                prefs[AGE_KEY] = currentAge + 1
            }
        }
        println("   年龄: ${result[AGE_KEY]}\n")
        
        // 示例 5: 删除字段
        println("5. 删除字段:")
        dataStore.edit { prefs ->
            prefs[TAGS_KEY] = null  // 删除标签
        }
        val afterDelete = dataStore.data.first()
        println("   标签已删除: ${afterDelete[TAGS_KEY]}\n")
        
        // 示例 6: 清空所有数据
        println("6. 清空所有数据:")
        dataStore.edit { prefs ->
            prefs.clear()
        }
        val cleared = dataStore.data.first()
        println("   已清空，当前数据为: $cleared\n")
        
        // 清理测试文件
        fileCreator().delete()
        println("✓ 示例执行完成")
    }
}
