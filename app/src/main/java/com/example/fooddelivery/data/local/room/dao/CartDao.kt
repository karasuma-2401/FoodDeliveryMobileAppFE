package com.example.fooddelivery.data.local.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.fooddelivery.data.local.room.entity.CartEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CartDao {
    @Query("SELECT * FROM cart_items")
    fun getAllCartItems(): Flow<List<CartEntity>>

    @Query("SELECT * FROM cart_items WHERE foodId = :foodId AND restaurantId = :restaurantId LIMIT 1")
    suspend fun getCartItem(foodId: String, restaurantId: String): CartEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCartItem(cartItem: CartEntity)

    @Update
    suspend fun updateCartItem(cartItem: CartEntity)

    @Delete
    suspend fun deleteCartItem(cartItem: CartEntity)

    @Query("DELETE FROM cart_items")
    suspend fun clearCart()

    @Transaction
    suspend fun addToCartAtomic(cartItem: CartEntity) {
        val existingItem = getCartItem(cartItem.foodId, cartItem.restaurantId)
        if (existingItem != null) {
            val updatedQuantity = existingItem.quantity + cartItem.quantity
            updateCartItem(existingItem.copy(quantity = updatedQuantity))
        } else {
            insertCartItem(cartItem)
        }
    }

    @Transaction
    suspend fun updateQuantityAtomic(foodId: String, restaurantId: String, delta: Int) {
        val existingItem = getCartItem(foodId, restaurantId)
        if (existingItem != null) {
            val newQuantity = existingItem.quantity + delta
            if (newQuantity > 0) {
                updateCartItem(existingItem.copy(quantity = newQuantity))
            } else {
                deleteCartItem(existingItem)
            }
        }
    }
}
