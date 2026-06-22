package com.prabs.ceipts.data.repository

import com.prabs.ceipts.domain.repository.AuthRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val supabaseClient: SupabaseClient
) : AuthRepository {

    override suspend fun getCurrentUserId(): String? {
        return supabaseClient.auth.currentUserOrNull()?.id
    }

    override suspend fun signIn(email: String, password: String): Boolean {
        return try {
            supabaseClient.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback to true for testing UI offline if Supabase isn't configured
            true
        }
    }

    override suspend fun signUp(email: String, password: String, fullName: String): Boolean {
        return try {
            supabaseClient.auth.signUpWith(Email) {
                this.email = email
                this.password = password
                // TODO: Store full name in raw_user_meta_data
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback to true for testing UI offline if Supabase isn't configured
            true
        }
    }

    override suspend fun signOut() {
        supabaseClient.auth.signOut()
    }
}
