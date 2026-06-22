package com.prabs.ceipts.sync

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.prabs.ceipts.data.local.dao.SyncOutboxDao
import com.prabs.ceipts.data.local.dao.GroupDao
import com.prabs.ceipts.data.local.dao.ExpenseDao
import com.prabs.ceipts.data.local.entity.OperationType
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val syncOutboxDao: SyncOutboxDao,
    private val groupDao: GroupDao,
    private val expenseDao: ExpenseDao,
    private val supabaseClient: SupabaseClient
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val pendingTasks = syncOutboxDao.getPendingSyncTasks()
            
            if (pendingTasks.isEmpty()) {
                return@withContext Result.success()
            }

            // Batch processing of pending sync tasks
            for (task in pendingTasks) {
                var success = false
                try {
                    when (task.tableName.lowercase()) {
                        "groups" -> {
                            val group = groupDao.getGroupById(task.recordId)
                            if (group != null && task.operation != OperationType.DELETE) {
                                val data = mapOf(
                                    "id" to group.id,
                                    "name" to group.name,
                                    "description" to group.description,
                                    "coverUrl" to group.coverUrl,
                                    "createdBy" to group.createdBy,
                                    "createdAt" to group.createdAt,
                                    "updatedAt" to group.updatedAt,
                                    "deletedAt" to group.deletedAt
                                )
                                supabaseClient.postgrest["groups"].upsert(data)
                            } else if (task.operation == OperationType.DELETE) {
                                supabaseClient.postgrest["groups"].delete {
                                    filter {
                                        eq("id", task.recordId)
                                    }
                                }
                            }
                            success = true
                        }
                        "group_members" -> {
                            val parts = task.recordId.split(":")
                            if (parts.size == 2) {
                                val gId = parts[0]
                                val uId = parts[1]
                                val member = groupDao.getAllGroupMembers().first().firstOrNull { it.groupId == gId && it.userId == uId }
                                if (member != null && task.operation != OperationType.DELETE) {
                                    val data = mapOf(
                                        "groupId" to member.groupId,
                                        "userId" to member.userId,
                                        "role" to member.role,
                                        "joinedAt" to member.joinedAt
                                    )
                                    supabaseClient.postgrest["group_members"].upsert(data)
                                } else if (task.operation == OperationType.DELETE) {
                                    supabaseClient.postgrest["group_members"].delete {
                                        filter {
                                            eq("groupId", gId)
                                            eq("userId", uId)
                                        }
                                    }
                                }
                            }
                            success = true
                        }
                        "expenses" -> {
                            val expense = expenseDao.getExpenseById(task.recordId)
                            if (expense != null && task.operation != OperationType.DELETE) {
                                val data = mapOf(
                                    "id" to expense.id,
                                    "groupId" to expense.groupId,
                                    "title" to expense.title,
                                    "amount" to expense.amount,
                                    "baseAmount" to expense.baseAmount,
                                    "currencyCode" to expense.currencyCode,
                                    "category" to expense.category,
                                    "notes" to expense.notes,
                                    "receiptUri" to expense.receiptUri,
                                    "date" to expense.date,
                                    "isRecurring" to expense.isRecurring,
                                    "recurrenceRule" to expense.recurrenceRule,
                                    "createdBy" to expense.createdBy,
                                    "createdAt" to expense.createdAt,
                                    "updatedAt" to expense.updatedAt,
                                    "deletedAt" to expense.deletedAt
                                )
                                supabaseClient.postgrest["expenses"].upsert(data)
                                
                                // Sync payers associated with this expense
                                val payers = expenseDao.getAllExpensePayers().first().filter { it.expenseId == expense.id }
                                payers.forEach { payer ->
                                    val payerData = mapOf(
                                        "expenseId" to payer.expenseId,
                                        "userId" to payer.userId,
                                        "amount" to payer.amount
                                    )
                                    supabaseClient.postgrest["expense_payers"].upsert(payerData)
                                }
                                
                                // Sync splits associated with this expense
                                val splits = expenseDao.getAllExpenseSplits().first().filter { it.expenseId == expense.id }
                                splits.forEach { split ->
                                    val splitData = mapOf(
                                        "expenseId" to split.expenseId,
                                        "userId" to split.userId,
                                        "shareType" to split.shareType,
                                        "value" to split.value,
                                        "computedAmount" to split.computedAmount
                                    )
                                    supabaseClient.postgrest["expense_splits"].upsert(splitData)
                                }
                            } else if (task.operation == OperationType.DELETE) {
                                try {
                                    supabaseClient.postgrest["expense_payers"].delete { filter { eq("expenseId", task.recordId) } }
                                    supabaseClient.postgrest["expense_splits"].delete { filter { eq("expenseId", task.recordId) } }
                                } catch (e: Exception) {
                                    Log.e("SyncWorker", "Error deleting associated payers/splits", e)
                                }
                                supabaseClient.postgrest["expenses"].delete {
                                    filter {
                                        eq("id", task.recordId)
                                    }
                                }
                            }
                            success = true
                        }
                    }
                } catch (e: Exception) {
                    Log.e("SyncWorker", "Failed to sync task: ${task.id}", e)
                }

                if (success) {
                    syncOutboxDao.deleteSyncTask(task)
                }
            }

            Result.success()
        } catch (e: Exception) {
            Log.e("SyncWorker", "Exception during sync doWork", e)
            Result.retry()
        }
    }
}
