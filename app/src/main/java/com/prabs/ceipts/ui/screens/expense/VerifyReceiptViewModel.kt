package com.prabs.ceipts.ui.screens.expense

import android.content.Context
import android.net.Uri
import android.util.Log
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
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class VerifyReceiptViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val expenseRepository: ExpenseRepository,
    private val groupRepository: GroupRepository,
    private val authRepository: AuthRepository,
    private val splitCalculator: SplitCalculator
) : ViewModel() {

    val groupId: String = checkNotNull(savedStateHandle["groupId"])
    val uriString: String? = savedStateHandle["uri"]

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _title = MutableStateFlow("")
    val title = _title.asStateFlow()

    private val _items = MutableStateFlow<List<OcrReceiptItem>>(emptyList())
    val items = _items.asStateFlow()

    fun parseReceipt(context: Context) {
        if (uriString.isNullOrBlank()) {
            populateFallbackItems()
            return
        }

        _isLoading.value = true
        viewModelScope.launch {
            try {
                val decodedUri = Uri.decode(uriString)
                val uri = Uri.parse(decodedUri)
                val parsed = OcrParser.parseReceipt(context, uri)
                if (parsed != null) {
                    _title.value = parsed.title
                    _items.value = parsed.items.map { OcrReceiptItem(it.name, it.price, isChecked = true) }
                } else {
                    populateFallbackItems()
                }
            } catch (e: Exception) {
                Log.e("VerifyReceiptViewModel", "Error running OCR", e)
                populateFallbackItems()
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun populateFallbackItems() {
        _title.value = "Sushi Restaurant"
        _items.value = listOf(
            OcrReceiptItem("Sushi Combo Deluxe", 28.50, isChecked = true),
            OcrReceiptItem("Green Tea (2x)", 6.00, isChecked = true),
            OcrReceiptItem("Gyoza Appetizer", 8.50, isChecked = true),
            OcrReceiptItem("Service Charge 10%", 4.30, isChecked = true)
        )
    }

    fun updateItem(index: Int, updatedItem: OcrReceiptItem) {
        val currentList = _items.value.toMutableList()
        if (index in currentList.indices) {
            currentList[index] = updatedItem
            _items.value = currentList
        }
    }

    fun deleteItem(index: Int) {
        val currentList = _items.value.toMutableList()
        if (index in currentList.indices) {
            currentList.removeAt(index)
            _items.value = currentList
        }
    }

    fun addItem() {
        val currentList = _items.value.toMutableList()
        currentList.add(OcrReceiptItem("", 0.0, isChecked = true))
        _items.value = currentList
    }

    fun saveExpenses(
        currencyCode: String,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                val userId = authRepository.getCurrentUserId() ?: "temp-user-id"
                val now = System.currentTimeMillis()
                val allMembers = groupRepository.getAllGroupMembers().first().filter { it.groupId == groupId }
                val memberIds = if (allMembers.isEmpty()) listOf(userId) else allMembers.map { it.userId }

                _items.value.filter { it.isChecked }.forEach { item ->
                    val expenseId = UUID.randomUUID().toString()
                    val expenseAmount = item.price

                    val baseVal = when (currencyCode.uppercase()) {
                        "USD" -> expenseAmount * 83.2
                        "EUR" -> expenseAmount * 90.4
                        "GBP" -> expenseAmount * 105.1
                        "JPY" -> expenseAmount * 0.53
                        "INR" -> expenseAmount
                        else -> expenseAmount
                    }

                    val prefix = _title.value.trim()
                    val expenseTitle = if (prefix.isNotEmpty() && prefix != "Receipt Expense" && prefix != "Grocery Bill") {
                        "$prefix - ${item.name}"
                    } else {
                        item.name
                    }

                    val expense = ExpenseEntity(
                        id = expenseId,
                        groupId = groupId,
                        title = expenseTitle,
                        amount = expenseAmount,
                        baseAmount = baseVal,
                        currencyCode = currencyCode,
                        date = now,
                        createdBy = userId,
                        createdAt = now,
                        updatedAt = now,
                        deletedAt = null
                    )

                    val payer = ExpensePayerEntity(expenseId, userId, expenseAmount)

                    val shareType = ShareType.EQUAL
                    val specs = memberIds.map { memberId ->
                        SplitSpec(userId = memberId, shareType = shareType, value = 0.0)
                    }

                    val splitResults = try {
                        splitCalculator.calculateSplits(expenseAmount, userId, specs)
                    } catch (e: Exception) {
                        splitCalculator.calculateSplits(expenseAmount, userId, memberIds.map { SplitSpec(it, ShareType.EQUAL) })
                    }

                    val splits = splitResults.map { result ->
                        ExpenseSplitEntity(
                            expenseId = expenseId,
                            userId = result.userId,
                            shareType = shareType.name,
                            value = 0.0,
                            computedAmount = result.computedAmount
                        )
                    }

                    expenseRepository.createExpense(expense, listOf(payer), splits)
                }
                onComplete()
            } catch (e: Exception) {
                Log.e("VerifyReceiptViewModel", "Error saving batch expenses", e)
            }
        }
    }
}
