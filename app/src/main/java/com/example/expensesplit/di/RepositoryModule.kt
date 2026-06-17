package com.example.expensesplit.di

import com.example.expensesplit.data.repository.AuthRepositoryImpl
import com.example.expensesplit.data.repository.ExpenseRepositoryImpl
import com.example.expensesplit.data.repository.GroupRepositoryImpl
import com.example.expensesplit.domain.repository.AuthRepository
import com.example.expensesplit.domain.repository.ExpenseRepository
import com.example.expensesplit.domain.repository.GroupRepository
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
