package com.example.expensesplit.domain.usecase

import com.example.expensesplit.data.local.entity.ExpensePayerEntity
import com.example.expensesplit.data.local.entity.ExpenseSplitEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BalanceCalculator @Inject constructor() {

    /**
     * Calculates the net balances for each user based on expenses, payers, and splits.
     * Returns a Map<UserId, Balance> where positive is owed to the user, and negative is they owe.
     */
    fun calculateBalances(
        payers: List<ExpensePayerEntity>,
        splits: List<ExpenseSplitEntity>
    ): Map<String, Double> {
        val balances = mutableMapOf<String, Double>()

        // Credit users who paid
        for (payer in payers) {
            val currentBalance = balances.getOrDefault(payer.userId, 0.0)
            balances[payer.userId] = currentBalance + payer.amount
        }

        // Debit users who owe
        for (split in splits) {
            val currentBalance = balances.getOrDefault(split.userId, 0.0)
            balances[split.userId] = currentBalance - split.amount
        }

        return balances
    }
}
