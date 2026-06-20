package com.example.fooddelivery.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class OrderRequest(
    val restaurantId: Int,
    val items: List<OrderItemRequest>,
    val addressId: Int,
    val deliveryOption: String,
    val paymentMethod: String,
    val note: String? = null,
    val totalAmount: Double
)

@Serializable
data class OrderItemRequest(
    val quantity: Int,
    val price: Double,
    val food: OrderFoodRequest
)

@Serializable
data class OrderFoodRequest(
    val id: Int,
    val size: String? = null
)

@Serializable
data class OrderResponse(
    val id: Int,
    val status: String,
    val orderCode: String? = null,
    val totalAmount: Double? = null,
    val createdAt: String? = null,
    val deeplink: String? = null
)

@Serializable
data class OrderDetailResponse(
    val id: Int,
    val status: String,
    val totalAmount: Double,
    val items: List<OrderItemDetailResponse>,
    val restaurantName: String,
    val createdAt: String
)

@Serializable
data class OrderItemDetailResponse(
    val foodName: String,
    val quantity: Int,
    val price: Double,
    val image: String? = null,
    val size: String? = null
)
