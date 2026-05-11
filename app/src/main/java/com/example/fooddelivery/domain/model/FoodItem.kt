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
    }
}
