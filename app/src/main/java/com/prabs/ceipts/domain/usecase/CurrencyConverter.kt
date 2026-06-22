package com.prabs.ceipts.domain.usecase

interface CurrencyConverter {
    /**
     * Converts a given [amount] from [fromCurrency] to the base currency (e.g., INR).
     * The rates should ideally be cached daily.
     */
    suspend fun convertToBaseCurrency(amount: Double, fromCurrency: String): Double

    /**
     * Converts an [amount] from the base currency to a target [toCurrency].
     */
    suspend fun convertFromBaseCurrency(amount: Double, toCurrency: String): Double
}
