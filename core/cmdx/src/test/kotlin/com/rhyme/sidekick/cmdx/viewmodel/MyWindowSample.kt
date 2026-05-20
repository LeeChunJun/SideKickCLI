package com.rhyme.sidekick.cmdx.viewmodel

import com.rhyme.sidekick.cmdx.viewmodel.core.ViewModelStore
import com.rhyme.sidekick.cmdx.viewmodel.common.ViewModelStoreOwner
import com.rhyme.sidekick.cmdx.viewmodel.common.viewModels
import kotlin.getValue

/**
 * 样例
 */
class MyWindowSample : ViewModelStoreOwner {
    override val viewModelStore = ViewModelStore()

    // 使用委托获取
    val userViewModel: UserViewModel by viewModels()

    fun onClose() {
        viewModelStore.clear() // 模拟生命周期结束
    }
}

fun main() {
    val window = MyWindowSample()
    println(window.userViewModel.state.value)

    // 模拟运行一段时间后关闭
    Thread.sleep(2000)
    println(window.userViewModel.state.value)
    window.onClose()
}