package com.prabs.ceipts.data.repository

import com.prabs.ceipts.data.local.dao.GroupDao
import com.prabs.ceipts.data.local.dao.SyncOutboxDao
import com.prabs.ceipts.data.local.entity.GroupEntity
import com.prabs.ceipts.data.local.entity.OperationType
import com.prabs.ceipts.data.local.entity.SyncOutboxEntity
import com.prabs.ceipts.data.local.entity.GroupMemberEntity
import com.prabs.ceipts.domain.repository.AuthRepository
import com.prabs.ceipts.domain.repository.GroupRepository
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GroupRepositoryImpl @Inject constructor(
    private val groupDao: GroupDao,
    private val syncOutboxDao: SyncOutboxDao,
    private val authRepository: AuthRepository,
    private val syncEngine: com.prabs.ceipts.sync.SyncEngine
) : GroupRepository {

    override fun getAllGroups(): Flow<List<GroupEntity>> = groupDao.getAllGroups()

    override suspend fun getGroupById(id: String): GroupEntity? = groupDao.getGroupById(id)

    override suspend fun createGroup(
        name: String,
        description: String?,
        coverUrl: String?,
        memberUserIds: List<String>
    ) {
        val groupId = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        val creatorId = authRepository.getCurrentUserId() ?: "temp-user-id"
        
        val group = GroupEntity(
            id = groupId,
            name = name,
            description = description,
            coverUrl = coverUrl,
            createdBy = creatorId,
            createdAt = now,
            updatedAt = now,
            deletedAt = null
        )
        
        groupDao.insertGroup(group)
        
        // Automatically insert the creator as a group member (owner)
        groupDao.insertGroupMember(
            GroupMemberEntity(
                groupId = groupId,
                userId = creatorId,
                role = "owner",
                joinedAt = now
            )
        )
        
        // Insert selected friends as members
        memberUserIds.forEach { memberId ->
            if (memberId != creatorId) {
                groupDao.insertGroupMember(
                    GroupMemberEntity(
                        groupId = groupId,
                        userId = memberId,
                        role = "member",
                        joinedAt = now
                    )
                )
            }
        }
        
        syncOutboxDao.insertSyncTask(
            SyncOutboxEntity(
                tableName = "groups",
                recordId = groupId,
                operation = OperationType.INSERT,
                timestamp = now
            )
        )
        
        // Trigger immediate background sync
        syncEngine.triggerImmediateSync()
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
        
        // Trigger immediate background sync
        syncEngine.triggerImmediateSync()
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
        
        // Trigger immediate background sync
        syncEngine.triggerImmediateSync()
    }

    override fun getAllGroupMembers(): Flow<List<com.prabs.ceipts.data.local.entity.GroupMemberEntity>> {
        return groupDao.getAllGroupMembers()
    }

    override suspend fun addMemberToGroup(groupId: String, userId: String) {
        val now = System.currentTimeMillis()
        val member = com.prabs.ceipts.data.local.entity.GroupMemberEntity(
            groupId = groupId,
            userId = userId,
            role = "member",
            joinedAt = now
        )
        groupDao.insertGroupMember(member)
        
        syncOutboxDao.insertSyncTask(
            SyncOutboxEntity(
                tableName = "group_members",
                recordId = "$groupId:$userId",
                operation = OperationType.INSERT,
                timestamp = now
            )
        )
        
        // Trigger immediate background sync
        syncEngine.triggerImmediateSync()
    }
}






