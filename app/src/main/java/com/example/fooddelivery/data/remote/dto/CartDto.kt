package com.example.fooddelivery.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class AddToCartRequest(
    val quantity: Int,
    val food: AddToCartFoodRequest
)

@Serializable
data class AddToCartFoodRequest(
    val id: Int,
    val size: String? = null
)

@Serializable
data class UpdateCartItemRequest(
    val quantity: Int
)

@Serializable
data class CartItemResponse(
    val id: Int,
    val quantity: Int,
    val food: CartFoodResponse
)

@Serializable
data class CartFoodResponse(
    val id: Int,
    val name: String,
    val price: Double,
    val image: String,
    val restaurantId: Int,
    val restaurantName: String? = null,
    val size: String? = null
)

@Serializable
data class CartResponse(
    val items: List<CartItemResponse>,
    val totalPrice: Double
)
