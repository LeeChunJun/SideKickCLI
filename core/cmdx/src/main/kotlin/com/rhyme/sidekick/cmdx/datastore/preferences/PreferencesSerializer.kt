package com.rhyme.sidekick.cmdx.datastore.preferences

import com.rhyme.sidekick.cmdx.datastore.core.CorruptionException
import com.rhyme.sidekick.cmdx.datastore.core.Serializer
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

/**
 * Preferences 序列化器 - 使用带类型标记的文本格式存储
 * 
 * 格式: key:type=value
 * 类型标记:
 *   B = Boolean
 *   I = Int
 *   L = Long
 *   F = Float
 *   D = Double
 *   S = String
 *   T = Set<String> (seT)
 * 
 * 示例:
 *   username:S=张三
 *   age:I=25
 *   score:F=98.5
 *   precision:D=3.141592653589793
 *   enabled:B=true
 *   tags:T=[开发者,技术爱好者]
 *   count:L=9223372036854775807
 */
object PreferencesSerializer : Serializer<Preferences> {
    override val defaultValue: Preferences = MutablePreferences(startFrozen = true)

    override suspend fun readFrom(input: InputStream): Preferences {
        try {
            val text = input.bufferedReader().use { it.readText() }
            if (text.isEmpty()) {
                return MutablePreferences(startFrozen = true)
            }

            val data = mutableMapOf<Preferences.Key<*>, Any>()
            val lines = text.split("\n").filter { it.isNotEmpty() }

            lines.forEach { line ->
                val colonIndex = line.indexOf(':')
                val equalsIndex = line.indexOf('=', colonIndex + 1)
                
                if (colonIndex > 0 && equalsIndex > colonIndex + 1) {
                    val key = line.substring(0, colonIndex).trim()
                    val type = line.substring(colonIndex + 1, equalsIndex).trim()
                    val value = line.substring(equalsIndex + 1).trim()

                    // 显式的类型解析
                    val actualValue = when (type) {
                        "B" -> {  // Boolean
                            value.toBoolean()
                        }
                        "I" -> {  // Int
                            value.toIntOrNull() 
                                ?: throw CorruptionException("无法解析 Int 值: $value")
                        }
                        "L" -> {  // Long
                            value.toLongOrNull() 
                                ?: throw CorruptionException("无法解析 Long 值: $value")
                        }
                        "F" -> {  // Float
                            value.toFloatOrNull() 
                                ?: throw CorruptionException("无法解析 Float 值: $value")
                        }
                        "D" -> {  // Double
                            value.toDoubleOrNull()
                                ?: throw CorruptionException("无法解析 Double 值: $value")
                        }
                        "S" -> {  // String
                            unescapeString(value)
                        }
                        "T" -> {  // Set<String>
                            parseStringSet(value)
                        }
                        else -> throw CorruptionException("未知的类型标记: $type")
                    }

                    @Suppress("UNCHECKED_CAST")
                    val typedKey = Preferences.Key<Any>(key)
                    data[typedKey] = actualValue
                }
            }

            return MutablePreferences(data, startFrozen = true)
        } catch (ex: CorruptionException) {
            throw ex
        } catch (ex: Exception) {
            throw CorruptionException("Failed to deserialize preferences", ex)
        }
    }

    override suspend fun writeTo(t: Preferences, output: OutputStream) {
        try {
            val lines = t.asMap().map { (key, value) ->
                val (type, formattedValue) = when (value) {
                    is Boolean -> "B" to value.toString()
                    is Int -> "I" to value.toString()
                    is Long -> "L" to value.toString()
                    is Float -> "F" to value.toString()
                    is Double -> "D" to value.toString()
                    is String -> "S" to escapeString(value)
                    is Set<*> -> "T" to formatStringSet(value)
                    else -> throw IOException("不支持的类型: ${value.javaClass}")
                }
                "${key.name}:$type=$formattedValue"
            }

            output.bufferedWriter().use { writer ->
                writer.write(lines.joinToString("\n"))
                writer.flush()
            }
        } catch (ex: IOException) {
            throw ex
        } catch (ex: Exception) {
            throw IOException("Failed to serialize preferences", ex)
        }
    }

    /**
     * 转义字符串值，处理特殊字符
     * - 引号转义为 \"
     * - 换行转义为 \n
     * - 等号转义为 \=（虽然格式中不会出现，但为了安全）
     */
    private fun escapeString(value: String): String {
        return value
            .replace("\\", "\\\\")  // 先转义反斜杠
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
    }

    /**
     * 反转义字符串值
     */
    private fun unescapeString(value: String): String {
        return value
            .replace("\\r", "\r")
            .replace("\\n", "\n")
            .replace("\\\"", "\"")
            .replace("\\\\", "\\")  // 最后转义反斜杠
    }

    /**
     * 格式化 Set<String> 为字符串
     * 格式: [item1,item2,item3]
     * 每个 item 用引号包围
     */
    private fun formatStringSet(set: Set<*>): String {
        val items = set.joinToString(",") { "\"${escapeString(it.toString())}\"" }
        return "[$items]"
    }

    /**
     * 解析 Set<String> 从字符串
     * 格式: [item1,item2,item3]
     */
    private fun parseStringSet(value: String): Set<String> {
        if (!value.startsWith("[") || !value.endsWith("]")) {
            throw CorruptionException("无效的 Set 格式: $value")
        }
        
        val content = value.substring(1, value.length - 1)
        if (content.isEmpty()) {
            return emptySet()
        }

        // 简单的 CSV 解析（处理带引号的值）
        val items = mutableSetOf<String>()
        var current = StringBuilder()
        var inQuotes = false
        var i = 0
        
        while (i < content.length) {
            when {
                content[i] == '\\' && i + 1 < content.length -> {
                    // 处理转义序列
                    current.append(content[i])
                    i++
                    if (i < content.length) {
                        current.append(content[i])
                        i++
                    }
                }
                content[i] == '"' -> {
                    inQuotes = !inQuotes
                    current.append(content[i])
                    i++
                }
                content[i] == ',' && !inQuotes -> {
                    // 分隔符
                    val item = current.toString().trim()
                    if (item.startsWith("\"") && item.endsWith("\"")) {
                        items.add(unescapeString(item.substring(1, item.length - 1)))
                    }
                    current = StringBuilder()
                    i++
                }
                else -> {
                    current.append(content[i])
                    i++
                }
            }
        }
        
        // 处理最后一个项目
        if (current.isNotEmpty()) {
            val item = current.toString().trim()
            if (item.startsWith("\"") && item.endsWith("\"")) {
                items.add(unescapeString(item.substring(1, item.length - 1)))
            }
        }
        
        return items
    }
}