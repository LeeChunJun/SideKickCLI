# DataStore 数据存储方案

DataStore 是一个安全、异步、事务性的数据存储解决方案，用于替代 SharedPreferences。它提供了原子性、一致性、隔离性和持久性 (ACID) 保证。

## 核心特性

### 1. **安全性**
- 所有操作都是原子的，保证数据一致性
- 通过 Mutex 确保线程安全
- 支持数据损坏恢复

### 2. **异步操作**
- 基于 Kotlin Flow，完全异步
- 不阻塞主线程
- 支持协程挂起

### 3. **事务保证**
- 读-修改-写为原子操作
- 磁盘同步保证持久化
- 临时文件策略防止数据丢失

### 4. **灵活扩展**
- 支持自定义序列化器
- 内置数据迁移支持
- 灵活的腐败处理策略

## 快速开始

### 方案一：使用 Preferences (简单键值对)

```kotlin
// 1. 定义 Key
private val USERNAME_KEY = stringPreferencesKey("username")
private val AGE_KEY = intPreferencesKey("age")
private val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")

// 2. 创建 DataStore
val dataStore = PreferenceDataStoreFactory.create(
    produceFile = { File("app_prefs.preferences_pb") }
)

// 3. 读取数据
val prefs = dataStore.data.first()
val username = prefs[USERNAME_KEY]

// 4. 修改数据 (原子操作)
dataStore.edit { prefs ->
    prefs[USERNAME_KEY] = "新用户名"
    prefs[AGE_KEY] = 25
}
```

### 方案二：使用自定义对象

```kotlin
// 1. 定义数据模型
data class UserConfig(
    val userId: String = "",
    val theme: String = "light",
    val language: String = "zh"
) : Serializable

// 2. 实现序列化器
object UserConfigSerializer : Serializer<UserConfig> {
    override val defaultValue = UserConfig()
    
    override suspend fun readFrom(input: InputStream): UserConfig {
        return ObjectInputStream(input).use { 
            it.readObject() as UserConfig 
        }
    }
    
    override suspend fun writeTo(t: UserConfig, output: OutputStream) {
        ObjectOutputStream(output).use { 
            it.writeObject(t)
        }
    }
}

// 3. 创建 DataStore
val configStore = DataStoreFactory.create(
    serializer = UserConfigSerializer,
    produceFile = { File("user_config.pb") }
)

// 4. 使用
val config = configStore.data.first()
configStore.updateData { 
    it.copy(theme = "dark") 
}
```

### 方案三：使用单例委托

```kotlin
// 在顶级对象中定义
object AppDataStore {
    val preferences by dataStore(
        name = "app_settings.preferences_pb",
        serializer = { PreferencesSerializer }
    )
}

// 在任何地方使用
class UserManager {
    suspend fun saveUsername(name: String) {
        AppDataStore.preferences.edit { prefs ->
            prefs[USERNAME_KEY] = name
        }
    }
}
```

## 高级用法

### 数据迁移

```kotlin
// 定义迁移规则
object MigrateV1ToV2 : DataMigration<UserConfig> {
    override suspend fun shouldMigrate(currentData: UserConfig): Boolean {
        // 仅当版本为 v1 时执行
        return currentData.version == 1
    }
    
    override suspend fun migrate(currentData: UserConfig): UserConfig {
        return currentData.copy(
            version = 2,
            newField = "默认值"
        )
    }
}

// 创建时指定迁移
val configStore = DataStoreFactory.create(
    serializer = UserConfigSerializer,
    migrations = listOf(MigrateV1ToV2),
    produceFile = { File("user_config.pb") }
)
```

### 数据损坏恢复

```kotlin
val dataStore = DataStoreFactory.create(
    serializer = MySerializer,
    corruptionHandler = ReplaceFileCorruptionHandler { exception ->
        println("检测到数据损坏: ${exception.message}")
        // 返回默认值或从备份恢复
        MyData()
    },
    produceFile = { File("data.pb") }
)
```

### 自定义协程作用域

```kotlin
val customScope = CoroutineScope(Dispatchers.Default + Job())

val dataStore = DataStoreFactory.create(
    serializer = MySerializer,
    scope = customScope,
    produceFile = { File("data.pb") }
)
```

## API 参考

### DataStore<T> 接口

