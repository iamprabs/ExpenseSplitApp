package com.example.expensesplit.data.repository

import androidx.room.withTransaction
import com.example.expensesplit.data.local.ExpenseSplitDatabase
import com.example.expensesplit.data.local.dao.ExpenseDao
import com.example.expensesplit.data.local.dao.SyncOutboxDao
import com.example.expensesplit.data.local.entity.ExpenseEntity
import com.example.expensesplit.data.local.entity.ExpensePayerEntity
import com.example.expensesplit.data.local.entity.ExpenseSplitEntity
import com.example.expensesplit.data.local.entity.OperationType
import com.example.expensesplit.data.local.entity.SyncOutboxEntity
import com.example.expensesplit.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExpenseRepositoryImpl @Inject constructor(
    private val database: ExpenseSplitDatabase,
    private val expenseDao: ExpenseDao,
    private val syncOutboxDao: SyncOutboxDao
) : ExpenseRepository {

    override fun getExpensesForGroup(groupId: String): Flow<List<ExpenseEntity>> {
        return expenseDao.getExpensesForGroup(groupId)
    }

    override suspend fun createExpense(
        expense: ExpenseEntity,
        payers: List<ExpensePayerEntity>,
        splits: List<ExpenseSplitEntity>
    ) {
        val now = System.currentTimeMillis()
        
        database.withTransaction {
            expenseDao.insertExpense(expense)
            expenseDao.insertExpensePayers(payers)
            expenseDao.insertExpenseSplits(splits)
            
            // Queue sync for the new expense
            syncOutboxDao.insertSyncTask(
                SyncOutboxEntity(
                    tableName = "expenses",
                    recordId = expense.id,
                    operation = OperationType.INSERT,
                    timestamp = now
                )
            )
        }
    }

    override suspend fun deleteExpense(expenseId: String) {
        val now = System.currentTimeMillis()
        database.withTransaction {
            expenseDao.softDeleteExpense(expenseId, now)
            
            syncOutboxDao.insertSyncTask(
                SyncOutboxEntity(
                    tableName = "expenses",
                    recordId = expenseId,
                    operation = OperationType.DELETE,
                    timestamp = now
                )
            )
        }
    }
}
