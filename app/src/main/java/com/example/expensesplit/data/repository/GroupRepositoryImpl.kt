package com.example.expensesplit.data.repository

import com.example.expensesplit.data.local.dao.GroupDao
import com.example.expensesplit.data.local.dao.SyncOutboxDao
import com.example.expensesplit.data.local.entity.GroupEntity
import com.example.expensesplit.data.local.entity.OperationType
import com.example.expensesplit.data.local.entity.SyncOutboxEntity
import com.example.expensesplit.domain.repository.GroupRepository
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GroupRepositoryImpl @Inject constructor(
    private val groupDao: GroupDao,
    private val syncOutboxDao: SyncOutboxDao
    // TODO: Inject AuthRepository to get createdBy UUID
) : GroupRepository {

    override fun getAllGroups(): Flow<List<GroupEntity>> = groupDao.getAllGroups()

    override suspend fun getGroupById(id: String): GroupEntity? = groupDao.getGroupById(id)

    override suspend fun createGroup(name: String, description: String?, coverUrl: String?) {
        val groupId = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        val group = GroupEntity(
            id = groupId,
            name = name,
            description = description,
            coverUrl = coverUrl,
            createdBy = "temp-user-id", // To be replaced by actual Auth session
            createdAt = now,
            updatedAt = now,
            deletedAt = null
        )
        
        groupDao.insertGroup(group)
        
        syncOutboxDao.insertSyncTask(
            SyncOutboxEntity(
                tableName = "groups",
                recordId = groupId,
                operation = OperationType.INSERT,
                timestamp = now
            )
        )
    }

    override suspend fun updateGroup(group: GroupEntity) {
        val now = System.currentTimeMillis()
        val updatedGroup = group.copy(updatedAt = now)
        groupDao.updateGroup(updatedGroup)
        
        syncOutboxDao.insertSyncTask(
            SyncOutboxEntity(
                tableName = "groups",
                recordId = group.id,
                operation = OperationType.UPDATE,
                timestamp = now
            )
        )
    }

    override suspend fun deleteGroup(id: String) {
        val now = System.currentTimeMillis()
        groupDao.softDeleteGroup(id, timestamp = now)
        
        syncOutboxDao.insertSyncTask(
            SyncOutboxEntity(
                tableName = "groups",
                recordId = id,
                operation = OperationType.DELETE,
                timestamp = now
            )
        )
    }
}
