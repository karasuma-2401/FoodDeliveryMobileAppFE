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
data class FoodResponse(
    val id: String,
    val name: String,
    val price: Double,
    val details: String,
    val category: String,
    val rating: Float,
    val reviewCount: Int,
    val imageUrl: String? = null
)

@Serializable
data class BaseResponse<T>(
    val data: T? = null,
    val message: String? = null,
    val isSuccess: Boolean
)
