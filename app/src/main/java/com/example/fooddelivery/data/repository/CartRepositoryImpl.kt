package com.example.fooddelivery.data.repository

import com.example.fooddelivery.domain.model.CartItem
import com.example.fooddelivery.domain.repository.CartRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CartRepositoryImpl @Inject constructor() : CartRepository {
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    override val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    override fun addToCart(item: CartItem) {
        _cartItems.update { currentList ->
            val existingItem = currentList.find { 
                it.food.id == item.food.id && it.size == item.size && it.restaurantId == item.restaurantId 
            }
            if (existingItem != null) {
                currentList.map {
                    if (it == existingItem) it.copy(quantity = it.quantity + item.quantity) else it
                }
            } else {
                currentList + item
            }
        }
    }

    override fun clearCart() {
        _cartItems.value = emptyList()
    }
}
