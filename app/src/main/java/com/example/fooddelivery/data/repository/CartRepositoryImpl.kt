package com.example.fooddelivery.data.repository

import com.example.fooddelivery.domain.model.CartItem
import com.example.fooddelivery.domain.repository.CartRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CartRepositoryImpl @Inject constructor() : CartRepository {
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    override fun getCartItems(): Flow<List<CartItem>> = _cartItems.asStateFlow()
    override fun addToCart(item: CartItem) {
        _cartItems.update { currentItems ->
            val existingItem = currentItems.indexOfFirst {
                it.food.id == item.food.id && it.size == item.size
            }
            if (existingItem != -1) {
                currentItems.mapIndexed { index, cartItem ->
                    if (index == existingItem) {
                        cartItem.copy(quantity = cartItem.quantity + item.quantity)
                    } else cartItem
                }
            } else {
                currentItems + item
            }
        }
    }
    override fun updateQuantity(foodId: String, size: String, delta: Int) {
        _cartItems.update { currentItems ->
            currentItems.map { item ->
                if (item.food.id == foodId && item.size == size) {
                    val newQuantity = (item.quantity + delta).coerceAtLeast(1)
                    item.copy(quantity = newQuantity)
                } else item
            }
        }
    }
    override fun removeItem(foodId: String, size: String) {
        _cartItems.update { currentItems ->
            currentItems.filterNot { it.food.id == foodId && it.size == size } }
        }
    override fun clearCart() {
        _cartItems.value = emptyList()
    }
}