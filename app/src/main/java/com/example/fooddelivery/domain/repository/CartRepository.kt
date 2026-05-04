package com.example.fooddelivery.domain.repository

import com.example.fooddelivery.domain.model.CartItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface CartRepository {
    fun getCartItems(): Flow<List<CartItem>>
    val cartItems: StateFlow<List<CartItem>>
    fun addToCart(item: CartItem)
    fun updateQuantity(foodId: String, size: String, delta: Int)
    fun removeItem(foodId: String, size: String)
    fun clearCart()
}