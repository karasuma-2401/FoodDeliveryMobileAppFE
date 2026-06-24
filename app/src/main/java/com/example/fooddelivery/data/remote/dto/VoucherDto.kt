package com.example.fooddelivery.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class VoucherRestaurantDto(
    val id: Int,
    val name: String
)

@Serializable
data class VoucherDto(
    val id: Int,
    val name: String,
    val code: String,
    val description: String? = null,
    val image: String? = null,
    val sale: Double,
    val type: String,
    val status: String,
    val minimumOrderAmount: Double = 0.0,
    val maximumDiscountAmount: Double? = null,
    val startAt: String? = null,
    val endAt: String? = null,
    val restaurant: VoucherRestaurantDto? = null,
    val restaurantId: Int? = null
)

