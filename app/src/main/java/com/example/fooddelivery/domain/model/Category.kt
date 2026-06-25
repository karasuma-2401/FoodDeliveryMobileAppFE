package com.example.fooddelivery.domain.model

data class Category(
    val id: String,
    val name: String,
    val imageUrl: String? = null,
    val description: String? = null,
    val foodCount: Int = 0,
    val displayOrder: Int = 0,
    val isActive: Boolean = true
)