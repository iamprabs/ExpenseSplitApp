package com.prabs.ceipts.data.local.entity

import androidx.room.Entity

@Entity(tableName = "group_members", primaryKeys = ["groupId", "userId"])
data class GroupMemberEntity(
    val groupId: String,
    val userId: String,
    val role: String = "member",
    val joinedAt: Long,
    val syncStatus: SyncStatus = SyncStatus.PENDING
)
