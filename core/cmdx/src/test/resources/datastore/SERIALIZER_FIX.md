# PreferencesSerializer 类型处理 Bug 修复

## 问题描述

### 发现的缺陷

`PreferencesSerializer` 的 `readFrom` 方法存在 **类型处理不完整** 的严重问题：

- ✅ **显式处理**: Boolean, Long, Float, Set<String>
- ❌ **缺失处理**: Int, String (依赖隐式推断)

而 `Preferences` 声称支持: `Boolean, Int, Long, Float, String, Set<String>`

### 具体问题

#### 问题 1: String 类型的隐式处理
```kotlin
// 当前实现
val actualValue = when {
    value == "true" || value == "false" -> value.toBoolean()
    value.startsWith("[") && value.endsWith("]") -> { /* Set */ }
    value.toLongOrNull() != null -> { /* Long 或 Int */ }
    value.toFloatOrNull() != null -> value.toFloat()
    else -> value  // ❌ String 被隐式处理！
}

// 问题案例
prefs[stringKey("count")] = "123"  // 本意：字符串

// 读取时：
// "123" 匹配 toLongOrNull() ✓
// 返回: 123L 或 123 (Int)  ❌ 类型错误！
```

#### 问题 2: Int 类型的脆弱依赖
```kotlin
// Int 依赖 Long 分支的范围检查
when {
    value.toLongOrNull() != null -> {
        val longVal = value.toLong()
        when {
            longVal in Int.MIN_VALUE..Int.MAX_VALUE -> longVal.toInt()  // ⚠️ 侥幸成功
            else -> longVal
        }
    }
}

// 边界情况：如果 Long 超出 Int 范围，会错误地返回 Long！
prefs[intKey("big")] = Int.MAX_VALUE
// 读取时仍然是 Int，但这是巧合，不是设计
```

#### 问题 3: 写入端不对称
```kotlin
// writeTo 中的处理
when(word) {
    is String -> if (value.contains(" ")) "\"$value\"" else value
    // ⚠️ 仅对有空格的字符串加引号
    // 如果字符串是 "123"，不会加引号
    // 读取时会被识别为数字！

    is Set<*> -> "[" + value.joinToString(",") { "\"$it\"" } + "]"
    // Set 中的字符串没有转义处理
    // 如果包含 "," 或 "\n" 会导致解析错误
}
```

---

## 修复方案

### 新的序列化格式

采用 **显式类型标记** 格式：`key:type=value`

#### 类型标记对照表

| 类型          | 标记  | 示例                              |
|-------------|-----|---------------------------------|
| Boolean     | `B` | `enabled:B=true`                |
| Int         | `I` | `age:I=25`                      |
| Long        | `L` | `count:L=9223372036854775807`   |
| Float       | `F` | `score:F=98.5`                  |
| Double      | `D` | `precision:D=3.141592653589793` |
| String      | `S` | `name:S=张三`                     |
| Set<String> | `T` | `tags:T=["开发者","技术爱好者"]`        |

#### 优势

1. ✅ **明确的类型信息** - 不需要推断，直接按类型解析
2. ✅ **不易混淆** - "123" 作为 String 和 Int 完全不同
3. ✅ **完整的转义支持** - 正确处理特殊字符
4. ✅ **向后兼容** - 可以检测旧格式并升级
5. ✅ **易于扩展** - 将来添加新类型时只需新增标记

### 实现细节

#### 1. readFrom - 显式类型解析

```kotlin
override suspend fun readFrom(input: InputStream): Preferences {
    val lines = text.split("\n")
    
    lines.forEach { line ->
        val colonIndex = line.indexOf(':')
        val equalsIndex = line.indexOf('=', colonIndex + 1)
        
        val key = line.substring(0, colonIndex)
        val type = line.substring(colonIndex + 1, equalsIndex)
        val value = line.substring(equalsIndex + 1)

        // ✅ 显式处理每种类型
        val actualValue = when (type) {
            "B" -> value.toBoolean()
            "I" -> value.toIntOrNull() ?: error("无法解析 Int")
            "L" -> value.toLongOrNull() ?: error("无法解析 Long")
            "F" -> value.toFloatOrNull() ?: error("无法解析 Float")
            "S" -> unescapeString(value)  // ✅ 正确处理转义
            "T" -> parseStringSet(value)  // ✅ 正确处理 Set
            else -> error("未知类型")
        }
    }
}
```

