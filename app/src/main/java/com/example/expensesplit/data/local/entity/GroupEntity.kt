package com.example.expensesplit.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "groups")
data class GroupEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String?,
    val coverUrl: String?,
    val createdBy: String,
    val createdAt: Long,
    val updatedAt: Long,
    val deletedAt: Long?,
    val syncStatus: SyncStatus = SyncStatus.PENDING
)

enum class SyncStatus {
    SYNCED,
    PENDING,
    DELETED
}
