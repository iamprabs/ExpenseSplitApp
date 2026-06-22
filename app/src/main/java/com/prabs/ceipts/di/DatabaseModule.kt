package com.prabs.ceipts.di

import android.content.Context
import androidx.room.Room
import com.prabs.ceipts.data.local.ExpenseSplitDatabase
import com.prabs.ceipts.data.local.dao.ExpenseDao
import com.prabs.ceipts.data.local.dao.GroupDao
import com.prabs.ceipts.data.local.dao.SyncOutboxDao
import com.prabs.ceipts.data.local.dao.UserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): ExpenseSplitDatabase {
        // TODO: integrate net.zetetic:android-database-sqlcipher with SupportFactory
        return Room.databaseBuilder(
            context,
            ExpenseSplitDatabase::class.java,
            "expense_split_db"
        )
        .fallbackToDestructiveMigration() // only for v1
        .build()
    }

    @Provides
    fun provideUserDao(db: ExpenseSplitDatabase): UserDao = db.userDao()

    @Provides
    fun provideGroupDao(db: ExpenseSplitDatabase): GroupDao = db.groupDao()

    @Provides
    fun provideExpenseDao(db: ExpenseSplitDatabase): ExpenseDao = db.expenseDao()

    @Provides
    fun provideSyncOutboxDao(db: ExpenseSplitDatabase): SyncOutboxDao = db.syncOutboxDao()
}
