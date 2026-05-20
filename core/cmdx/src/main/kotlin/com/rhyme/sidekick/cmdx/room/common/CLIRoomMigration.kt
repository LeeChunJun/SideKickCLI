package com.rhyme.sidekick.cmdx.room.common

import java.sql.Connection

/**
 * 数据库迁移
 */
data class CLIRoomMigration(
    val startVersion: Int,
    val endVersion: Int,
    val migrate: (Connection) -> Unit
)