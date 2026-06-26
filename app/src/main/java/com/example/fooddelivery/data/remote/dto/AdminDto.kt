package com.example.fooddelivery.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class AdminDashboardResponse(
    val users: Int,
    val restaurants: Int,
    val orders: Int,
    val payments: Int,
    val categories: Int,
    val vouchers: Int,
    val deliveredRevenue: Double
)

@Serializable
data class PaymentResponse(
    val success: Boolean,
    val data: List<PaymentDto>
)

@Serializable
data class AdminPaymentDto(
    val id: Int,
    val orderId: Int,
    val amount: Double,
    val method: String?,
    val paymentStatus: String?,
    val createdAt: String?,
    val updatedAt: String?,
    val order: AdminOrderDto?
)
@Serializable
data class AdminOrderDto(
    val id: Int,
    val status: String,
    val totalPrice: Double,
    val user: AdminUserDto,
    val restaurant: AdminRestaurantDto
)

@Serializable
data class AdminUserDto(
    val id: Int,
    val name: String,
    val email: String
)

@Serializable
data class AdminRestaurantDto(
    val id: Int,
    val name: String
)