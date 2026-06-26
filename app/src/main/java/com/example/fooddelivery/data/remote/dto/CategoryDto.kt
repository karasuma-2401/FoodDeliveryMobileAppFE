package com.example.fooddelivery.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class CategoryFoodDto(
    val id: Int,
    val name: String,
    val image: String? = null,
    val price: Double,
    val restaurant: FoodRestaurantDto? = null
)

@Serializable
data class CategoryResponse(
    val id: Int,
    val name: String,
    val image: String = "",
    val description: String? = null,
    val sortOrder: Int? = null,
    val displayOrder: Int? = null,
    val isActive: Boolean? = null,
    val foodCount: Int? = null
)

@Serializable
data class CategoryDetailResponse(
    val id: Int,
    val name: String,
    val image: String = "",
    val description: String? = null,
    val sortOrder: Int? = null,
    val displayOrder: Int? = null,
    val isActive: Boolean? = null,
    val foodCount: Int? = null,
    val foods: List<CategoryFoodDto>? = null
)
