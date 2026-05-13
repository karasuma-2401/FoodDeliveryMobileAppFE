package com.example.fooddelivery.domain.model

data class FoodItem(
    val id: String = "",
    val name: String,
    val restaurantId: String = "",
    val restaurantName: String,
    val categoryId: String = "",
    val price: Double,
    val rating: Float = 0f,
    val reviewCount: Int = 0,
    val soldCount: Int = 0,
    val imageUrl: String? = null,
    val imageRes: Int? = null,
    val promoTag: String? = null
) {
    init {
        require(id.isNotEmpty()) { "FoodItem id must not be empty" }
        require(price >= 0.0) { "FoodItem price must not be negative" }
        require(reviewCount >= 0) { "FoodItem reviewCount must not be negative" }
        require(soldCount >= 0) { "FoodItem soldCount must not be negative" }
    }
}
