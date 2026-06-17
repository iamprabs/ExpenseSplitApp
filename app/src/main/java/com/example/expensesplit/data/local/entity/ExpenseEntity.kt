package com.example.expensesplit.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val groupId: String,
    val title: String,
    val amount: Double,
    val baseAmount: Double = amount, // Normalized to INR
    val currencyCode: String = "INR",
    val category: String = "General",
    val notes: String? = null,
    val receiptUri: String? = null,
    val date: Long,
    val isRecurring: Boolean = false,
    val recurrenceRule: String? = null,
    val createdBy: String,
    val createdAt: Long,
    val updatedAt: Long,
    val deletedAt: Long?,
    val version: Int = 1,
    val syncStatus: SyncStatus = SyncStatus.PENDING
)
