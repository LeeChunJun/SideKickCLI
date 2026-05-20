package com.rhyme.sidekick.cmdx.datastore.core

/**
 * 数据版本迁移接口
 */
interface DataMigration<T> {
    /**
     * 执行数据迁移
     * @param currentData 当前数据
     * @return 迁移后的数据
     */
    suspend fun migrate(currentData: T): T

    /**
     * 检查是否需要执行此迁移
     */
    suspend fun shouldMigrate(currentData: T): Boolean = true
}