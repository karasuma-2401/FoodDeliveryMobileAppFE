package com.example.fooddelivery.domain.model

data class Restaurant(
    val id: String,
    val name: String,
    val description: String = "",
    val tags: List<String>,
    val rating: Float,
    val deliveryFee: Double,
    val deliveryTime: String,
    val imageUrl: String? = null,
    val imageRes: Int? = null,
    val promoTags: List<String> = emptyList()
)
