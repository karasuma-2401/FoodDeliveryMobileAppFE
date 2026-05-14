package com.example.fooddelivery.domain.model

enum class PaymentType {
    MOMO
}

data class LinkedPaymentMethod(
    val id: String,
    val type: PaymentType = PaymentType.MOMO,
    val name: String,
    val identifier: String,
    val token: String,
    val isDefault: Boolean = false
)
