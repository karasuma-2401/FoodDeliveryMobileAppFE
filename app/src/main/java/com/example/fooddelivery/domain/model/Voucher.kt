package com.example.fooddelivery.domain.model

data class Voucher(
    val id: String,
    val code: String,
    val title: String,
    val description: String,
    val discountAmount: Double,
    val minOrderAmount: Double = 0.0,
    val expiryText: String? = null,
    val type: VoucherType = VoucherType.DISCOUNT,
    val isApplicable: Boolean = true,
    val conditionMessage: String? = null
)

enum class VoucherType {
    DISCOUNT, FREESHIP
}