#### 2. writeTo - 显式类型记录

```kotlin
override suspend fun writeTo(t: Preferences, output: OutputStream) {
    val lines = t.asMap().map { (key, value) ->
        // ✅ 为每种类型显式生成标记
        val (type, formattedValue) = when (value) {
            is Boolean -> "B" to value.toString()
            is Int -> "I" to value.toString()
            is Long -> "L" to value.toString()
            is Float -> "F" to value.toString()
            is String -> "S" to escapeString(value)  // ✅ 所有字符串都转义
            is Set<*> -> "T" to formatStringSet(value)
            else -> error("不支持的类型")
        }
        "${key.name}:$type=$formattedValue"
    }
}
```

#### 3. 转义处理

```kotlin
private fun escapeString(value: String): String {
    return value
        .replace("\\", "\\\\")  // 反斜杠
        .replace("\"", "\\\"")  // 引号
        .replace("\n", "\\n")   // 换行
        .replace("\r", "\\r")   // 回车
}

private fun unescapeString(value: String): String {
    // 反向处理
}
```

---

## 修复前后对比

### 测试用例

```kotlin
// 混合类型的 Preferences
dataStore.edit { prefs ->
    prefs[booleanKey("enabled")] = true
    prefs[intKey("age")] = 25
    prefs[longKey("count")] = 9223372036854775807L
    prefs[floatKey("score")] = 98.5f
    prefs[stringKey("name")] = "张三"
    prefs[stringKey("number_str")] = "123"  // 关键：字符串，不是数字
    prefs[stringSetKey("tags")] = setOf("开发者", "Kotlin,爱好者")  // 包含分隔符
}
```

#### 修复前 (旧格式)
```
enabled=true
age=25  (or 123L if 25.toLongOrNull() matched first?)
count=9223372036854775807
score=98.5
name=张三
number_str=123  (❌ 会被识别为 Int!)
tags=["开发者","Kotlin,爱好者"]  (❌ CSV 解析会失败)
```

#### 修复后 (新格式)
```
enabled:B=true
age:I=25
count:L=9223372036854775807
score:F=98.5
name:S=张三
number_str:S=123
tags:T=["开发者","Kotlin,爱好者"]
```

### 完全避免的问题

| 场景             | 修复前          | 修复后     |
|----------------|--------------|---------|
| `"123"` 作为字符串  | ❌ 转为 Int     | ✅ 保持字符串 |
| `"true"` 作为字符串 | ❌ 转为 Boolean | ✅ 保持字符串 |
| 字符串包含 `,`      | ❌ Set 解析失败   | ✅ 正确转义  |
| 字符串包含 `\n`     | ❌ 格式破坏       | ✅ 正确转义  |
| 边界 Int 值       | ⚠️ 侥幸成功      | ✅ 显式保证  |

---

## 向后兼容性

### 迁移策略

由于修改了序列化格式，**旧数据无法直接读取**。建议：

#### 方案 1: 清除所有旧数据 (快速)
```kotlin
// 在应用更新时清除
if (isFirstLaunch) {
    File("preferences.preferences_pb").delete()
}
```

#### 方案 2: 自动升级 (保留数据)
```kotlin
object MigratePreferencesV1ToV2 : DataMigration<Preferences> {
    override suspend fun shouldMigrate(currentData: Preferences): Boolean {
        // 检查是否是旧格式（没有 ':' 分隔符）
        val isOldFormat = currentData.asMap().isEmpty()
        return isOldFormat
    }
    
    override suspend fun migrate(currentData: Preferences): Preferences {
        // 尝试解析旧格式，转换为新格式
        // 如果失败则返回默认值
        return currentData
    }
}
```

