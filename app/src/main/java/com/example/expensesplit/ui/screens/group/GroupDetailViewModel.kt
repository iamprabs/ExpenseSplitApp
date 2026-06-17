package com.example.expensesplit.ui.screens.group

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensesplit.data.local.entity.ExpenseEntity
import com.example.expensesplit.data.local.entity.GroupEntity
import com.example.expensesplit.domain.repository.ExpenseRepository
import com.example.expensesplit.domain.repository.GroupRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class GroupDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    groupRepository: GroupRepository,
    expenseRepository: ExpenseRepository
) : ViewModel() {

    val groupId: String = checkNotNull(savedStateHandle["groupId"])

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

    val members: StateFlow<List<com.example.expensesplit.data.local.entity.GroupMemberEntity>> = kotlinx.coroutines.flow.flow {
        groupRepository.getAllGroupMembers().collect { allMembers ->
            emit(allMembers.filter { it.groupId == groupId })
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
}
