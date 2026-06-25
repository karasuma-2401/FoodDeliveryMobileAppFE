package com.example.fooddelivery.domain.model

data class RestaurantRevenue(
    val netRevenue: Double,
    val grossRevenue: Double,
    val platformFee: Double,
    val totalOrders: Int,
    val orderHistory: List<RevenueDetailItem>
)

data class RevenueDetailItem(
    val orderId: String,
    val totalAmount: Double,
    val platformCommission: Double,
    val restaurantNetRevenue: Double,
    val completedAt: String,
    val paymentMethod: String,
    val customerName: String
)