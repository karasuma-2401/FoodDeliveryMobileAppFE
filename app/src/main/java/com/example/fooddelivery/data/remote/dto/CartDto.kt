package com.example.fooddelivery.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class AddToCartRequest(
    val foodId: Int,
    val quantity: Int,
    val foodSizeId: Int? = null,
    val fullText: String? = null
)

@Serializable
data class UpdateCartItemRequest(
    val quantity: Int
)

@Serializable
data class CartResponse(
    val id: Int,
    val totalItems: Int,
    val subtotal: Double,
    val restaurant: CartRestaurantResponse? = null,
    val items: List<CartItemResponse>
)

@Serializable
data class CartItemResponse(
    val id: Int,
    val quantity: Int,
    val lineTotal: Double,
    val foodSizeId: Int? = null,
    val sizeName: String? = null,
    val fullText: String? = null,
    val food: CartFoodResponse
)

@Serializable
data class CartFoodResponse(
    val id: Int,
    val name: String,
    val price: Double,
    val image: String,
    val label: String? = null,
    val restaurantId: Int,
    val restaurant: CartRestaurantResponse? = null,
    val category: CartCategoryResponse? = null
)

@Serializable
data class CartRestaurantResponse(
    val id: Int,
    val name: String
)

@Serializable
data class CartCategoryResponse(
    val id: Int,
    val name: String
)
