package com.example.expensesplit.domain.repository

import com.example.expensesplit.data.local.entity.ExpenseEntity
import com.example.expensesplit.data.local.entity.ExpensePayerEntity
import com.example.expensesplit.data.local.entity.ExpenseSplitEntity
import kotlinx.coroutines.flow.Flow

interface ExpenseRepository {
    fun getExpensesForGroup(groupId: String): Flow<List<ExpenseEntity>>
    suspend fun createExpense(
        expense: ExpenseEntity, 
        payers: List<ExpensePayerEntity>, 
        splits: List<ExpenseSplitEntity>
    )
    suspend fun deleteExpense(expenseId: String)
}
