package com.example.fooddelivery.domain.model

data class CartRestaurantGroup(
    val restaurantId: String,
    val restaurantName: String,
    val imageUrl: String? = null,
    val deliveryFee: Double? = null,
    val estimatedDeliveryTime: Int? = null,
    val itemCount: Int = 0,
    val subtotal: Double = 0.0,
)

data class ReorderResult(
    val message: String,
    val addedCount: Int,
    val skippedItems: List<SkippedReorderItem> = emptyList(),
)

data class SkippedReorderItem(
    val foodId: Int,
    val reason: String,
)
