package com.example.fooddelivery.domain.util

import com.example.fooddelivery.domain.model.FoodItem
import com.example.fooddelivery.domain.model.Voucher
import java.util.Locale

object FoodShareTextBuilder {
    private const val APP_NAME = "DFood"

    fun build(
        food: FoodItem,
        unitPrice: Double,
        vouchers: List<Voucher> = emptyList()
    ): String = buildString {
        appendLine("Check out ${food.name} at ${food.restaurantName} on $APP_NAME!")
        appendLine("$${String.format(Locale.US, "%.0f", unitPrice)}")
        food.promoTag?.takeIf { it.isNotBlank() }?.let { appendLine(it) }
        bestVoucherLine(vouchers)?.let { appendLine(it) }
        appendLine()
        append("Order on $APP_NAME today!")
    }

    private fun bestVoucherLine(vouchers: List<Voucher>): String? =
        vouchers.pickBestDomainBadgeLabel()?.let { "Vouchers available — $it" }
}
