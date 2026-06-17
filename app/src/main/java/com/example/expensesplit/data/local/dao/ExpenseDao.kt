package com.example.expensesplit.data.local.dao

import androidx.room.*
import com.example.expensesplit.data.local.entity.ExpenseEntity
import com.example.expensesplit.data.local.entity.ExpensePayerEntity
import com.example.expensesplit.data.local.entity.ExpenseSplitEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses WHERE groupId = :groupId AND deletedAt IS NULL ORDER BY date DESC")
    fun getExpensesForGroup(groupId: String): Flow<List<ExpenseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity)

    @Update
    suspend fun updateExpense(expense: ExpenseEntity)

    @Query("UPDATE expenses SET deletedAt = :timestamp, syncStatus = :syncStatus WHERE id = :expenseId")
    suspend fun softDeleteExpense(expenseId: String, timestamp: Long, syncStatus: String = "PENDING")

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpenseSplits(splits: List<ExpenseSplitEntity>)

    @Query("SELECT * FROM expense_splits WHERE expenseId = :expenseId")
    suspend fun getSplitsForExpense(expenseId: String): List<ExpenseSplitEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpensePayers(payers: List<ExpensePayerEntity>)

    @Query("SELECT * FROM expense_payers WHERE expenseId = :expenseId")
    suspend fun getPayersForExpense(expenseId: String): List<ExpensePayerEntity>
}
