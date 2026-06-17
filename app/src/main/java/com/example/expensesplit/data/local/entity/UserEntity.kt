package com.example.expensesplit.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val id: String, // Maps to Supabase auth.users UUID
    val email: String?,
    val fullName: String?,
    val avatarUrl: String?,
    val createdAt: Long
)
