package com.example.expensesplit.domain.usecase

import com.example.expensesplit.data.local.entity.ExpenseEntity
import com.example.expensesplit.data.local.entity.ExpensePayerEntity
import com.example.expensesplit.data.local.entity.ExpenseSplitEntity
import javax.inject.Inject
import javax.inject.Singleton

data class FriendBalance(
    val friendId: String,
    val balance: Double // Positive means they owe the user, negative means the user owes them
)

data class GlobalBalanceResult(
    val netBalance: Double,
    val totalOwe: Double,
    val totalOwed: Double,
    val friendBalances: List<FriendBalance>
)

@Singleton
class BalanceCalculator @Inject constructor() {

    fun calculateGlobalBalance(
        userId: String,
        expenses: List<ExpenseEntity>,
        splits: List<ExpenseSplitEntity>,
        payers: List<ExpensePayerEntity>
    ): GlobalBalanceResult {
        val activeExpenseIds = expenses.map { it.id }.toSet()
        val activeSplits = splits.filter { it.expenseId in activeExpenseIds }
        val activePayers = payers.filter { it.expenseId in activeExpenseIds }

        val friendBalancesMap = mutableMapOf<String, Double>()

        for (expenseId in activeExpenseIds) {
            val expenseSplits = activeSplits.filter { it.expenseId == expenseId }
            val expensePayers = activePayers.filter { it.expenseId == expenseId }
            
            val userPaidAmount = expensePayers.firstOrNull { it.userId == userId }?.amount ?: 0.0
            val userSplitAmount = expenseSplits.firstOrNull { it.userId == userId }?.computedAmount ?: 0.0

            val userNetForExpense = userPaidAmount - userSplitAmount

            if (userNetForExpense > 0) {
                // User overpaid, others owe the user
                val underpayers = expenseSplits.map { split ->
                    val paid = expensePayers.firstOrNull { p -> p.userId == split.userId }?.amount ?: 0.0
                    val net = paid - split.computedAmount
                    split.userId to net
                }.filter { it.second < -0.01 && it.first != userId }

                val totalUnderpaid = underpayers.sumOf { Math.abs(it.second) }
                if (totalUnderpaid > 0) {
                    underpayers.forEach { (friendId, underpaidAmount) ->
                        val proportion = Math.abs(underpaidAmount) / totalUnderpaid
                        val owedToUser = userNetForExpense * proportion
                        friendBalancesMap[friendId] = (friendBalancesMap[friendId] ?: 0.0) + owedToUser
                    }
                }
            } else if (userNetForExpense < 0) {
                // User underpaid, user owes others
                val overpayers = expenseSplits.map { split ->
                    val paid = expensePayers.firstOrNull { p -> p.userId == split.userId }?.amount ?: 0.0
                    val net = paid - split.computedAmount
                    split.userId to net
                }.filter { it.second > 0.01 && it.first != userId }

                val totalOverpaid = overpayers.sumOf { it.second }
                if (totalOverpaid > 0) {
                    overpayers.forEach { (friendId, overpaidAmount) ->
                        val proportion = overpaidAmount / totalOverpaid
                        val owedByUser = Math.abs(userNetForExpense) * proportion
                        friendBalancesMap[friendId] = (friendBalancesMap[friendId] ?: 0.0) - owedByUser
                    }
                }
            }
        }

        var totalOwed = 0.0
        var totalOwe = 0.0

        val friendBalancesList = friendBalancesMap.map { (friendId, balance) ->
            if (balance > 0.01) {
                totalOwed += balance
            } else if (balance < -0.01) {
                totalOwe += Math.abs(balance)
            }
            FriendBalance(friendId, balance)
        }.filter { Math.abs(it.balance) > 0.01 }

        val netBalance = totalOwed - totalOwe

        return GlobalBalanceResult(
            netBalance = netBalance,
            totalOwe = totalOwe,
            totalOwed = totalOwed,
            friendBalances = friendBalancesList
        )
    }
}
