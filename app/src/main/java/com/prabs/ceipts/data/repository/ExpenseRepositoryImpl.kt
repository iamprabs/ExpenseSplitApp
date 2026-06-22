package com.prabs.ceipts.data.repository

import androidx.room.withTransaction
import com.prabs.ceipts.data.local.ExpenseSplitDatabase
import com.prabs.ceipts.data.local.dao.ExpenseDao
import com.prabs.ceipts.data.local.dao.SyncOutboxDao
import com.prabs.ceipts.data.local.entity.ExpenseEntity
import com.prabs.ceipts.data.local.entity.ExpensePayerEntity
import com.prabs.ceipts.data.local.entity.ExpenseSplitEntity
import com.prabs.ceipts.data.local.entity.OperationType
import com.prabs.ceipts.data.local.entity.SyncOutboxEntity
import com.prabs.ceipts.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExpenseRepositoryImpl @Inject constructor(
    private val database: ExpenseSplitDatabase,
    private val expenseDao: ExpenseDao,
    private val syncOutboxDao: SyncOutboxDao,
    private val syncEngine: com.prabs.ceipts.sync.SyncEngine
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
        
        // Trigger immediate background sync
        syncEngine.triggerImmediateSync()
    }

    override suspend fun updateExpense(
        expense: ExpenseEntity,
        payers: List<ExpensePayerEntity>,
        splits: List<ExpenseSplitEntity>
    ) {
        val now = System.currentTimeMillis()
        
        database.withTransaction {
            expenseDao.insertExpense(expense)
            expenseDao.deletePayersForExpense(expense.id)
            expenseDao.deleteSplitsForExpense(expense.id)
            expenseDao.insertExpensePayers(payers)
            expenseDao.insertExpenseSplits(splits)
            
            // Queue sync for the updated expense
            syncOutboxDao.insertSyncTask(
                SyncOutboxEntity(
                    tableName = "expenses",
                    recordId = expense.id,
                    operation = OperationType.UPDATE,
                    timestamp = now
                )
            )
        }
        
        // Trigger immediate background sync
        syncEngine.triggerImmediateSync()
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
        
        // Trigger immediate background sync
        syncEngine.triggerImmediateSync()
    }

    override suspend fun getExpenseById(id: String): ExpenseEntity? {
        return expenseDao.getExpenseById(id)
    }

    override suspend fun getSplitsForExpense(expenseId: String): List<ExpenseSplitEntity> {
        return expenseDao.getSplitsForExpense(expenseId)
    }

    override fun getAllExpenses(): Flow<List<ExpenseEntity>> {
        return expenseDao.getAllExpenses()
    }

    override fun getAllExpenseSplits(): Flow<List<ExpenseSplitEntity>> {
        return expenseDao.getAllExpenseSplits()
    }

    override fun getAllExpensePayers(): Flow<List<ExpensePayerEntity>> {
        return expenseDao.getAllExpensePayers()
    }
}
