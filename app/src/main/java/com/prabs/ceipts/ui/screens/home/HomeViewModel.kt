package com.prabs.ceipts.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prabs.ceipts.data.local.entity.GroupEntity
import com.prabs.ceipts.domain.repository.AuthRepository
import com.prabs.ceipts.domain.repository.ExpenseRepository
import com.prabs.ceipts.domain.repository.GroupRepository
import com.prabs.ceipts.domain.usecase.BalanceCalculator
import com.prabs.ceipts.domain.usecase.GlobalBalanceResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GroupWithStats(
    val group: GroupEntity,
    val memberCount: Int,
    val expenseCount: Int
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val groupRepository: GroupRepository,
    private val expenseRepository: ExpenseRepository,
    private val authRepository: AuthRepository,
    private val balanceCalculator: BalanceCalculator,
    private val userDao: com.prabs.ceipts.data.local.dao.UserDao
) : ViewModel() {

    val allUsers: StateFlow<List<com.prabs.ceipts.data.local.entity.UserEntity>> = userDao.getAllUsers()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addFriend(email: String, name: String) {
        viewModelScope.launch {
            val friendUser = com.prabs.ceipts.data.local.entity.UserEntity(
                id = java.util.UUID.randomUUID().toString(),
                email = email,
                fullName = name,
                avatarUrl = null,
                createdAt = System.currentTimeMillis()
            )
            userDao.insertUser(friendUser)
        }
    }

    val groupsWithStats: StateFlow<List<GroupWithStats>> = combine(
        groupRepository.getAllGroups(),
        groupRepository.getAllGroupMembers(),
        expenseRepository.getAllExpenses()
    ) { groups, members, expenses ->
        groups.map { group ->
            val mCount = members.count { it.groupId == group.id }
            val eCount = expenses.count { it.groupId == group.id }
            GroupWithStats(group, mCount, eCount)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val globalBalance: StateFlow<GlobalBalanceResult?> = combine(
        expenseRepository.getAllExpenses(),
        expenseRepository.getAllExpenseSplits(),
        expenseRepository.getAllExpensePayers()
    ) { expenses, splits, payers ->
        val userId = authRepository.getCurrentUserId() ?: "temp-user-id"
        balanceCalculator.calculateGlobalBalance(userId, expenses, splits, payers)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val allExpenses: StateFlow<List<com.prabs.ceipts.data.local.entity.ExpenseEntity>> = expenseRepository.getAllExpenses()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allGroups: StateFlow<List<com.prabs.ceipts.data.local.entity.GroupEntity>> = groupRepository.getAllGroups()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun importCsvExpenses(
        groupId: String?,
        transactions: List<ParsedCsvTransaction>
    ) {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId() ?: "temp-user-id"
            val now = System.currentTimeMillis()
            
            transactions.forEach { tx ->
                val expenseId = java.util.UUID.randomUUID().toString()
                
                val expense = com.prabs.ceipts.data.local.entity.ExpenseEntity(
                    id = expenseId,
                    groupId = groupId ?: "",
                    title = tx.title,
                    amount = tx.amount,
                    baseAmount = tx.amount,
                    currencyCode = "USD",
                    category = tx.category,
                    date = now,
                    createdBy = userId,
                    createdAt = now,
                    updatedAt = now,
                    deletedAt = null
                )
                
                val payer = com.prabs.ceipts.data.local.entity.ExpensePayerEntity(
                    expenseId = expenseId,
                    userId = userId,
                    amount = tx.amount
                )
                
                val memberIds = if (groupId.isNullOrBlank()) {
                    listOf(userId)
                } else {
                    val members = groupRepository.getAllGroupMembers().first().filter { it.groupId == groupId }
                    if (members.isEmpty()) listOf(userId) else members.map { it.userId }
                }
                
                val splits = memberIds.map { memberId ->
                    val share = tx.amount / memberIds.size
                    com.prabs.ceipts.data.local.entity.ExpenseSplitEntity(
                        expenseId = expenseId,
                        userId = memberId,
                        shareType = "EQUAL",
                        value = 0.0,
                        computedAmount = share
                    )
                }
                
                expenseRepository.createExpense(expense, listOf(payer), splits)
            }
        }
    }

    var showCreateGroupDialog = androidx.compose.runtime.mutableStateOf(false)

    fun createGroup(name: String, description: String, memberUserIds: List<String> = emptyList()) {
        viewModelScope.launch {
            groupRepository.createGroup(
                name = name,
                description = description,
                coverUrl = null,
                memberUserIds = memberUserIds
            )
        }
    }

    val groups: StateFlow<List<com.prabs.ceipts.data.local.entity.GroupEntity>> = allGroups

    private val currentUserIdFlow = MutableStateFlow<String?>(null)

    init {
        viewModelScope.launch {
            currentUserIdFlow.value = authRepository.getCurrentUserId()
        }
    }

    val currentUser: StateFlow<com.prabs.ceipts.data.local.entity.UserEntity?> = combine(
        allUsers,
        currentUserIdFlow
    ) { users, userId ->
        val id = userId ?: "temp-user-id"
        users.firstOrNull { it.id == id } ?: com.prabs.ceipts.data.local.entity.UserEntity(
            id = id,
            email = "alex.mercer@ceipts.app",
            fullName = "Alex Mercer",
            avatarUrl = null,
            createdAt = System.currentTimeMillis()
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    fun updateProfile(fullName: String, email: String, avatarUrl: String?) {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId() ?: "temp-user-id"
            val existingUser = userDao.getUserById(userId)
            val updatedUser = com.prabs.ceipts.data.local.entity.UserEntity(
                id = userId,
                email = email,
                fullName = fullName,
                avatarUrl = avatarUrl ?: existingUser?.avatarUrl,
                createdAt = existingUser?.createdAt ?: System.currentTimeMillis()
            )
            userDao.insertUser(updatedUser)
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.signOut()
        }
    }

}

data class ParsedCsvTransaction(
    val title: String,
    val amount: Double,
    val category: String,
    val dateStr: String
)
