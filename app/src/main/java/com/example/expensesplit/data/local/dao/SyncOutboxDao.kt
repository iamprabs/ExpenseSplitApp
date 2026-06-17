package com.example.expensesplit.data.local.dao

import androidx.room.*
import com.example.expensesplit.data.local.entity.SyncOutboxEntity

@Dao
interface SyncOutboxDao {
    @Query("SELECT * FROM sync_outbox ORDER BY timestamp ASC")
    suspend fun getPendingSyncTasks(): List<SyncOutboxEntity>

    @Insert
    suspend fun insertSyncTask(task: SyncOutboxEntity)

    @Delete
    suspend fun deleteSyncTask(task: SyncOutboxEntity)
}
