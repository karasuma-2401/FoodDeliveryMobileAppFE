package com.example.fooddelivery.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class UserProfileResponse(
    val id: Int? = null,
    val name: String,
    val email: String,
    val phone: String,
    val birthday: String? = null,
    val avatar: String? = null
)

@Serializable
data class UserReviewDto(
    val id: Int,
    val restaurantId: Int,
    val restaurantName: String,
    val restaurantImage: String? = null,
    val orderId: Int,
    val vote: Int,
    val comment: String? = null,
    val tags: List<String> = emptyList(),
    val createdAt: String
)

@Serializable
data class PaginationDto(
    val total: Int,
    val limit: Int,
    val offset: Int
)

@Serializable
data class FavoriteRestaurantResponse(
    val data: List<RestaurantResponse>,
    val pagination: PaginationDto
)
