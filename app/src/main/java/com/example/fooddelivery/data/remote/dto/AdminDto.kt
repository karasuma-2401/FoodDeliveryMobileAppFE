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

@Serializable
data class AdminUserListResponse(
    val success: Boolean,
    val data: List<AdminUserItemDto>,
    val total: Int,
    val limit: Int,
    val offset: Int
)

@Serializable
data class AdminUserItemDto(
    val id: Int,
    val name: String,
    val email: String,
    val phone: String?,
    val active: Boolean,
    val isBlocked: Boolean,
    val blockedReason: String?,
    val blockedAt: String?,
    val createdAt: String,
    val roles: List<String>
)
@Serializable
data class AdminRevenueResponse(
    val success: Boolean,
    val data: AdminRevenueDataDto
)

@Serializable
data class AdminRevenueDataDto(
    val grossRevenue: Double,
    val adminCommissionRate: Double,
    val adminRevenue: Double,
    val restaurants: List<RestaurantRevenueDto>
)

@Serializable
data class RestaurantRevenueDto(
    val restaurantId: Int,
    val restaurantName: String,
    val grossRevenue: Double,
    val adminRevenue: Double
)

@Serializable
data class RestaurantItemDto(
    val id: Int,
    val name: String,
    val phone: String,
    val status: String
)

@Serializable
data class ApprovalRequest(
    val status: String // "APPROVED" hoặc "REJECTED"
)