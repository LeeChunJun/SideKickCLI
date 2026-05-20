package com.rhyme.sidekick.cmdx.datastore.preferences

import com.rhyme.sidekick.cmdx.datastore.core.DataStore
import java.util.Collections
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Preferences 数据模型，类似于一个 Map，用于存储简单的键值对
 * 支持的类型：Boolean, Int, Long, Float, Double, String, Set<String>
 */
abstract class Preferences constructor() {
    /**
     * Preferences 的 Key，与特定类型的值关联
     */
    class Key<T> constructor(val name: String) {
        infix fun to(value: T): Pair<T> = Pair(this, value)
        override fun equals(other: Any?): Boolean =
            if (other is Key<*>) name == other.name else false

        override fun hashCode(): Int = name.hashCode()
        override fun toString(): String = name
    }

    /**
     * Key-Value 配对
     */
    class Pair<T> constructor(val key: Key<T>, val value: T)

    abstract operator fun <T> contains(key: Key<T>): Boolean
    abstract operator fun <T> get(key: Key<T>): T?
    abstract fun asMap(): Map<Key<*>, Any>

    fun toMutablePreferences(): MutablePreferences {
        return MutablePreferences(asMap().toMutableMap(), startFrozen = false)
    }
}

/**
 * 可变的 Preferences，用于修改数据
 */
class MutablePreferences constructor(
    val preferencesMap: MutableMap<Key<*>, Any> = mutableMapOf(),
    startFrozen: Boolean = true
) : Preferences() {

    private val frozen = AtomicBoolean(startFrozen)

    fun checkNotFrozen() {
        check(!frozen.get()) { "Preferences 被冻结后不能修改" }
    }

    fun freeze() {
        frozen.set(true)
    }

    override operator fun <T> contains(key: Key<T>): Boolean {
        return preferencesMap.containsKey(key)
    }

    override operator fun <T> get(key: Key<T>): T? {
        @Suppress("UNCHECKED_CAST")
        return preferencesMap[key] as T?
    }

    override fun asMap(): Map<Key<*>, Any> {
        return Collections.unmodifiableMap(preferencesMap)
    }

    operator fun <T> set(key: Key<T>, value: T?) {
        checkNotFrozen()
        when (value) {
            null -> preferencesMap.remove(key)
            is Set<*> -> preferencesMap[key] = Collections.unmodifiableSet(value.toSet())
            else -> preferencesMap[key] = value
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> remove(key: Key<T>): T? {
        checkNotFrozen()
        return preferencesMap.remove(key) as T?
    }

    fun clear() {
        checkNotFrozen()
        preferencesMap.clear()
    }

    override fun equals(other: Any?): Boolean {
        return if (other is MutablePreferences) {
            preferencesMap == other.preferencesMap
        } else {
            false
        }
    }

    override fun hashCode(): Int {
        return preferencesMap.hashCode()
    }

    override fun toString(): String =
        preferencesMap.entries.joinToString(separator = ", ", prefix = "{", postfix = "}") {
            "${it.key.name}=${it.value}"
        }
}

/**
 * 编辑 DataStore 中的 Preferences
 */
suspend fun DataStore<Preferences>.edit(
    transform: suspend (MutablePreferences) -> Unit
): Preferences {
    return this.updateData {
        it.toMutablePreferences().apply { transform(this) }
    }
}

// Preferences Key 创建函数
@JvmName("booleanKey")
fun booleanPreferencesKey(name: String): Preferences.Key<Boolean> = Preferences.Key(name)

@JvmName("intKey")
fun intPreferencesKey(name: String): Preferences.Key<Int> = Preferences.Key(name)

@JvmName("longKey")
fun longPreferencesKey(name: String): Preferences.Key<Long> = Preferences.Key(name)

@JvmName("floatKey")
fun floatPreferencesKey(name: String): Preferences.Key<Float> = Preferences.Key(name)

@JvmName("doubleKey")
fun doublePreferencesKey(name: String): Preferences.Key<Double> = Preferences.Key(name)

@JvmName("stringKey")
fun stringPreferencesKey(name: String): Preferences.Key<String> = Preferences.Key(name)

@JvmName("stringSetKey")
fun stringSetPreferencesKey(name: String): Preferences.Key<Set<String>> = Preferences.Key(name)
