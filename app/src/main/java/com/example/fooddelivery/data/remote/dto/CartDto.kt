package com.example.fooddelivery.data.remote.dto

import com.example.fooddelivery.domain.model.CartRestaurantGroup
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
    val restaurantGroups: List<CartRestaurantGroupResponse> = emptyList(),
    val restaurant: CartRestaurantResponse? = null,
    val items: List<CartItemResponse>
)

@Serializable
data class CartRestaurantGroupResponse(
    val restaurant: CartRestaurantResponse,
    val itemCount: Int,
    val subtotal: Double,
    val items: List<CartItemResponse> = emptyList(),
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
    val name: String,
    val image: String? = null,
    val deliveryFee: Double? = null,
    val estimatedDeliveryTime: Int? = null,
)

@Serializable
data class CartCategoryResponse(
    val id: Int,
    val name: String
)

fun CartRestaurantGroupResponse.toDomain(): CartRestaurantGroup {
    return CartRestaurantGroup(
        restaurantId = restaurant.id.toString(),
        restaurantName = restaurant.name,
        imageUrl = restaurant.image,
        deliveryFee = restaurant.deliveryFee,
        estimatedDeliveryTime = restaurant.estimatedDeliveryTime,
        itemCount = itemCount,
        subtotal = subtotal,
    )
}

fun CartResponse.toRestaurantGroups(): List<CartRestaurantGroup> {
    if (restaurantGroups.isNotEmpty()) {
        return restaurantGroups.map { it.toDomain() }
    }
    return items
        .groupBy { it.food.restaurantId }
        .map { (restaurantId, groupItems) ->
            val restaurant = groupItems.first().food.restaurant
            CartRestaurantGroup(
                restaurantId = restaurantId.toString(),
                restaurantName = restaurant?.name ?: "Unknown",
                imageUrl = restaurant?.image,
                deliveryFee = restaurant?.deliveryFee,
                estimatedDeliveryTime = restaurant?.estimatedDeliveryTime,
                itemCount = groupItems.sumOf { it.quantity },
                subtotal = groupItems.sumOf { it.lineTotal },
            )
        }
}
