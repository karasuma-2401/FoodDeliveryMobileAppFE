package com.example.fooddelivery.domain.model

data class Voucher(
    val id: Int,
    val code: String,
    val title: String,
    val description: String,
    val image: String? = null,
    val discountAmount: Double,
    val minOrderAmount: Double = 0.0,
    val maxDiscountAmount: Double? = null,
    val expiryText: String? = null,
    val startAt: String? = null,
    val type: VoucherType = VoucherType.MONEY,
    val isApplicable: Boolean = true,
    val conditionMessage: String? = null,
    val restaurantName: String? = null
)

enum class VoucherType {
    MONEY, PERCENT, FREE_SHIPPING
}
