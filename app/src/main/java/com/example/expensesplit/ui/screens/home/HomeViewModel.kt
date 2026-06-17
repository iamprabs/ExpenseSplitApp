package com.example.expensesplit.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensesplit.data.local.entity.GroupEntity
import com.example.expensesplit.domain.repository.GroupRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val groupRepository: GroupRepository
) : ViewModel() {

    val groups: StateFlow<List<GroupEntity>> = groupRepository.getAllGroups()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    var showCreateGroupDialog = androidx.compose.runtime.mutableStateOf(false)

    fun createGroup(name: String, description: String) {
        viewModelScope.launch {
            groupRepository.createGroup(
                name = name,
                description = description,
                coverUrl = null
            )
        }
    }
}
