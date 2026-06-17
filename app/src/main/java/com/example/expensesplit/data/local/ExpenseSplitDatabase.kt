package com.example.expensesplit.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.expensesplit.data.local.dao.*
import com.example.expensesplit.data.local.entity.*

@Database(
    entities = [
        UserEntity::class,
        GroupEntity::class,
        GroupMemberEntity::class,
        ExpenseEntity::class,
        ExpensePayerEntity::class,
        ExpenseSplitEntity::class,
        SyncOutboxEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class ExpenseSplitDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun groupDao(): GroupDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun syncOutboxDao(): SyncOutboxDao
}
