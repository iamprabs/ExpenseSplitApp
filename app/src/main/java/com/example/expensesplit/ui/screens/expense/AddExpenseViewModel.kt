package com.example.expensesplit.ui.screens.expense

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensesplit.data.local.entity.ExpenseEntity
import com.example.expensesplit.data.local.entity.ExpensePayerEntity
import com.example.expensesplit.data.local.entity.ExpenseSplitEntity
import com.example.expensesplit.domain.model.ShareType
import com.example.expensesplit.domain.repository.AuthRepository
import com.example.expensesplit.domain.repository.ExpenseRepository
import com.example.expensesplit.domain.repository.GroupRepository
import com.example.expensesplit.domain.usecase.SplitCalculator
import com.example.expensesplit.domain.usecase.SplitSpec
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class AddExpenseViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val expenseRepository: ExpenseRepository,
    private val groupRepository: GroupRepository,
    private val authRepository: AuthRepository,
    private val splitCalculator: SplitCalculator,
    private val userDao: com.example.expensesplit.data.local.dao.UserDao
) : ViewModel() {

    val groupId: String = checkNotNull(savedStateHandle["groupId"])

    private val _title = MutableStateFlow("")
    val title = _title.asStateFlow()

    private val _amount = MutableStateFlow("")
    val amount = _amount.asStateFlow()

    // Dynamically retrieve group members with user profile details
    val groupMembers: StateFlow<List<com.example.expensesplit.data.local.entity.UserEntity>> = combine(
        groupRepository.getAllGroupMembers(),
        userDao.getAllUsers()
    ) { members, allUsers ->
        val memberUserIds = members.filter { it.groupId == groupId }.map { it.userId }
        allUsers.filter { it.id in memberUserIds }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun onTitleChange(newTitle: String) {
        _title.value = newTitle
    }

    fun onAmountChange(newAmount: String) {
        _amount.value = newAmount
    }

    fun saveExpense(
        splitTypeStr: String,
        currencyCode: String,
        customWeights: Map<String, Double>,
        isSaveDefault: Boolean,
        context: Context,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            val expenseAmount = _amount.value.toDoubleOrNull() ?: 0.0
            if (expenseAmount <= 0) return@launch

            val userId = authRepository.getCurrentUserId() ?: "temp-user-id"
            val expenseId = UUID.randomUUID().toString()
            val now = System.currentTimeMillis()

            // Exchange rates conversion to INR base standard
            val baseVal = when (currencyCode.uppercase()) {
                "USD" -> expenseAmount * 83.2
                "EUR" -> expenseAmount * 90.4
                "GBP" -> expenseAmount * 105.1
                "JPY" -> expenseAmount * 0.53
                "INR" -> expenseAmount
                else -> expenseAmount
            }

            val expense = ExpenseEntity(
                id = expenseId,
                groupId = groupId,
                title = _title.value,
                amount = expenseAmount,
                baseAmount = baseVal,
                currencyCode = currencyCode,
                date = now,
                createdBy = userId,
                createdAt = now,
                updatedAt = now,
                deletedAt = null
            )

            // Current user pays the entire expense
            val payer = ExpensePayerEntity(expenseId, userId, expenseAmount)

            // Fetch active member IDs
            val allMembers = groupRepository.getAllGroupMembers().first().filter { it.groupId == groupId }
            val memberIds = if (allMembers.isEmpty()) listOf(userId) else allMembers.map { it.userId }

            val shareType = when (splitTypeStr.lowercase()) {
                "equal" -> ShareType.EQUAL
                "custom" -> ShareType.EXACT
                "percent" -> ShareType.PERCENT
                "shares" -> ShareType.SHARES
                else -> ShareType.EQUAL
            }

            // Create splits specs mapped to custom weights input
            val specs = memberIds.map { memberId ->
                val weightValue = customWeights[memberId] ?: when (shareType) {
                    ShareType.EQUAL -> 0.0
                    ShareType.PERCENT -> (100.0 / memberIds.size)
                    ShareType.SHARES -> 1.0
                    ShareType.EXACT -> (expenseAmount / memberIds.size)
                }
                SplitSpec(
                    userId = memberId,
                    shareType = shareType,
                    value = weightValue
                )
            }

            val splitResults = try {
                splitCalculator.calculateSplits(expenseAmount, userId, specs)
            } catch (e: Exception) {
                splitCalculator.calculateSplits(expenseAmount, userId, memberIds.map { SplitSpec(it, ShareType.EQUAL) })
            }

            val splits = splitResults.map { result ->
                val specValue = specs.firstOrNull { it.userId == result.userId }?.value ?: 0.0
                ExpenseSplitEntity(
                    expenseId = expenseId,
                    userId = result.userId,
                    shareType = shareType.name,
                    value = specValue,
                    computedAmount = result.computedAmount
                )
            }

            expenseRepository.createExpense(expense, listOf(payer), splits)

            if (isSaveDefault) {
                DefaultSplitCache.saveDefaultSplits(context, groupId, splitTypeStr, customWeights)
            }

            onComplete()
        }
    }
}

object DefaultSplitCache {
    fun saveDefaultSplits(context: Context, groupId: String, splitType: String, weights: Map<String, Double>) {
        val sp = context.getSharedPreferences("default_splits_pref", Context.MODE_PRIVATE)
        val weightsStr = weights.map { "${it.key}:${it.value}" }.joinToString(",")
        sp.edit()
            .putString("split_type_$groupId", splitType)
            .putString("weights_$groupId", weightsStr)
            .apply()
    }

    fun getDefaultSplitType(context: Context, groupId: String): String? {
        val sp = context.getSharedPreferences("default_splits_pref", Context.MODE_PRIVATE)
        return sp.getString("split_type_$groupId", null)
    }

    fun getDefaultWeights(context: Context, groupId: String): Map<String, Double> {
        val sp = context.getSharedPreferences("default_splits_pref", Context.MODE_PRIVATE)
        val weightsStr = sp.getString("weights_$groupId", null) ?: return emptyMap()
        return try {
            weightsStr.split(",").associate {
                val parts = it.split(":")
                parts[0] to parts[1].toDouble()
            }
        } catch (e: Exception) {
            emptyMap()
        }
    }
}
