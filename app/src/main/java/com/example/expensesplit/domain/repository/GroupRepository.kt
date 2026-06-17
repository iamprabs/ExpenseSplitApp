package com.example.expensesplit.domain.repository

import com.example.expensesplit.data.local.entity.GroupEntity
import kotlinx.coroutines.flow.Flow

interface GroupRepository {
    fun getAllGroups(): Flow<List<GroupEntity>>
    suspend fun getGroupById(id: String): GroupEntity?
    suspend fun createGroup(name: String, description: String?, coverUrl: String?)
    suspend fun updateGroup(group: GroupEntity)
    suspend fun deleteGroup(id: String)
}
