# DataStore 最佳实践指南

## 目录
1. [架构设计](#架构设计)
2. [使用规范](#使用规范)
3. [性能优化](#性能优化)
4. [错误处理](#错误处理)
5. [测试策略](#测试策略)
6. [常见陷阱](#常见陷阱)

---

## 架构设计

### 推荐的代码组织结构

```
app/
├── data/
│   ├── datastore/
│   │   ├── AppPreferences.kt          # 全局 Preferences 单例
│   │   ├── UserConfigStore.kt         # 用户配置数据存储
│   │   └── CacheStore.kt              # 缓存数据存储
│   └── repository/
│       └── PreferencesRepository.kt   # 业务逻辑层
├── domain/
│   ├── model/
│   │   └── UserConfig.kt              # 数据模型
│   └── usecase/
│       └── GetUserConfigUseCase.kt
└── ui/
    └── ...
```

### 单例管理最佳实践

#### 方法 1: 对象声明 + 委托

```kotlin
// 推荐：清晰、线程安全、延迟初始化
object AppPreferences {
    val dataStore by dataStore(
        name = "app_prefs.preferences_pb",
        serializer = { PreferencesSerializer }
    )
    
    // 便利函数
    suspend fun getUserTheme(): String? {
        val prefs = dataStore.data.first()
        return prefs[THEME_KEY]
    }
}
```

#### 方法 2: 工厂模式

```kotlin
// 用于需要参数化的情况
class UserDataStoreFactory(val userId: String) {
    val configStore: DataStore<UserConfig> by lazy {
        DataStoreFactory.create(
            serializer = UserConfigSerializer,
            produceFile = {
                File(getDataDir(), "user_${userId}_config.pb")
            }
        )
    }
}
```

#### 方法 3: 依赖注入

```kotlin
// 推荐用于 Android/模块化项目
interface DataStoreProvider {
    val preferencesStore: DataStore<Preferences>
    val configStore: DataStore<UserConfig>
}

class DataStoreProviderImpl(context: Context) : DataStoreProvider {
    override val preferencesStore: DataStore<Preferences> by lazy {
        PreferenceDataStoreFactory.create(
            produceFile = { File(context.filesDir, "app_prefs.preferences_pb") }
        )
    }
    
    override val configStore: DataStore<UserConfig> by lazy {
        DataStoreFactory.create(
            serializer = UserConfigSerializer,
            produceFile = { File(context.filesDir, "user_config.pb") }
        )
    }
}
```

---

## 使用规范

### 1. Preferences 使用规范

#### ✅ DO：使用 edit 进行批量操作

```kotlin
// 单个修改
dataStore.edit { prefs ->
    prefs[USERNAME_KEY] = newName
}

// 多个修改 - 一次 edit 完成
dataStore.edit { prefs ->
    prefs[USERNAME_KEY] = newName
    prefs[EMAIL_KEY] = newEmail
    prefs[AGE_KEY] = newAge
}

// 带条件的修改
dataStore.edit { prefs ->
    if (prefs[AGE_KEY] ?: 0 < 18) {
        prefs[IS_ADULT_KEY] = false
    }
}
```

#### ❌ DON'T：多次 edit 调用

```kotlin
// 不好：3次磁盘写入
dataStore.edit { prefs[USERNAME_KEY] = newName }
dataStore.edit { prefs[EMAIL_KEY] = newEmail }
dataStore.edit { prefs[AGE_KEY] = newAge }
```

### 2. Flow 订阅规范

#### ✅ DO：使用 collect 进行持续监听

```kotlin
// 在 UI 层或需要持续监听时
class UserViewModel {
    private val _userName = MutableStateFlow("")
    val userName = _userName.asStateFlow()
    
    init {
        viewModelScope.launch {
            AppPreferences.dataStore.data.collect { prefs ->
                _userName.value = prefs[USERNAME_KEY] ?: ""
            }
        }
    }
}
```

#### ✅ DO：使用 first() 获取单次值

```kotlin
// 在协程作用域内需要同步获取值时
suspend fun loadConfig() {
    val config = configStore.data.first()
    // 使用 config
}
```

#### ❌ DON'T：频繁调用 first()

```kotlin
// 不好：多次读取导致多次磁盘访问
val theme = dataStore.data.first()[THEME_KEY]
val language = dataStore.data.first()[LANGUAGE_KEY]
val fontSize = dataStore.data.first()[FONT_SIZE_KEY]

// 好：一次读取，多次访问
val prefs = dataStore.data.first()
val theme = prefs[THEME_KEY]
val language = prefs[LANGUAGE_KEY]
val fontSize = prefs[FONT_SIZE_KEY]
```

### 3. updateData 使用规范

#### ✅ DO：原子更新复杂数据结构

```kotlin
// 自定义对象的原子更新
configStore.updateData { config ->
    config.copy(
        userId = newUserId,
        lastUpdateTime = System.currentTimeMillis(),
        version = config.version + 1
    )
}

// 条件更新
configStore.updateData { config ->
    if (config.theme == "auto") {
        config.copy(theme = getCurrentSystemTheme())
    } else {
        config
    }
}
```

#### ✅ DO：处理 updateData 异常

```kotlin
try {
    configStore.updateData { config ->
        // 修改逻辑
        config.copy(theme = "dark")
    }
} catch (ex: IOException) {
    Logger.error("配置保存失败", ex)
    showErrorMessage("无法保存配置")
} catch (ex: CorruptionException) {
    Logger.error("配置数据损坏，已恢复", ex)
    // 数据已由 corruptionHandler 恢复
}
```

---

## 性能优化

### 1. 减少磁盘 I/O

```kotlin
// 策略：缓存热数据
class UserConfigRepository(
    private val configStore: DataStore<UserConfig>
) {
    private var cachedConfig: UserConfig? = null
    private var cacheTime = 0L
    private val CACHE_DURATION = 5 * 60 * 1000  // 5分钟
    
    suspend fun getConfig(): UserConfig {
        val now = System.currentTimeMillis()
        
        // 缓存有效
        if (cachedConfig != null && (now - cacheTime) < CACHE_DURATION) {
            return cachedConfig!!
        }
        
        // 重新加载
        cachedConfig = configStore.data.first()
        cacheTime = now
        return cachedConfig!!
    }
}
```

### 2. 流式处理大型数据结构

```kotlin
// 对于包含大量条目的数据，使用流式处理
configStore.data
    .debounce(300)  // 防止频繁更新
    .distinctUntilChanged()  // 仅在改变时处理
    .map { it.theme }  // 投影到感兴趣的字段
    .collect { theme ->
        applyTheme(theme)
    }
```

### 3. 批量写入优化

```kotlin
// 不好：链式单个写入
suspend fun importUserData(data: List<UserItem>) {
    data.forEach { item ->
        dataStore.edit { 
            prefs[item.key] = item.value 
        }  // N次磁盘写入
    }
}

// 好：合并为单次写入
suspend fun importUserData(data: List<UserItem>) {
    dataStore.edit { prefs ->
        data.forEach { item ->
            prefs[item.key] = item.value
        }
    }  // 1次磁盘写入
}
```

---

## 错误处理

### 1. 损坏数据恢复

```kotlin
// 方案 1: 返回默认值
val configStore = DataStoreFactory.create(
    serializer = UserConfigSerializer,
    corruptionHandler = ReplaceFileCorruptionHandler { _ ->
        Logger.warn("用户配置损坏，使用默认值")
        UserConfig()  // 默认值
    },
    produceFile = { File("user_config.pb") }
)

// 方案 2: 尝试备份恢复
val configStore = DataStoreFactory.create(
    serializer = UserConfigSerializer,
    corruptionHandler = ReplaceFileCorruptionHandler { ex ->
        Logger.error("配置文件损坏", ex)
        val backup = loadBackupConfig()
        backup ?: UserConfig()
    },
    produceFile = { File("user_config.pb") }
)
```

### 2. IO 错误处理

```kotlin
class SafePreferencesRepository(
    private val dataStore: DataStore<Preferences>
) {
    suspend fun saveUserData(data: UserData): Result<Unit> = runCatching {
        dataStore.edit { prefs ->
            prefs[USER_ID_KEY] = data.userId
            prefs[USER_NAME_KEY] = data.name
        }
    }.onFailure { ex ->
        when (ex) {
            is IOException -> {
                Logger.error("无法保存用户数据：磁盘错误", ex)
                // 可能需要提示用户重试
            }
            is CorruptionException -> {
                Logger.error("数据损坏", ex)
                // 数据会自动恢复
            }
            else -> {
                Logger.error("未知错误", ex)
            }
        }
    }
}
```

### 3. 迁移错误处理

```kotlin
object MigrateV1ToV2 : DataMigration<UserConfig> {
    override suspend fun shouldMigrate(currentData: UserConfig): Boolean {
        return currentData.version < 2
    }
    
    override suspend fun migrate(currentData: UserConfig): UserConfig {
        return try {
            currentData.copy(
                version = 2,
                newField = transformOldField(currentData.oldField)
            )
        } catch (ex: Exception) {
            Logger.error("迁移失败，使用默认值", ex)
            UserConfig(version = 2)
        }
    }
}
```

---

## 测试策略

### 1. 单元测试

```kotlin
class PreferencesRepositoryTest {
    private lateinit var dataStore: DataStore<Preferences>
    
    @Before
    fun setup() {
        // 使用临时文件进行测试
        val tempFile = File.createTempFile("test_prefs", ".preferences_pb")
        
        dataStore = PreferenceDataStoreFactory.create(
            produceFile = { tempFile }
        )
    }
    
    @After
    fun cleanup() {
        // 清理测试文件
        dataStore.data.first().asMap().clear()
    }
    
    @Test
    fun testSaveAndLoad() = runBlocking {
        // Arrange
        val expectedName = "张三"
        
        // Act
        dataStore.edit { prefs ->
            prefs[USERNAME_KEY] = expectedName
        }
        
        // Assert
        val prefs = dataStore.data.first()
        assertEquals(expectedName, prefs[USERNAME_KEY])
    }
    
    @Test
    fun testAtomicUpdate() = runBlocking {
        // 多线程原子性测试
        coroutineScope {
            repeat(100) {
                launch {
                    dataStore.edit { prefs ->
                        val current = prefs[COUNTER_KEY] ?: 0
                        prefs[COUNTER_KEY] = current + 1
                    }
                }
            }
        }
        
        val finalValue = dataStore.data.first()[COUNTER_KEY] ?: 0
        assertEquals(100, finalValue)
    }
}
```

### 2. 集成测试

```kotlin
class UserConfigStoreIntegrationTest {
    private lateinit var configStore: DataStore<UserConfig>
    
    @Test
    fun testMigrationFlow() = runBlocking {
        // 创建包含迁移的 DataStore
        configStore = DataStoreFactory.create(
            serializer = UserConfigSerializer,
            migrations = listOf(MigrateV1ToV2),
            produceFile = { createTestFile() }
        )
        
        // 验证迁移完成
        val config = configStore.data.first()
        assertEquals(2, config.version)
    }
}
```

### 3. 使用 FakeDataStore 进行单元测试

```kotlin
// 创建虚假实现用于测试
class FakeDataStore<T>(
    private val initialValue: T
) : DataStore<T> {
    private val state = MutableStateFlow(initialValue)
    
    override val data: Flow<T> = state.asStateFlow()
    
    override suspend fun updateData(transform: suspend (t: T) -> T): T {
        val newValue = transform(state.value)
        state.value = newValue
        return newValue
    }
}

// 在测试中使用
@Test
fun testWithFakeDataStore() = runBlocking {
    val fakeStore = FakeDataStore(UserConfig())
    
    fakeStore.updateData { config ->
        config.copy(theme = "dark")
    }
    
    val result = fakeStore.data.first()
    assertEquals("dark", result.theme)
}
```

---

## 常见陷阱

### 1. ❌ 在非协程上下文调用 suspend 函数

```kotlin
// 不好
val config = configStore.data.first()  // 在主线程，会阻塞！

// 好
launch {
    val config = configStore.data.first()
}
```

### 2. ❌ 在 Preferences 外部修改

```kotlin
// 不好：Preferences 被冻结，无法修改
val prefs = dataStore.data.first()
prefs[KEY] = value  // IllegalStateException

// 好：在 edit 块内修改
dataStore.edit { prefs ->
    prefs[KEY] = value
}
```

### 3. ❌ 为每个字段创建单独的 DataStore

```kotlin
// 不好：资源浪费
object AppSettings {
    val themeStore by dataStore("theme.pb") { /* ...*/ }
    val languageStore by dataStore("language.pb") { /* ...*/ }
    val fontSizeStore by dataStore("font_size.pb") { /* ...*/ }
}

// 好：统一存储
object AppSettings {
    val settingsStore by dataStore("app_settings.pb") { /* ...*/ }
}
```

### 4. ❌ 忘记处理 CorruptionException

```kotlin
// 不好：未处理损坏
val dataStore = DataStoreFactory.create(
    serializer = MySerializer,
    produceFile = { File("data.pb") }
    // corruptionHandler 未指定
)

// 好：明确处理
val dataStore = DataStoreFactory.create(
    serializer = MySerializer,
    corruptionHandler = ReplaceFileCorruptionHandler { _ ->
        Logger.warn("数据损坏，使用默认值")
        MyData()
    },
    produceFile = { File("data.pb") }
)
```

### 5. ❌ 在 collect 中进行长时间操作

```kotlin
// 不好：阻塞 Flow
dataStore.data.collect { config ->
    Thread.sleep(1000)  // 会阻塞所有订阅者
    updateUI(config)
}

// 好：使用 flowOn 切换线程
dataStore.data
    .flowOn(Dispatchers.IO)
    .collect { config ->
        val processed = expensiveComputation(config)
        withContext(Dispatchers.Default) {
            updateUI(processed)
        }
    }
```

---

## 总结检查清单

- [ ] 使用对象委托管理全局 DataStore 实例
- [ ] Preferences 修改时使用单个 `edit` 块
- [ ] 批量操作合并为一次磁盘写入
- [ ] 正确处理 IOException 和 CorruptionException
- [ ] 实现 corruptionHandler 进行数据恢复
- [ ] 为自定义类型实现 Serializer
- [ ] 测试原子性和并发场景
- [ ] 缓存热数据减少磁盘访问
- [ ] 使用 Flow 进行响应式数据订阅
- [ ] 在非 UI 线程执行所有 DataStore 操作
