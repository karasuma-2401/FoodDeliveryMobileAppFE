package com.example.fooddelivery.domain.model

data class CartItem (
    val food: FoodItem,
    val size: String,
    val quantity: Int,
    val unitPrice: Double,
    val restaurantId: String,
    val restaurantName: String
) {
    val totalPrice: Double get() = unitPrice * quantity
}