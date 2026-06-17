package com.example.expensesplit.data.local.entity

import androidx.room.Entity

@Entity(tableName = "expense_splits", primaryKeys = ["expenseId", "userId"])
data class ExpenseSplitEntity(
    val expenseId: String,
    val userId: String,
    val shareType: String = "EQUAL",
    val value: Double = 0.0,
    val computedAmount: Double = 0.0
)