#### 方案 3: 灵活读取 (最稳妥)
```kotlin
override suspend fun readFrom(input: InputStream): Preferences {
    val text = input.bufferedReader().use { it.readText() }
    
    // 检测格式版本
    return if (isNewFormat(text)) {
        readNewFormat(text)      // 新格式
    } else {
        readOldFormat(text)      // 旧格式（向后兼容）
    }
}

private fun isNewFormat(text: String): Boolean {
    // 新格式包含 ':' 类型标记
    return text.contains(Regex("\\w+:\\w+="))
}
```

---

## 验证和测试

### 单元测试

```kotlin
class PreferencesSerializerTest {
    
    @Test
    fun testAllSupportedTypes() = runBlocking {
        val prefs = MutablePreferences(startFrozen = false).apply {
            this[booleanKey("b")] = true
            this[intKey("i")] = 42
            this[longKey("l")] = Long.MAX_VALUE
            this[floatKey("f")] = 3.14f
            this[stringKey("s")] = "text"
            this[stringSetKey("t")] = setOf("a", "b")
        }
        
        // 序列化和反序列化
        val bytes = ByteArrayOutputStream()
        PreferencesSerializer.writeTo(prefs, bytes)
        
        val loaded = PreferencesSerializer.readFrom(
            ByteArrayInputStream(bytes.toByteArray())
        )
        
        // 验证所有类型
        assertEquals(true, loaded[booleanKey("b")])
        assertEquals(42, loaded[intKey("i")])
        assertEquals(Long.MAX_VALUE, loaded[longKey("l")])
        assertEquals(3.14f, loaded[floatKey("f")])
        assertEquals("text", loaded[stringKey("s")])
        assertEquals(setOf("a", "b"), loaded[stringSetKey("t")])
    }
    
    @Test
    fun testStringAndNumberDistinction() = runBlocking {
        // 关键测试：字符串 "123" 不应转为数字
        val prefs = MutablePreferences(startFrozen = false).apply {
            this[stringKey("num")] = "123"
            this[intKey("int")] = 123
        }
        
        val restored = roundTrip(prefs)
        
        val numValue = restored[stringKey("num")]
        val intValue = restored[intKey("int")]
        
        assertTrue(numValue is String)
        assertEquals("123", numValue)
        assertTrue(intValue is Int)
        assertEquals(123, intValue)
    }
    
    @Test
    fun testSpecialCharactersInString() = runBlocking {
        val prefs = MutablePreferences(startFrozen = false).apply {
            this[stringKey("special")] = "line1\nline2\ttab\"quoted\""
        }
        
        val restored = roundTrip(prefs)
        assertEquals("line1\nline2\ttab\"quoted\"", restored[stringKey("special")])
    }
    
    @Test
    fun testSetWithSpecialCharacters() = runBlocking {
        val prefs = MutablePreferences(startFrozen = false).apply {
            this[stringSetKey("set")] = setOf("a,b", "c\"d", "e\nf")
        }
        
        val restored = roundTrip(prefs)
        assertEquals(setOf("a,b", "c\"d", "e\nf"), restored[stringSetKey("set")])
    }
    
    private suspend fun roundTrip(prefs: Preferences): Preferences {
        val bytes = ByteArrayOutputStream()
        PreferencesSerializer.writeTo(prefs, bytes)
        return PreferencesSerializer.readFrom(
            ByteArrayInputStream(bytes.toByteArray())
        )
    }
}
```

---

## 总结

### 修复的问题

| 问题                  | 原因        | 修复方式        |
|---------------------|-----------|-------------|
| String "123" 转为 Int | 隐式类型推断    | 显式类型标记      |
| Int 依赖 Long 范围检查    | 缺少显式处理    | 独立的 Int 处理  |
| 特殊字符导致解析失败          | 没有转义机制    | 完整的转义/反转义   |
| Set 中的分隔符导致错误       | CSV 解析不安全 | 带转义的 CSV 解析 |

### 代码质量提升

- ✅ 类型安全：从推断式 → 显式式
- ✅ 错误处理：从容错 → 严格验证
- ✅ 可维护性：从隐含 → 明确
- ✅ 可扩展性：从固定 → 灵活（可添加新类型）

### 建议的后续步骤

1. 运行所有单元测试验证修复
2. 根据业务需求选择向后兼容策略
3. 发布版本更新说明
4. 监控用户反馈
