package com.example.fooddelivery.domain.repository

import com.example.fooddelivery.domain.model.CartItem
import kotlinx.coroutines.flow.Flow

interface CartRepository {
    fun getCartItems(): Flow<List<CartItem>>
    fun addToCart(item: CartItem)
    fun updateQuantity(foodId: String, size: String, delta: Int)
    fun removeItem(foodId: String, size: String)
    fun clearCart()
}