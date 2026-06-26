package com.example.fooddelivery.domain.model

data class CategoryDetail(
    val id: String,
    val name: String,
    val imageUrl: String? = null,
    val description: String? = null,
    val foods: List<FoodItem> = emptyList()
)
