package com.example.expensesplit.data.local.dao

import androidx.room.*
import com.example.expensesplit.data.local.entity.GroupEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GroupDao {
    @Query("SELECT * FROM groups WHERE deletedAt IS NULL")
    fun getAllGroups(): Flow<List<GroupEntity>>

    @Query("SELECT * FROM groups WHERE id = :groupId")
    suspend fun getGroupById(groupId: String): GroupEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroup(group: GroupEntity)

    @Update
    suspend fun updateGroup(group: GroupEntity)

    @Query("UPDATE groups SET deletedAt = :timestamp, syncStatus = :syncStatus WHERE id = :groupId")
    suspend fun softDeleteGroup(groupId: String, timestamp: Long, syncStatus: String = "PENDING")

    @Query("SELECT * FROM group_members WHERE groupId = :groupId")
    fun getGroupMembers(groupId: String): Flow<List<com.example.expensesplit.data.local.entity.GroupMemberEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroupMember(member: com.example.expensesplit.data.local.entity.GroupMemberEntity)
}
