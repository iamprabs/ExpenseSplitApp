package com.prabs.ceipts.domain.repository

interface AuthRepository {
    suspend fun getCurrentUserId(): String?
    suspend fun signIn(email: String, password: String): Boolean
    suspend fun signUp(email: String, password: String, fullName: String): Boolean
    suspend fun signOut()
}
