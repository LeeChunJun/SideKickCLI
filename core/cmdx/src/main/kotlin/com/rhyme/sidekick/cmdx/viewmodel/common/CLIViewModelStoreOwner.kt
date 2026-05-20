package com.rhyme.sidekick.cmdx.viewmodel.common

import com.rhyme.sidekick.cmdx.viewmodel.core.ViewModelStore

/**
 * ViewModelStore 持有至
 */
class CLIViewModelStoreOwner : ViewModelStoreOwner {

    private val map = mutableMapOf<String, ViewModelStore>()

    override val viewModelStore: ViewModelStore
        get() = getOrCreate("_default") { ViewModelStore() } // 采用 _default 避免和用户定义冲突

    fun getOrCreate(key: String, factory: () -> ViewModelStore): ViewModelStore =
        map.getOrPut(key) { factory() }

    fun clear() {
        map.values.forEach { it.clear() }
        map.clear()
    }
}