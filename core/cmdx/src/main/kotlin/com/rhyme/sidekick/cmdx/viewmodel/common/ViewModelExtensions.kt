package com.rhyme.sidekick.cmdx.viewmodel.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.rhyme.sidekick.cmdx.viewmodel.core.ViewModel
import com.rhyme.sidekick.cmdx.viewmodel.core.ViewModelProvider
import com.rhyme.sidekick.cmdx.viewmodel.core.ViewModelStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import java.io.Closeable
import kotlin.coroutines.CoroutineContext

private const val JOB_KEY = "jvm.viewmodel.scope.JobKey"

// 拓展属性 viewModel 上的协程
val ViewModel.viewModelScope: CoroutineScope
    get() {
        val scope: CoroutineScope? = this.setTagIfAbsent(
            JOB_KEY, CloseableCoroutineScope(
                SupervisorJob() + Dispatchers.Default // 注意：JVM环境需确保有Main调度器，否则改为Default
            )
        )
        return scope!!
    }

// 包装 CoroutineScope 使其可关闭
class CloseableCoroutineScope(context: CoroutineContext) : Closeable, CoroutineScope {
    override val coroutineContext: CoroutineContext = context
    override fun close() {
        coroutineContext.cancel()
    }
}

// 扩展方法，类似 Android 的 by viewModels()
inline fun <reified T : ViewModel> ViewModelStoreOwner.viewModels(
    noinline factoryProducer: (() -> ViewModelProvider.Factory)? = null
): Lazy<T> {
    val factoryPromise = factoryProducer ?: { ViewModelProvider.NewInstanceFactory() }
    return ViewModelLazy(T::class, { this.viewModelStore }, factoryPromise)
}

// 拓展方法，在 Compose 中使用 cliViewModel 获取 ViewModel（会在内部使用提供的 ViewModelStore）
// 例如：val userVm: UserViewModel = cliViewModel("user") { ViewModelStore() }
@Composable
inline fun <reified T : ViewModel> cliViewModel(
    key: String,
    noinline scopeFactory: () -> ViewModelStore
): T {
    val storeOwner = LocalCLIViewModelStoreOwner.current

    val store = remember(key, scopeFactory) {
        storeOwner.getOrCreate(key, scopeFactory)
    }

    // 使用单例工厂避免重复创建
    val viewModel = remember(store) {
        val viewModelFactory = ViewModelProvider.NewInstanceFactory()
        ViewModelProvider(store, viewModelFactory).get(T::class.java)
    }

    return viewModel
}