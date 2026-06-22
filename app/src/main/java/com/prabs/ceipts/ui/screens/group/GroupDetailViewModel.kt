package com.prabs.ceipts.ui.screens.group

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prabs.ceipts.data.local.entity.ExpenseEntity
import com.prabs.ceipts.data.local.entity.GroupEntity
import com.prabs.ceipts.data.local.entity.UserEntity
import com.prabs.ceipts.data.local.entity.ExpensePayerEntity
import com.prabs.ceipts.data.local.entity.ExpenseSplitEntity
import com.prabs.ceipts.domain.repository.AuthRepository
import com.prabs.ceipts.domain.repository.ExpenseRepository
import com.prabs.ceipts.domain.repository.GroupRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GroupMemberWithDetails(
    val member: com.prabs.ceipts.data.local.entity.GroupMemberEntity,
    val user: UserEntity?
)

data class ExpenseWithPayerDetails(
    val expense: ExpenseEntity,
    val payerName: String
)

@HiltViewModel
class GroupDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val groupRepository: GroupRepository,
    private val expenseRepository: ExpenseRepository,
    private val authRepository: AuthRepository,
    private val userDao: com.prabs.ceipts.data.local.dao.UserDao
) : ViewModel() {

    val groupId: String = checkNotNull(savedStateHandle["groupId"])

    val currentUserId = androidx.compose.runtime.mutableStateOf("temp-user-id")

    init {
        viewModelScope.launch {
            currentUserId.value = authRepository.getCurrentUserId() ?: "temp-user-id"
        }
    }

    val group: StateFlow<GroupEntity?> = kotlinx.coroutines.flow.flow {
        emit(groupRepository.getGroupById(groupId))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val expenses: StateFlow<List<ExpenseEntity>> = expenseRepository.getExpensesForGroup(groupId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val expensesWithPayer: StateFlow<List<ExpenseWithPayerDetails>> = combine(
        expenseRepository.getExpensesForGroup(groupId),
        expenseRepository.getAllExpensePayers(),
        userDao.getAllUsers()
    ) { groupExpenses, allPayers, allUsers ->
        groupExpenses.map { expense ->
            val expensePayers = allPayers.filter { it.expenseId == expense.id }
            val payerId = expensePayers.firstOrNull()?.userId ?: expense.createdBy
            val payerUser = allUsers.firstOrNull { it.id == payerId }
            val payerName = if (payerId == currentUserId.value) "You" else (payerUser?.fullName ?: "Someone")
            ExpenseWithPayerDetails(expense, payerName)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allUsers: StateFlow<List<UserEntity>> = userDao.getAllUsers()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val members: StateFlow<List<GroupMemberWithDetails>> = combine(
        groupRepository.getAllGroupMembers(),
        userDao.getAllUsers()
    ) { allMembers, allUsers ->
        allMembers.filter { it.groupId == groupId }.map { member ->
            val user = allUsers.firstOrNull { it.id == member.userId }
            GroupMemberWithDetails(member, user)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    
    val groupExpensePayers: StateFlow<List<ExpensePayerEntity>> = combine(
        expenseRepository.getExpensesForGroup(groupId),
        expenseRepository.getAllExpensePayers()
    ) { groupExpenses, allPayers ->
        val groupExpenseIds = groupExpenses.map { it.id }.toSet()
        allPayers.filter { it.expenseId in groupExpenseIds }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val groupExpenseSplits: StateFlow<List<ExpenseSplitEntity>> = combine(
        expenseRepository.getExpensesForGroup(groupId),
        expenseRepository.getAllExpenseSplits()
    ) { groupExpenses, allSplits ->
        val groupExpenseIds = groupExpenses.map { it.id }.toSet()
        allSplits.filter { it.expenseId in groupExpenseIds }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun addMemberToGroup(userId: String) {
        viewModelScope.launch {
            groupRepository.addMemberToGroup(groupId, userId)
        }
    }

    fun addFriendAndMember(friendId: String, email: String, name: String) {
        viewModelScope.launch {
            val friendUser = com.prabs.ceipts.data.local.entity.UserEntity(
                id = friendId,
                email = email,
                fullName = name,
                avatarUrl = null,
                createdAt = System.currentTimeMillis()
            )
            userDao.insertUser(friendUser)
            groupRepository.addMemberToGroup(groupId, friendId)
        }
    }

    fun deleteGroup(onComplete: () -> Unit) {
        viewModelScope.launch {
            groupRepository.deleteGroup(groupId)
            onComplete()
        }
    }
}
