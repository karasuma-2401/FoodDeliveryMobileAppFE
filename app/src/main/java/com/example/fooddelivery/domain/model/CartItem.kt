package com.example.fooddelivery.domain.model

data class CartItem (
    val food: FoodItem,
    val quantity: Int,
    val unitPrice: Double,
    val restaurantId: String,
    val restaurantName: String,
    val cartItemId: Int? = null // Server-side ID for updates/deletes
) {
    val totalPrice: Double get() = unitPrice * quantity
}