package com.example.fooddelivery.domain.model

data class Category(
    val id: String,
    val name: String,
    val imageUrl: String? = null,
    val imageRes: Int? = null,
    val startingPrice: Double = 0.0,
    val promoText: String? = null
)