package com.rhyme.sidekick.cmdx.framework

import com.rhyme.sidekick.cmdx.lifecycle.common.CLILifecycleOwner
import com.rhyme.sidekick.cmdx.navigation.simple.CLINavigator
import com.rhyme.sidekick.cmdx.room.runtime.CLIRoomDatabase
import com.rhyme.sidekick.cmdx.viewmodel.common.CLIViewModelStoreOwner
import kotlinx.coroutines.CoroutineScope
import kotlin.reflect.KClass

/**
 * 项目名：SideKickCLI
 * 包名：com.rhyme.sidekick.framework
 * 描述：App 上下文
 *
 * @author 李春俊-Rhyme
 * @since 20:02 2026/4/22 星期三
 * @version 1.0
 */
interface CLIContext {
    val config: CLIConfig
    val resources: CLIResources
    val preferences: CLIPreferences
    val database: CLIRoomDatabase
    val navigator: CLINavigator
    val lifecycleOwner: CLILifecycleOwner
    val viewModelStoreOwner: CLIViewModelStoreOwner
    val serviceManager: CLIServiceManager
    val coroutineScope: CoroutineScope

    /**
     * 获取指定类型的服务实例
     * @param clazz 服务类型的KClass
     * @return 服务实例
     */
    fun <T : Any> getService(clazz: KClass<T>): T

    /**
     * 注入指定类型的服务实例（懒加载）
     * @param clazz 服务类型的KClass
     * @return 懒加载的服务实例
     */
    fun <T : Any> inject(clazz: KClass<T>): Lazy<T>

    /**
     * 注册服务实例
     * @param clazz 服务类型的KClass
     * @param instance 服务实例
     */
    fun <T : Any> registerService(clazz: KClass<T>, instance: T)

    /**
     * 注册服务工厂函数（用于懒加载）
     * @param clazz 服务类型的KClass
     * @param factory 创建服务实例的工厂函数
     */
    fun <T : Any> registerServiceFactory(clazz: KClass<T>, factory: () -> T)
}

