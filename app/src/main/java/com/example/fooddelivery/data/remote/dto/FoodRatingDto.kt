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

// ===== Vendor Review DTOs =====

@Serializable
data class ReviewUserDto(
    val id: Int,
    val name: String,
    val avatar: String? = null
)

@Serializable
data class VendorReviewResponse(
    val id: Int,
    val vote: Int,
    val comment: String? = null,
    val tags: List<String> = emptyList(),
    val createdAt: String,
    val reply: String? = null,
    val user: ReviewUserDto,
    val orderId: Int? = null
)

@Serializable
data class RatingTagCount(
    val tag: String,
    val count: Int
)

@Serializable
data class RatingStarCount(
    val `1`: Int = 0,
    val `2`: Int = 0,
    val `3`: Int = 0,
    val `4`: Int = 0,
    val `5`: Int = 0
)

@Serializable
data class RatingStatsResponse(
    val averageRating: Double,
    val totalReviews: Int,
    val starCount: RatingStarCount,
    val popularTags: List<RatingTagCount> = emptyList()
)

@Serializable
data class ReplyRequest(
    val reply: String
)

@Serializable
data class RestaurantReviewDataDto(
    val id: Int,
    val name: String,
    val status: String,
    val averageRating: Double = 0.0,
    val ratingCount: Int = 0,
    val ratings: List<VendorReviewResponse> = emptyList()
)