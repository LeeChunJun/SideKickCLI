package com.rhyme.sidekick.cmdx.viewmodel.core

/**
 * 负责创建 ViewModel 实例
 */
class ViewModelProvider(
    private val store: ViewModelStore,
    private val factory: Factory
) {
    interface Factory {
        fun <T : ViewModel> create(modelClass: Class<T>): T
    }

    // 默认工厂实现：使用反射
    class NewInstanceFactory : Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return try {
                modelClass.getDeclaredConstructor().newInstance()
            } catch (e: Exception) {
                throw RuntimeException("Cannot create an instance of $modelClass", e)
            }
        }
    }

    fun <T : ViewModel> get(modelClass: Class<T>): T {
        val key = "jvm.viewmodel.store.DefaultKey:" + modelClass.canonicalName
        val viewModel = store.get(key)

        if (modelClass.isInstance(viewModel)) {
            return modelClass.cast(viewModel)
        }

        val newViewModel = factory.create(modelClass)
        store.put(key, newViewModel)
        return newViewModel
    }
}