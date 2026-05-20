package com.rhyme.sidekick.cmdx.datastore.core

import java.io.InputStream
import java.io.OutputStream

/**
 * DataStore 的序列化/反序列化接口
 */
interface Serializer<T> {
    /**
     * 文件不存在时返回的默认值
     */
    val defaultValue: T

    /**
     * 从输入流反序列化对象
     * @throws CorruptionException 如果数据无法反序列化
     */
    @Throws(CorruptionException::class)
    suspend fun readFrom(input: InputStream): T

    /**
     * 将对象序列化到输出流
     */
    suspend fun writeTo(t: T, output: OutputStream)
}