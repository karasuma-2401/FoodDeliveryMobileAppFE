package com.example.fooddelivery.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class DashboardResponse(
    val runningOrders: Int,
    val orderRequest: Int,
    val revenue: Double,
    val rating: Double,
    val totalReviews: Int
)

@Serializable
data class FoodRequest(
    val name: String,
    val price: Double,
    val details: String,
    val category: String,
    val imageUrl: String? = null
)

@Serializable
data class FoodSizeRequest(
    val sizeId: Int,
    val price: Double,
    val isDefault: Boolean = false
)

@Serializable
data class FoodResponse(
    val id: Int,
    val name: String,
    val price: Double,
    val description: String,
    val categoryId: Int,
    val restaurantId: Int,
    val label: String? = null,
    val isAvailable: Boolean = true,
    val image: String? = null,
    val rating: Float? = null,
    val reviewCount: Int? = null
)

@Serializable
data class BaseResponse<T>(
    val data: T? = null,
    val message: String? = null,
    val success: Boolean
)

@Serializable
data class RestaurantResponse(
    val id: Int,
    val name: String,
    val image: String,
    val coverImage: String? = null,
    val description: String? = null,
    val phone: String? = null,
    val deliveryFee: Double? = null,
    val minimumOrder: Double? = null,
    val estimatedDeliveryTime: Int? = null,
    val address: RestaurantAddressDto? = null,
    val averageRating: Double? = null,
    val rating: Double? = null, // Dùng cho API /user/favorites
    val ratingCount: Int? = null,
    val categories: List<RestaurantCategoryDto>? = null,
    val tags: List<String>? = null, // Dùng cho API /user/favorites
    val startingPrice: Double? = null,
    val isLiked: Boolean? = false,
    val totalLikes: Int? = 0
)

@Serializable
data class LikeStatusResponse(
    val restaurantId: Int? = null,
    val isLiked: Boolean,
    val totalLikes: Int? = null
)

@Serializable
data class RestaurantAddressDto(
    val id: Int,
    val title: String,
    val fullText: String
)

@Serializable
data class RestaurantCategoryDto(
    val id: Int,
    val name: String
)
