package com.example.fooddelivery.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class HomeDashboardDto(
    val user: HomeUserDto? = null,
    val categories: List<HomeCategoryDto>,
    val restaurants: List<HomeRestaurantDto>,
    val addresses: List<HomeAddressDto>,
    val counters: HomeCountersDto
)

@Serializable
data class HomeUserDto(
    val id: Int,
    val fullName: String,
    val avatarUrl: String? = null,
    val phone: String? = ""
)

@Serializable
data class HomeCategoryDto(
    val id: Int,
    val name: String,
    val imageUrl: String
)

@Serializable
data class HomeRestaurantDto(
    val id: Int,
    val name: String,
    val imageUrl: String,
    val averageRating: Double,
    val reviewCount: Int,
    val deliveryFee: Double,
    val distance: Double? = null,
    val tags: List<String>,
    val estimatedDeliveryTime: Int,
    val isLiked: Boolean
)

@Serializable
data class HomeAddressDto(
    val id: Int,
    val title: String,
    val fullText: String
)

@Serializable
data class HomeCountersDto(
    val cartItemCount: Int,
    val unreadMessageCount: Int
)
