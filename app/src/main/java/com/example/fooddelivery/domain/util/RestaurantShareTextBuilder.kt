package com.example.fooddelivery.domain.util

import com.example.fooddelivery.domain.model.Restaurant
import com.example.fooddelivery.domain.model.Voucher
import com.example.fooddelivery.domain.model.VoucherType
import java.util.Locale

object RestaurantShareTextBuilder {
    private const val APP_NAME = "DFood"

    fun build(
        restaurant: Restaurant,
        vouchers: List<Voucher> = emptyList()
    ): String = buildString {
        appendLine("Check out ${restaurant.name} on $APP_NAME!")
        if (restaurant.rating > 0f) {
            val reviews = if (restaurant.reviewCount > 0) {
                " (${restaurant.reviewCount}+ reviews)"
            } else {
                ""
            }
            appendLine("⭐ ${"%.1f".format(Locale.US, restaurant.rating)}$reviews")
        }
        bestVoucherLine(vouchers)?.let { appendLine(it) }
        if (restaurant.description.isNotBlank()) {
            appendLine(restaurant.description.trim().take(120))
        }
        appendLine()
        append("Order on $APP_NAME today!")
    }

    private fun bestVoucherLine(vouchers: List<Voucher>): String? {
        val bestPercent = vouchers
            .filter { it.type == VoucherType.PERCENT }
            .maxByOrNull { it.discountAmount }
        if (bestPercent != null) {
            return "Vouchers available — save up to ${bestPercent.discountAmount.toInt()}%!"
        }
        val bestMoney = vouchers
            .filter { it.type == VoucherType.MONEY }
            .maxByOrNull { it.discountAmount }
        return bestMoney?.let {
            "Vouchers available — save up to $${it.discountAmount.toInt()}!"
        }
    }
}
