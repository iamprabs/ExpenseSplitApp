package com.prabs.ceipts.ui.screens.expense

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prabs.ceipts.data.local.entity.ExpenseEntity
import com.prabs.ceipts.data.local.entity.ExpensePayerEntity
import com.prabs.ceipts.data.local.entity.ExpenseSplitEntity
import com.prabs.ceipts.domain.model.ShareType
import com.prabs.ceipts.domain.repository.AuthRepository
import com.prabs.ceipts.domain.repository.ExpenseRepository
import com.prabs.ceipts.domain.repository.GroupRepository
import com.prabs.ceipts.domain.usecase.SplitCalculator
import com.prabs.ceipts.domain.usecase.SplitSpec
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
    private val userDao: com.prabs.ceipts.data.local.dao.UserDao
) : ViewModel() {

    val groupId: String = checkNotNull(savedStateHandle["groupId"])
    val expenseId: String? = savedStateHandle["expenseId"]

    private val _existingExpense = MutableStateFlow<ExpenseEntity?>(null)
    val existingExpense = _existingExpense.asStateFlow()

    private val _existingSplits = MutableStateFlow<List<ExpenseSplitEntity>>(emptyList())
    val existingSplits = _existingSplits.asStateFlow()

    private val _currentUserId = MutableStateFlow("temp-user-id")
    val currentUserId = _currentUserId.asStateFlow()

    private val _existingPayerUserId = MutableStateFlow<String?>(null)
    val existingPayerUserId = _existingPayerUserId.asStateFlow()

    init {
        viewModelScope.launch {
            _currentUserId.value = authRepository.getCurrentUserId() ?: "temp-user-id"
        }
        expenseId?.let { id ->
            viewModelScope.launch {
                val expense = expenseRepository.getExpenseById(id)
                if (expense != null) {
                    _existingExpense.value = expense
                    _title.value = expense.title
                    _amount.value = String.format(java.util.Locale.ROOT, "%.2f", expense.amount)
                }
                
                val splits = expenseRepository.getSplitsForExpense(id)
                _existingSplits.value = splits

                val payers = expenseRepository.getAllExpensePayers().first().filter { it.expenseId == id }
                if (payers.isNotEmpty()) {
                    _existingPayerUserId.value = payers.first().userId
                }
            }
        }
    }

    private val _title = MutableStateFlow("")
    val title = _title.asStateFlow()

    private val _amount = MutableStateFlow("")
    val amount = _amount.asStateFlow()

    // Dynamically retrieve group members with user profile details (synthesized if missing from DB)
    val groupMembers: StateFlow<List<com.prabs.ceipts.data.local.entity.UserEntity>> = combine(
        groupRepository.getAllGroupMembers(),
        userDao.getAllUsers(),
        currentUserId
    ) { members, allUsers, currentUserIdVal ->
        val groupSpecificMembers = members.filter { it.groupId == groupId }
        groupSpecificMembers.map { member ->
            val id = member.userId
            val user = allUsers.firstOrNull { it.id == id }
            if (user != null) {
                user
            } else if (id == currentUserIdVal) {
                com.prabs.ceipts.data.local.entity.UserEntity(
                    id = id,
                    email = "alex.mercer@ceipts.app",
                    fullName = if (member.role == "owner") "You (Owner)" else "You",
                    avatarUrl = null,
                    createdAt = System.currentTimeMillis()
                )
            } else {
                com.prabs.ceipts.data.local.entity.UserEntity(
                    id = id,
                    email = "",
                    fullName = if (member.role == "owner") "Owner" else "Member ${id.take(5)}",
                    avatarUrl = null,
                    createdAt = System.currentTimeMillis()
                )
            }
        }
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
        payerUserId: String,
        customWeights: Map<String, Double>,
        isSaveDefault: Boolean,
        context: Context,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            val expenseAmount = _amount.value.toDoubleOrNull() ?: 0.0
            if (expenseAmount <= 0) return@launch

            val userId = authRepository.getCurrentUserId() ?: "temp-user-id"
            val finalExpenseId = expenseId ?: UUID.randomUUID().toString()
            val existing = _existingExpense.value
            val now = System.currentTimeMillis()
            val creatorId = existing?.createdBy ?: userId
            val creationTime = existing?.createdAt ?: now

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
                id = finalExpenseId,
                groupId = groupId,
                title = _title.value,
                amount = expenseAmount,
                baseAmount = baseVal,
                currencyCode = currencyCode,
                date = existing?.date ?: now,
                createdBy = creatorId,
                createdAt = creationTime,
                updatedAt = now,
                deletedAt = null
            )

            // Current user pays the entire expense
            val payer = ExpensePayerEntity(finalExpenseId, payerUserId, expenseAmount)

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

            // For equal splits, filter the participants list using the checked list from customWeights
            val participatingMemberIds = if (shareType == ShareType.EQUAL) {
                val selected = memberIds.filter { customWeights[it] == 1.0 }
                if (selected.isNotEmpty()) selected else memberIds
            } else {
                memberIds
            }

            // Create splits specs mapped to custom weights input
            val specs = participatingMemberIds.map { memberId ->
                val weightValue = customWeights[memberId] ?: when (shareType) {
                    ShareType.EQUAL -> 1.0
                    ShareType.PERCENT -> (100.0 / participatingMemberIds.size)
                    ShareType.SHARES -> 1.0
                    ShareType.EXACT -> (expenseAmount / participatingMemberIds.size)
                }
                SplitSpec(
                    userId = memberId,
                    shareType = shareType,
                    value = weightValue
                )
            }

            val splitResults = try {
                splitCalculator.calculateSplits(expenseAmount, payerUserId, specs)
            } catch (e: Exception) {
                splitCalculator.calculateSplits(expenseAmount, payerUserId, participatingMemberIds.map { SplitSpec(it, ShareType.EQUAL) })
            }

            val splits = splitResults.map { result ->
                val specValue = specs.firstOrNull { it.userId == result.userId }?.value ?: 0.0
                ExpenseSplitEntity(
                    expenseId = finalExpenseId,
                    userId = result.userId,
                    shareType = shareType.name,
                    value = specValue,
                    computedAmount = result.computedAmount
                )
            }

            if (expenseId != null) {
                expenseRepository.updateExpense(expense, listOf(payer), splits)
            } else {
                expenseRepository.createExpense(expense, listOf(payer), splits)
            }

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