```kotlin
interface DataStore<T> {
    // 提供最新数据的 Flow，支持多个收集者
    val data: Flow<T>
    
    // 原子读-修改-写操作
    suspend fun updateData(transform: suspend (t: T) -> T): T
}
```

### Preferences 特有操作

```kotlin
suspend fun DataStore<Preferences>.edit(
    transform: suspend (MutablePreferences) -> Unit
): Preferences
```

## 性能优化建议

### 1. **缓存策略**
- DataStore 内部缓存最后一次读取的数据
- `data` Flow 只在首次订阅时从磁盘读取
- 减少频繁的 `first()` 调用

### 2. **批量操作**
```kotlin
// ❌ 不好：多次 edit 导致多次磁盘写入
dataStore.edit { prefs[KEY1] = value1 }
dataStore.edit { prefs[KEY2] = value2 }

// ✅ 好：单次 edit 完成多个操作
dataStore.edit { 
    prefs[KEY1] = value1
    prefs[KEY2] = value2
}
```

### 3. **避免热更新**
```kotlin
// ❌ 不好：频繁更新（如每秒更新一次）
launchPeriodicTask {
    dataStore.updateData { it.copy(timestamp = System.currentTimeMillis()) }
}

// ✅ 好：定期批量更新
launchPeriodicTask {
    val updates = collectUpdatesForInterval()
    dataStore.updateData { current ->
        updates.fold(current) { acc, update -> acc.apply(update) }
    }
}
```

## 错误处理

### 常见异常

| 异常 | 原因 | 处理方式 |
|------|------|---------|
| `CorruptionException` | 数据文件损坏 | 使用 `corruptionHandler` 恢复 |
| `IOException` | 磁盘操作失败 | 记录日志，可重试 |
| `IllegalStateException` | Preferences 被冻结后修改 | 在 edit 块内修改 |

### 推荐的错误处理

```kotlin
try {
    dataStore.updateData { 
        it.copy(field = newValue) 
    }
} catch (ex: IOException) {
    Logger.error("数据持久化失败", ex)
    // 通知用户或回退操作
} catch (ex: CorruptionException) {
    Logger.error("数据损坏，已恢复", ex)
    // 损坏处理器会自动恢复
}
```

## 文件结构

```
~/.datastore/
├── datastore/
│   ├── app_preferences.preferences_pb     # Preferences 数据
│   ├── user_config.pb                     # 自定义对象数据
│   └── user_config.pb.tmp                 # 临时文件（正常操作中不应存在）
└── [其他数据存储]
```

## 注意事项

### 1. **单进程限制**
- 当前实现仅支持单进程访问
- 多进程场景需要使用带文件锁的方案

### 2. **Preferences 冻结机制**
- 通过 `edit` 函数修改的 Preferences 会被自动冻结
- 冻结状态防止意外修改

### 3. **协程安全**
- 所有 API 都是暂停的，安全在协程内使用
- 不要在 UI 线程同步等待

### 4. **文件权限**
- 确保应用有权限读写数据存储目录
- 在受限环境中可能需要特殊权限

## 与 SharedPreferences 对比

| 特性 | DataStore | SharedPreferences |
|------|-----------|------------------|
| 线程安全 | ✅ 原子操作 | ⚠️ 需要同步 |
| 异步 API | ✅ 基于 Flow | ❌ 同步阻塞 |
| 类型安全 | ✅ 强类型 | ❌ 所有值为 Any |
| 事务保证 | ✅ ACID | ❌ 不保证 |
| 数据验证 | ✅ 支持 | ❌ 不支持 |
| 文件格式 | 二进制或文本 | XML |

## 完整示例

参考 `PreferenceExample.kt` 和 `UserConfigExample.kt` 获取完整的实际使用示例。

## 疑难解答

### Q: 为什么数据没有保存？
A: 检查以下几点：
1. 是否在协程作用域内调用 suspend 函数
2. 是否正确处理 IOException
3. 文件系统是否有足够空间

### Q: 数据损坏如何恢复？
A: 
1. 设置 `corruptionHandler` 返回默认值
2. 或者手动删除损坏的文件，重新初始化

### Q: 性能如何优化？
A:
1. 减少频繁的小规模更新
2. 批量执行多个字段更新
3. 考虑使用本地缓存减少磁盘访问

## 许可证

本实现基于 Android DataStore 库，遵循 Apache License 2.0
