package com.example.fooddelivery.domain.repository

import com.example.fooddelivery.data.remote.dto.CartResponse
import com.example.fooddelivery.domain.model.CartItem
import com.example.fooddelivery.domain.model.CartRestaurantGroup
import kotlinx.coroutines.flow.Flow

interface CartRepository {
    val cartItems: Flow<List<CartItem>>
    val restaurantGroups: Flow<List<CartRestaurantGroup>>
    suspend fun syncCart(): Result<Unit>
    suspend fun applyServerCart(cartResponse: CartResponse): Result<Unit>
    suspend fun addToCart(foodId: Int, quantity: Int, size: String? = null, note: String? = null): Result<Unit>
    suspend fun addToCartWithOptimisticLocal(
        foodId: Int,
        quantity: Int,
        foodSizeId: Int?,
        note: String?,
        optimisticItem: CartItem
    ): Result<Unit>
    suspend fun updateQuantity(cartItemId: Int, quantity: Int): Result<Unit>
    suspend fun removeItem(cartItemId: Int): Result<Unit>
    suspend fun clearCartByRestaurant(restaurantId: Int): Result<Unit>
    suspend fun clearCart(): Result<Unit>
}
