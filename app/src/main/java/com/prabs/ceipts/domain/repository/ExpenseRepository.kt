package com.prabs.ceipts.domain.repository

import com.prabs.ceipts.data.local.entity.ExpenseEntity
import com.prabs.ceipts.data.local.entity.ExpensePayerEntity
import com.prabs.ceipts.data.local.entity.ExpenseSplitEntity
import kotlinx.coroutines.flow.Flow

interface ExpenseRepository {
    fun getExpensesForGroup(groupId: String): Flow<List<ExpenseEntity>>
    suspend fun createExpense(
        expense: ExpenseEntity, 
        payers: List<ExpensePayerEntity>, 
        splits: List<ExpenseSplitEntity>
    )
    suspend fun updateExpense(
        expense: ExpenseEntity, 
        payers: List<ExpensePayerEntity>, 
        splits: List<ExpenseSplitEntity>
    )
    suspend fun deleteExpense(expenseId: String)
    suspend fun getExpenseById(id: String): ExpenseEntity?
    suspend fun getSplitsForExpense(expenseId: String): List<ExpenseSplitEntity>

    fun getAllExpenses(): Flow<List<ExpenseEntity>>
    fun getAllExpenseSplits(): Flow<List<ExpenseSplitEntity>>
    fun getAllExpensePayers(): Flow<List<ExpensePayerEntity>>
}
