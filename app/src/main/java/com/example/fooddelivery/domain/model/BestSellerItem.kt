package com.example.fooddelivery.domain.model

data class BestSellerItem(
    val name: String,
    val price: String,
    val rating: Float,
    val soldCount: Int,
    val imageRes: Int
)