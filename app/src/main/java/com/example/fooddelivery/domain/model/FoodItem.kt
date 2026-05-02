package com.example.fooddelivery.domain.model

data class FoodItem(
    val id: String = "",
    val name: String,
    val restaurantName: String,
    val category: String,
    val price: String,
//    val rating: Float,
//    val reviewCount: Int,
    val imageRes: Int,
    val promoTag: String? = null
)
