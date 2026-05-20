package com.rhyme.sidekick.cmdx.viewmodel.common

import com.rhyme.sidekick.cmdx.viewmodel.core.ViewModel
import com.rhyme.sidekick.cmdx.viewmodel.core.ViewModelProvider
import com.rhyme.sidekick.cmdx.viewmodel.core.ViewModelStore
import kotlin.reflect.KClass

/**
 * ViewModel 延迟加载和缓存
 */
class ViewModelLazy<T : ViewModel>(
    private val viewModelClass: KClass<T>,
    private val storeProducer: () -> ViewModelStore,
    private val factoryProducer: () -> ViewModelProvider.Factory
) : Lazy<T> {
    private var cached: T? = null

    override val value: T
        get() {
            val vm = cached
            return if (vm == null) {
                val factory = factoryProducer()
                val store = storeProducer()
                ViewModelProvider(store, factory).get(viewModelClass.java).also {
                    cached = it
                }
            } else {
                vm
            }
        }

    override fun isInitialized(): Boolean = cached != null
}