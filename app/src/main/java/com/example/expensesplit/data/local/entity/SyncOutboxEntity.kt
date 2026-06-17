package com.example.expensesplit.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sync_outbox")
data class SyncOutboxEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val tableName: String,
    val recordId: String, // ID of the entity that was changed
    val operation: OperationType,
    val timestamp: Long,
    val payloadJson: String? = null // Optional serialized state for conflict resolution
)

enum class OperationType {
    INSERT,
    UPDATE,
    DELETE
}
