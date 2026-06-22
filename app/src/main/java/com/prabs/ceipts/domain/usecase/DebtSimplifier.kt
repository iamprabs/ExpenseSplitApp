package com.prabs.ceipts.domain.usecase

import javax.inject.Inject
import javax.inject.Singleton

data class Debt(val debtorId: String, val creditorId: String, val amount: Double)

@Singleton
class DebtSimplifier @Inject constructor() {

    /**
     * Simplifies debts in a group using a greedy approach.
     * @param balances A map of userId to their net balance (positive = they are owed money, negative = they owe money).
     * @return A list of simplified debts.
     */
    fun simplifyDebts(balances: Map<String, Double>): List<Debt> {
        val debtors = balances.filterValues { it < -0.01 }.toMutableMap()
        val creditors = balances.filterValues { it > 0.01 }.toMutableMap()

        val simplifiedDebts = mutableListOf<Debt>()

        while (debtors.isNotEmpty() && creditors.isNotEmpty()) {
            // Find max debtor and max creditor
            val maxDebtor = debtors.maxByOrNull { -it.value }!!
            val maxCreditor = creditors.maxByOrNull { it.value }!!

            val debtorId = maxDebtor.key
            val creditorId = maxCreditor.key
            val debtAmount = -maxDebtor.value
            val creditAmount = maxCreditor.value

            val settledAmount = minOf(debtAmount, creditAmount)

            simplifiedDebts.add(Debt(debtorId, creditorId, settledAmount))

            // Update balances
            debtors[debtorId] = - (debtAmount - settledAmount)
            creditors[creditorId] = creditAmount - settledAmount

            // Remove settled individuals
            if (-debtors[debtorId]!! < 0.01) debtors.remove(debtorId)
            if (creditors[creditorId]!! < 0.01) creditors.remove(creditorId)
        }

        return simplifiedDebts
    }
}
