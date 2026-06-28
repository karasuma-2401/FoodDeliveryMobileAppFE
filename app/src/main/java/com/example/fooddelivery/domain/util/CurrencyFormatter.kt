package com.example.fooddelivery.domain.util

import java.util.Locale

object CurrencyFormatter {
    fun format(amount: Double): String = String.format(Locale.US, "$%.2f", amount)
    
    fun format(amount: String): String = try {
        String.format(Locale.US, "$%.2f", amount.toDouble())
    } catch (e: Exception) {
        "$$amount"
    }
    
    fun format(amount: Int): String = String.format(Locale.US, "$%.2f", amount.toDouble())
}
