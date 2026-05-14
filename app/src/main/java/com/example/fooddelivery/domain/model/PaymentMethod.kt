package com.example.fooddelivery.domain.model

enum class PaymentMethodType {
    MOMO,
    VISA,
    MASTERCARD,
    CASH
}

data class PaymentMethod(
    val id: String,
    val type: PaymentMethodType,
    val provider: String,
    val maskDisplay: String,
    val token: String,
    val isDefault: Boolean = false
)
