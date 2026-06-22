package com.prabs.ceipts.ui.screens.expense

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prabs.ceipts.data.local.entity.ExpenseEntity
import com.prabs.ceipts.data.local.entity.ExpensePayerEntity
import com.prabs.ceipts.data.local.entity.ExpenseSplitEntity
import com.prabs.ceipts.domain.repository.AuthRepository
import com.prabs.ceipts.domain.repository.ExpenseRepository
import com.prabs.ceipts.domain.repository.GroupRepository
import com.prabs.ceipts.domain.usecase.DebtSimplifier
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DebtDisplayItem(
    val debtorId: String,
    val debtorName: String,
    val creditorId: String,
    val creditorName: String,
    val amount: Double
)

@HiltViewModel
class DebtSimplificationViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val expenseRepository: ExpenseRepository,
    private val groupRepository: GroupRepository,
    private val authRepository: AuthRepository,
    private val debtSimplifier: DebtSimplifier,
    private val userDao: com.prabs.ceipts.data.local.dao.UserDao
) : ViewModel() {

    val groupId: String = checkNotNull(savedStateHandle["groupId"])

    private val currentUserIdFlow = kotlinx.coroutines.flow.MutableStateFlow("temp-user-id")

    init {
        viewModelScope.launch {
            currentUserIdFlow.value = authRepository.getCurrentUserId() ?: "temp-user-id"
        }
    }

    /**
     * Computes simplified debts for the group by:
     * 1. Finding all expenses in this group
     * 2. Computing each member's net balance (total paid - total owed)
     * 3. Running the greedy debt simplification algorithm
     * 4. Mapping user IDs to display names
     */
    val simplifiedDebts: StateFlow<List<DebtDisplayItem>> = combine(
        expenseRepository.getExpensesForGroup(groupId),
        expenseRepository.getAllExpensePayers(),
        expenseRepository.getAllExpenseSplits(),
        userDao.getAllUsers(),
        currentUserIdFlow
    ) { expenses, allPayers, allSplits, allUsers, currentUserId ->
        val expenseIds = expenses.map { it.id }.toSet()
        val groupPayers = allPayers.filter { it.expenseId in expenseIds }
        val groupSplits = allSplits.filter { it.expenseId in expenseIds }

        // Calculate net balance for each member: paid - owed
        // Positive = they overpaid (are owed money)
        // Negative = they underpaid (owe money)
        val netBalances = mutableMapOf<String, Double>()

        groupPayers.forEach { payer ->
            netBalances[payer.userId] = (netBalances[payer.userId] ?: 0.0) + payer.amount
        }
        groupSplits.forEach { split ->
            netBalances[split.userId] = (netBalances[split.userId] ?: 0.0) - split.computedAmount
        }

        // Run debt simplification
        val debts = debtSimplifier.simplifyDebts(netBalances)

        // Map to display items with names
        debts.map { debt ->
            val debtorUser = allUsers.firstOrNull { it.id == debt.debtorId }
            val creditorUser = allUsers.firstOrNull { it.id == debt.creditorId }

            val debtorName = if (debt.debtorId == currentUserId) "You"
                else debtorUser?.fullName ?: "User ${debt.debtorId.take(5)}"
            val creditorName = if (debt.creditorId == currentUserId) "You"
                else creditorUser?.fullName ?: "User ${debt.creditorId.take(5)}"

            DebtDisplayItem(
                debtorId = debt.debtorId,
                debtorName = debtorName,
                creditorId = debt.creditorId,
                creditorName = creditorName,
                amount = debt.amount
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    /**
     * Records a settlement between two users by creating a settlement expense.
     * The debtor "pays" the amount, and the split assigns the full amount to the creditor.
     * This zeroes out the balance between them for this settlement amount.
     */
    fun recordSettlement(
        debtorId: String,
        creditorId: String,
        amount: Double,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val settlementId = java.util.UUID.randomUUID().toString()

            // Create a settlement expense: the debtor pays the creditor
            val expense = ExpenseEntity(
                id = settlementId,
                groupId = groupId,
                title = "Settlement",
                amount = amount,
                baseAmount = amount,
                currencyCode = "INR",
                category = "Settlement",
                date = now,
                createdBy = debtorId,
                createdAt = now,
                updatedAt = now,
                deletedAt = null
            )

            // The debtor is the payer (they are paying the creditor)
            val payer = ExpensePayerEntity(
                expenseId = settlementId,
                userId = debtorId,
                amount = amount
            )

            // The split assigns the full amount to the creditor only
            // This means: debtor paid `amount`, creditor owes `amount`
            // Net effect: debtor's balance goes up by `amount`, creditor's goes down by `amount`
            // Which cancels out the existing debt.
            val split = ExpenseSplitEntity(
                expenseId = settlementId,
                userId = creditorId,
                shareType = "EXACT",
                value = amount,
                computedAmount = amount
            )

            expenseRepository.createExpense(expense, listOf(payer), listOf(split))
            onComplete()
        }
    }
}
