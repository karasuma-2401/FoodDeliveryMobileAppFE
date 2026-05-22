package com.example.fooddelivery.data.repository

import com.example.fooddelivery.data.local.room.dao.CartDao
import com.example.fooddelivery.data.local.room.entity.toDomain
import com.example.fooddelivery.data.local.room.entity.toEntity
import com.example.fooddelivery.domain.model.CartItem
import com.example.fooddelivery.domain.repository.CartRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CartRepositoryImpl @Inject constructor(
    private val cartDao: CartDao
) : CartRepository {
    override val cartItems: Flow<List<CartItem>> = cartDao.getAllCartItems().map { entities ->
        entities.map { it.toDomain() }
    }

    override suspend fun addToCart(cartItem: CartItem) {
        val existingItem = cartDao.getCartItem(
            foodId = cartItem.food.id,
            size = cartItem.size,
            restaurantId = cartItem.restaurantId
        )

        if (existingItem != null) {
            val updatedQuantity = existingItem.quantity + cartItem.quantity
            cartDao.updateCartItem(existingItem.copy(quantity = updatedQuantity))
        } else {
            cartDao.insertCartItem(cartItem.toEntity())
        }
    }

    override suspend fun updateQuantity(foodId: String, size: String, restaurantId: String, delta: Int) {
        val existingItem = cartDao.getCartItem(foodId, size, restaurantId)

        if (existingItem != null) {
            val newQuantity = existingItem.quantity + delta
            if (newQuantity > 0) {
                cartDao.updateCartItem(existingItem.copy(quantity = newQuantity))
            } else {
                cartDao.deleteCartItem(existingItem)
            }
        }
    }

    override suspend fun removeItem(foodId: String, size: String, restaurantId: String) {
        val existingItem = cartDao.getCartItem(foodId, size, restaurantId)
        if (existingItem != null) {
            cartDao.deleteCartItem(existingItem)
        }
    }

    override suspend fun clearCart() {
        cartDao.clearCart()
    }
}
