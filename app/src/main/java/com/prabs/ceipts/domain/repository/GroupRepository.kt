package com.prabs.ceipts.domain.repository

import com.prabs.ceipts.data.local.entity.GroupEntity
import kotlinx.coroutines.flow.Flow

interface GroupRepository {
    fun getAllGroups(): Flow<List<GroupEntity>>
    suspend fun getGroupById(id: String): GroupEntity?
    suspend fun createGroup(name: String, description: String?, coverUrl: String?, memberUserIds: List<String> = emptyList())
    suspend fun updateGroup(group: GroupEntity)
    suspend fun deleteGroup(id: String)
    fun getAllGroupMembers(): Flow<List<com.prabs.ceipts.data.local.entity.GroupMemberEntity>>
    suspend fun addMemberToGroup(groupId: String, userId: String)
}
