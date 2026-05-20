package com.rhyme.sidekick.cmdx.room.runtime

/**
 * 核心注解
 */
@Target(AnnotationTarget.CLASS)
annotation class Entity(val tableName: String = "")

@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY)
annotation class PrimaryKey(val autoGenerate: Boolean = false)

@Target(AnnotationTarget.CLASS)
annotation class Dao

@Target(AnnotationTarget.FUNCTION)
annotation class Query(val value: String)

@Target(AnnotationTarget.FUNCTION)
annotation class Insert

@Target(AnnotationTarget.FUNCTION)
annotation class Update

@Target(AnnotationTarget.FUNCTION)
annotation class Delete