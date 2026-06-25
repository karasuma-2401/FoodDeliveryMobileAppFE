package com.example.fooddelivery.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class RevenueResponseDto(
    val success: Boolean,
    val data: RevenueDataWrapperDto
)

@Serializable
data class RevenueDataWrapperDto(
    val success: Boolean,
    val data: List<RevenueItemDto>,
    val total: Int,
    val limit: Int,
    val offset: Int
)

@Serializable
data class RevenueItemDto(
    val orderId: String,
    val totalAmount: Double,
    val platformCommission: Double,
    val restaurantNetRevenue: Double,
    val completedAt: String,
    val paymentMethod: String,
    val customerName: String
)