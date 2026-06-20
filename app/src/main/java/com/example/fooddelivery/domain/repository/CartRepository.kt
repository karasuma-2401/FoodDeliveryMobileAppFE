package com.example.fooddelivery.domain.repository

import com.example.fooddelivery.domain.model.CartItem
import kotlinx.coroutines.flow.Flow

interface CartRepository {
    val cartItems: Flow<List<CartItem>>
    suspend fun syncCart(): Result<Unit>
    suspend fun addToCart(foodId: Int, quantity: Int, size: String? = null): Result<Unit>
    suspend fun updateQuantity(cartItemId: Int, quantity: Int): Result<Unit>
    suspend fun removeItem(cartItemId: Int): Result<Unit>
    suspend fun clearCart(): Result<Unit>
}
