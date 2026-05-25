package com.example.fooddelivery.domain.repository

import com.example.fooddelivery.domain.model.CartItem
import kotlinx.coroutines.flow.Flow

interface CartRepository {
    val cartItems: Flow<List<CartItem>>
    suspend fun addToCart(item: CartItem)
    suspend fun updateQuantity(foodId: String, size: String, restaurantId: String, delta: Int)
    suspend fun removeItem(foodId: String, size: String, restaurantId: String)
    suspend fun clearCart()
}
