package com.prabs.ceipts.domain.usecase

import com.prabs.ceipts.data.local.entity.ExpenseEntity
import com.prabs.ceipts.data.local.entity.ExpensePayerEntity
import com.prabs.ceipts.data.local.entity.ExpenseSplitEntity
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

    /**
     * Calculates the global balance for [userId] across all expenses.
     *
     * For each expense, computes each participant's net position (paid − split).
     * Then distributes the user's overpayment to underpayers (friends who owe user)
     * and the user's underpayment to overpayers (friends the user owes).
     */
    fun calculateGlobalBalance(
        userId: String,
        expenses: List<ExpenseEntity>,
        splits: List<ExpenseSplitEntity>,
        payers: List<ExpensePayerEntity>
    ): GlobalBalanceResult {
        val activeExpenseIds = expenses.map { it.id }.toSet()
        val activeSplits = splits.filter { it.expenseId in activeExpenseIds }
        val activePayers = payers.filter { it.expenseId in activeExpenseIds }

        // Accumulate per-friend balances across all expenses
        val friendBalancesMap = mutableMapOf<String, Double>()

        for (expenseId in activeExpenseIds) {
            val expenseSplits = activeSplits.filter { it.expenseId == expenseId }
            val expensePayers = activePayers.filter { it.expenseId == expenseId }

            // Compute net for every participant in this expense: paid - owed
            // Positive net = overpaid, Negative net = underpaid
            val participantIds = (expenseSplits.map { it.userId } + expensePayers.map { it.userId }).toSet()
            val nets = mutableMapOf<String, Double>()
            for (pid in participantIds) {
                val paid = expensePayers.filter { it.userId == pid }.sumOf { it.amount }
                val owed = expenseSplits.filter { it.userId == pid }.sumOf { it.computedAmount }
                nets[pid] = paid - owed
            }

            val userNet = nets[userId] ?: 0.0

            if (userNet > 0.01) {
                // User overpaid → friends who underpaid owe the user
                // Distribute user's overpayment proportionally among all underpayers
                val underpayers = nets.filter { it.key != userId && it.value < -0.01 }
                val totalUnderpaid = underpayers.values.sumOf { Math.abs(it) }
                if (totalUnderpaid > 0.01) {
                    for ((friendId, friendNet) in underpayers) {
                        // The friend owes a portion of the user's overpayment
                        val friendUnderpaid = Math.abs(friendNet)
                        val proportion = friendUnderpaid / totalUnderpaid
                        val friendOwes = userNet * proportion
                        friendBalancesMap[friendId] = (friendBalancesMap[friendId] ?: 0.0) + friendOwes
                    }
                }
            } else if (userNet < -0.01) {
                // User underpaid → user owes friends who overpaid
                val overpayers = nets.filter { it.key != userId && it.value > 0.01 }
                val totalOverpaid = overpayers.values.sumOf { it }
                if (totalOverpaid > 0.01) {
                    for ((friendId, friendNet) in overpayers) {
                        val proportion = friendNet / totalOverpaid
                        val userOwes = Math.abs(userNet) * proportion
                        friendBalancesMap[friendId] = (friendBalancesMap[friendId] ?: 0.0) - userOwes
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
