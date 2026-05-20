package com.rhyme.sidekick.cmdx.framework

import kotlin.reflect.KClass

/**
 * 服务管理器
 */
class CLIServiceManager {

    // 服务注册表，用于存储和检索服务实例
    private val serviceRegistry = mutableMapOf<KClass<*>, Any>()

    // 服务工厂，用于懒加载创建服务实例
    private val serviceFactories = mutableMapOf<KClass<*>, () -> Any>()


    /**
     * 获取指定类型的服务实例
     * @param clazz 服务类型的KClass
     * @return 服务实例
     */
    @Suppress("UNCHECKED_CAST")
    fun <T : Any> getService(clazz: KClass<T>): T {
        // 首先检查是否已存在实例
        val existingInstance = serviceRegistry[clazz]
        if (existingInstance != null) {
            return existingInstance as T
        }

        // 如果不存在但有工厂函数，则使用工厂创建实例
        val factory = serviceFactories[clazz]
        if (factory != null) {
            val newInstance = factory()
            serviceRegistry[clazz] = newInstance
            return newInstance as T
        }

        throw IllegalArgumentException("Service not found for class: ${clazz.simpleName}")
    }

    /**
     * 获取指定类型的服务实例（reified版本）
     * @return 服务实例
     */
    inline fun <reified T : Any> getService(): T {
        return getService(T::class)
    }

    /**
     * 注入指定类型的服务实例（懒加载）
     * @param clazz 服务类型的KClass
     * @return 懒加载的服务实例
     */
    fun <T : Any> inject(clazz: KClass<T>): Lazy<T> {
        return lazy { getService(clazz) }
    }

    /**
     * 注入指定类型的服务实例（reified版本，懒加载）
     * @return 懒加载的服务实例
     */
    inline fun <reified T : Any> inject(): Lazy<T> {
        return inject(T::class)
    }

    /**
     * 注册服务实例
     * @param clazz 服务类型的KClass
     * @param instance 服务实例
     */
    fun <T : Any> registerService(clazz: KClass<T>, instance: T) {
        serviceRegistry[clazz] = instance
    }

    /**
     * 注册服务实例（reified版本）
     * @param instance 服务实例
     */
    inline fun <reified T : Any> registerService(instance: T) {
        registerService(T::class, instance)
    }

    /**
     * 注册服务工厂函数（用于懒加载）
     * @param clazz 服务类型的KClass
     * @param factory 创建服务实例的工厂函数
     */
    fun <T : Any> registerServiceFactory(clazz: KClass<T>, factory: () -> T) {
        serviceFactories[clazz] = factory
    }

    /**
     * 注册服务工厂函数（使用reified类型参数）
     * @param factory 创建服务实例的工厂函数
     */
    inline fun <reified T : Any> registerServiceFactory(noinline factory: () -> T) {
        registerServiceFactory(T::class, factory)
    }
}