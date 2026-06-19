package com.example.fooddelivery.domain.model

data class ReviewItem(
    val id: String,
    val date: String,
    val title: String,
    val rating: Int,
    val description: String,
    val userAvatar: Int? = null
)