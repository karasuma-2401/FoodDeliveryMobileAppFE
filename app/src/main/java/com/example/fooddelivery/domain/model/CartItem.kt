package com.example.fooddelivery.domain.model

data class CartItem(
    val food: FoodItem,
    val quantity: Int,
    val lineTotal: Double,
    val restaurantId: String,
    val restaurantName: String,
    val cartItemId: Int? = null,
    val note: String? = null,
    val foodSizeId: String? = null
) {
    val unitPrice: Double get() = if (quantity > 0) lineTotal / quantity else 0.0
    val totalPrice: Double get() = lineTotal
}
