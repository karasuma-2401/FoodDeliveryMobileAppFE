package com.example.fooddelivery.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class RestaurantRatingRequest(
    val orderId: Int,
    val vote: Int,
    val comment: String? = null,
    val tags: List<String> = emptyList()
)

@Serializable
data class UpdateReviewRequest(
    val vote: Int,
    val comment: String? = null,
    val tags: List<String> = emptyList()
)

@Serializable
data class FoodRatingResponse(
    val message: String
)

@Serializable
data class RestaurantReviewDto(
    val id: Int,
    val orderId: Int? = null,
    val userId: Int? = null,
    val vote: Int,
    val comment: String? = null,
    val tags: List<String>? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)
