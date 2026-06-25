package com.example.fooddelivery.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class CheckPaymentRequest(
    val momoOrderId: String,
    val status: String // "DONE" or "FAILED"
)

@Serializable
data class PaymentStatusResponse(
    val id: Int,
    val orderId: Int,
    val amount: Double,
    val method: String,
    val paymentStatus: String,
    val createdAt: String,
    val updatedAt: String? = null,
    val deleteAt: String? = null
)
