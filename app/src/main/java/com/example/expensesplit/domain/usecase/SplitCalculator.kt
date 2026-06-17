package com.example.expensesplit.domain.usecase

import com.example.expensesplit.domain.model.ShareType
import javax.inject.Inject
import kotlin.math.round

data class SplitSpec(
    val userId: String,
    val shareType: ShareType,
    val value: Double = 0.0
)

data class SplitResult(
    val userId: String,
    val computedAmount: Double
)

class SplitCalculator @Inject constructor() {

    /**
     * Calculates the exact monetary amount for each participant in a split,
     * ensuring the total precisely matches the [totalAmount].
     * Any rounding remainder (e.g., 0.01) is added to the [payerId]'s share
     * (or the first user if payer is not in the split).
     */
    fun calculateSplits(
        totalAmount: Double,
        payerId: String,
        specs: List<SplitSpec>
    ): List<SplitResult> {
        if (specs.isEmpty()) return emptyList()

        val results = mutableListOf<SplitResult>()

        when {
            // If all are EQUAL, divide evenly
            specs.all { it.shareType == ShareType.EQUAL } -> {
                val splitAmount = roundToTwoDecimals(totalAmount / specs.size)
                specs.forEach { results.add(SplitResult(it.userId, splitAmount)) }
            }

            // If EXACT, just take the values
            specs.all { it.shareType == ShareType.EXACT } -> {
                specs.forEach { results.add(SplitResult(it.userId, roundToTwoDecimals(it.value))) }
            }

            // If PERCENT, calculate based on total
            specs.all { it.shareType == ShareType.PERCENT } -> {
                val totalPercent = specs.sumOf { it.value }
                if (roundToTwoDecimals(totalPercent) != 100.0) {
                    throw IllegalArgumentException("Percentages must sum to 100")
                }
                specs.forEach {
                    val amount = roundToTwoDecimals(totalAmount * (it.value / 100.0))
                    results.add(SplitResult(it.userId, amount))
                }
            }

            // If SHARES, calculate based on total shares
            specs.all { it.shareType == ShareType.SHARES } -> {
                val totalShares = specs.sumOf { it.value }
                if (totalShares <= 0) {
                    throw IllegalArgumentException("Total shares must be > 0")
                }
                specs.forEach {
                    val amount = roundToTwoDecimals(totalAmount * (it.value / totalShares))
                    results.add(SplitResult(it.userId, amount))
                }
            }

            else -> {
                throw IllegalArgumentException("Cannot mix ShareTypes in a single expense split")
            }
        }

        // Adjust for rounding errors
        val computedTotal = results.sumOf { it.computedAmount }
        val difference = roundToTwoDecimals(totalAmount - computedTotal)

        if (difference != 0.0) {
            // Find payer in the split, or just the first person
            val adjustmentIndex = results.indexOfFirst { it.userId == payerId }.takeIf { it != -1 } ?: 0
            val adjustedResult = results[adjustmentIndex].copy(
                computedAmount = roundToTwoDecimals(results[adjustmentIndex].computedAmount + difference)
            )
            results[adjustmentIndex] = adjustedResult
        }

        return results
    }

    private fun roundToTwoDecimals(value: Double): Double {
        return round(value * 100) / 100
    }
}
