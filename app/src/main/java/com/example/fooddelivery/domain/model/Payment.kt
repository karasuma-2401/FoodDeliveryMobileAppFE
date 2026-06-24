package com.example.fooddelivery.domain.model

data class Payment(
    val id: Int,
    val orderId: Int,
    val amount: Double,
    val method: String,
    val paymentStatus: String,
    val createdAt: String,
    val updatedAt: String? = null
)
