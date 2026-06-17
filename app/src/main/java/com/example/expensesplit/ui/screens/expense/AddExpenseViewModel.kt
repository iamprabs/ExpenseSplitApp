package com.example.expensesplit.ui.screens.expense

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensesplit.data.local.entity.ExpenseEntity
import com.example.expensesplit.data.local.entity.ExpensePayerEntity
import com.example.expensesplit.data.local.entity.ExpenseSplitEntity
import com.example.expensesplit.domain.repository.AuthRepository
import com.example.expensesplit.domain.repository.ExpenseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class AddExpenseViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val expenseRepository: ExpenseRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val groupId: String = checkNotNull(savedStateHandle["groupId"])

    private val _title = MutableStateFlow("")
    val title = _title.asStateFlow()

    private val _amount = MutableStateFlow("")
    val amount = _amount.asStateFlow()

    // For simplicity in v1 MVP, we assume equal split and current user paid full amount.
    // In a full implementation, we would query group members and let the user allocate amounts.

    fun onTitleChange(newTitle: String) {
        _title.value = newTitle
    }

    fun onAmountChange(newAmount: String) {
        _amount.value = newAmount
    }

    fun saveExpense(onComplete: () -> Unit) {
        viewModelScope.launch {
            val expenseAmount = _amount.value.toDoubleOrNull() ?: 0.0
            if (expenseAmount <= 0) return@launch

            val userId = authRepository.getCurrentUserId() ?: "temp-user-id"
            val expenseId = UUID.randomUUID().toString()
            val now = System.currentTimeMillis()

            val expense = ExpenseEntity(
                id = expenseId,
                groupId = groupId,
                title = _title.value,
                amount = expenseAmount,
                currencyCode = "USD",
                date = now,
                createdBy = userId,
                createdAt = now,
                updatedAt = now,
                deletedAt = null
            )

            // Simplest logic: Current user paid it all, current user owes it all (Personal expense basically, or pending split adjustment)
            val payer = ExpensePayerEntity(expenseId, userId, expenseAmount)
            val split = ExpenseSplitEntity(expenseId, userId, expenseAmount)

            expenseRepository.createExpense(expense, listOf(payer), listOf(split))
            
            onComplete()
        }
    }
}
