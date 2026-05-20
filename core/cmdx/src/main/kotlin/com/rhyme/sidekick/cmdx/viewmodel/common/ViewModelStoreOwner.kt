package com.rhyme.sidekick.cmdx.viewmodel.common

import com.rhyme.sidekick.cmdx.viewmodel.core.ViewModelStore

/**
 * 持有 ViewModelStore
 */
interface ViewModelStoreOwner {
    val viewModelStore: ViewModelStore
}