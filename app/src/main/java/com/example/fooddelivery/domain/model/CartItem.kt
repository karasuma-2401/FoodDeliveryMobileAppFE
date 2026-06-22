package com.example.fooddelivery.domain.model

data class CartItem (
    val food: FoodItem,
    val quantity: Int,
    val unitPrice: Double,
    val restaurantId: String,
    val restaurantName: String,
    val cartItemId: Int? = null,
    val note: String? = null,
    val foodSizeId: String? = null
) {
    val totalPrice: Double get() = unitPrice * quantity
}