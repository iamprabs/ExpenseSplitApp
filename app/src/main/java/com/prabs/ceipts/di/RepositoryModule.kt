package com.prabs.ceipts.di

import com.prabs.ceipts.data.repository.AuthRepositoryImpl
import com.prabs.ceipts.data.repository.ExpenseRepositoryImpl
import com.prabs.ceipts.data.repository.GroupRepositoryImpl
import com.prabs.ceipts.domain.repository.AuthRepository
import com.prabs.ceipts.domain.repository.ExpenseRepository
import com.prabs.ceipts.domain.repository.GroupRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindGroupRepository(
        groupRepositoryImpl: GroupRepositoryImpl
    ): GroupRepository

    @Binds
    @Singleton
    abstract fun bindExpenseRepository(
        expenseRepositoryImpl: ExpenseRepositoryImpl
    ): ExpenseRepository
}
