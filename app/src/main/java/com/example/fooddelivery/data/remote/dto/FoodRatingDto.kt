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
