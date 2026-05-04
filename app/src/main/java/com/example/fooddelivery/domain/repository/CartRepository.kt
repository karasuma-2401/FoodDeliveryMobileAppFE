package com.example.fooddelivery.domain.repository

import com.example.fooddelivery.domain.model.CartItem
import kotlinx.coroutines.flow.StateFlow

interface CartRepository {
    val cartItems: StateFlow<List<CartItem>>
    fun addToCart(item: CartItem)
    fun clearCart()
}
