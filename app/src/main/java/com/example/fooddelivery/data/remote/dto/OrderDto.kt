package com.example.fooddelivery.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class OrderRequest(
    val restaurantId: String,
    val items: List<OrderItemRequest>,
    val addressId: String,
    val deliveryOption: String,
    val paymentMethod: String,
    val note: String,
    val totalAmount: Double
)

@Serializable
data class OrderItemRequest(
    val foodId: String,
    val quantity: Int,
    val price: Double,
    val size: String
)

@Serializable
data class OrderResponse(
    val orderId: String,
    val status: String,
    val deeplink: String? = null
)
