package com.rhyme.sidekick.cmdx.framework

import com.rhyme.sidekick.cmdx.lifecycle.common.CLILifecycleOwner
import com.rhyme.sidekick.cmdx.navigation.simple.CLINavigator
import com.rhyme.sidekick.cmdx.room.runtime.CLIRoomDatabase
import com.rhyme.sidekick.cmdx.viewmodel.common.CLIViewModelStoreOwner
import kotlinx.coroutines.CoroutineScope
import kotlin.reflect.KClass

/**
 * 项目名：SideKickCLI
 * 包名：com.rhyme.sidekick.cmdx.framework
 * 描述：CLIContext 默认实现类
 *
 * 提供完整的服务管理功能，支持服务实例注册和服务工厂注册
 *
 * @author 李春俊-Rhyme
 * @since 21:30 2026/5/6 星期三
 * @version 1.0
 */
class CLIContextImpl(
    override val config: CLIConfig,
    override val resources: CLIResources,
    override val preferences: CLIPreferences,
    override val database: CLIRoomDatabase,
    override val navigator: CLINavigator,
    override val lifecycleOwner: CLILifecycleOwner,
    override val viewModelStoreOwner: CLIViewModelStoreOwner,
    override val serviceManager: CLIServiceManager,
    override val coroutineScope: CoroutineScope
) : CLIContext {

    /**
     * 获取指定类型的服务实例
     * @param clazz 服务类型的KClass
     * @return 服务实例
     */
    override fun <T : Any> getService(clazz: KClass<T>): T {
        return serviceManager.getService(clazz)
    }

    /**
     * 获取指定类型的服务实例（reified版本）
     * @return 服务实例
     */
    inline fun <reified T : Any> getService(): T {
        return serviceManager.getService()
    }

    /**
     * 注入指定类型的服务实例（懒加载）
     * @param clazz 服务类型的KClass
     * @return 懒加载的服务实例
     */
    override fun <T : Any> inject(clazz: KClass<T>): Lazy<T> {
        return serviceManager.inject(clazz)
    }

    /**
     * 注入指定类型的服务实例（reified版本，懒加载）
     * @return 懒加载的服务实例
     */
    inline fun <reified T : Any> inject(): Lazy<T> {
        return serviceManager.inject()
    }

    /**
     * 注册服务实例
     * @param clazz 服务类型的KClass
     * @param instance 服务实例
     */
    override fun <T : Any> registerService(clazz: KClass<T>, instance: T) {
        serviceManager.registerService(clazz, instance)
    }

    /**
     * 注册服务实例（reified版本）
     * @param instance 服务实例
     */
    inline fun <reified T : Any> registerService(instance: T) {
        serviceManager.registerService(instance)
    }

    /**
     * 注册服务工厂函数（用于懒加载）
     * @param clazz 服务类型的KClass
     * @param factory 创建服务实例的工厂函数
     */
    override fun <T : Any> registerServiceFactory(clazz: KClass<T>, factory: () -> T) {
        serviceManager.registerServiceFactory(clazz, factory)
    }

    /**
     * 注册服务工厂函数（reified版本）
     * @param factory 创建服务实例的工厂函数
     */
    inline fun <reified T : Any> registerServiceFactory(noinline factory: () -> T) {
        serviceManager.registerServiceFactory(factory)
    }
}