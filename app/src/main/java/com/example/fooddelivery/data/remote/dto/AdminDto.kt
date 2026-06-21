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
