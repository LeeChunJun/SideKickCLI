package com.rhyme.sidekick.cmdx.viewmodel.core

/**
 * ViewModelStore：实际持有者
 */
class ViewModelStore {
    private val map = mutableMapOf<String, ViewModel>()

    fun put(key: String, viewModel: ViewModel) {
        val oldViewModel = map.put(key, viewModel)
        oldViewModel?.clear()
    }

    fun get(key: String): ViewModel? = map[key]

    @Suppress("UNCHECKED_CAST")
    fun <T : ViewModel> getOrCreate(key: String, factory: () -> T): T {
        return map.getOrPut(key) { factory() } as T
    }

    fun clear() {
        map.values.forEach { it.clear() }
        map.clear()
    }
}