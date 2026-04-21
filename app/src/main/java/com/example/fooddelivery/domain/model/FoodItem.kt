package com.example.fooddelivery.domain.model

data class FoodItem(
    val name: String,
    val category: String,
    val price: String,
    val rating: Float,
    val reviewCount: Int,
    val imageRes: Int
)