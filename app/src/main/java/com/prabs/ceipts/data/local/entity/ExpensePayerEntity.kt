package com.prabs.ceipts.data.local.entity

import androidx.room.Entity

@Entity(tableName = "expense_payers", primaryKeys = ["expenseId", "userId"])
data class ExpensePayerEntity(
    val expenseId: String,
    val userId: String,
    val amount: Double
)
