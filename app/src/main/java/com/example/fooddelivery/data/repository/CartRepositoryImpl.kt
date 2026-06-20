package com.example.fooddelivery.data.repository

import com.example.fooddelivery.data.local.room.dao.CartDao
import com.example.fooddelivery.data.local.room.entity.CartEntity
import com.example.fooddelivery.data.local.room.entity.toDomain
import com.example.fooddelivery.data.remote.api.CartApi
import com.example.fooddelivery.data.remote.dto.AddToCartFoodRequest
import com.example.fooddelivery.data.remote.dto.AddToCartRequest
import com.example.fooddelivery.data.remote.dto.UpdateCartItemRequest
import com.example.fooddelivery.domain.model.CartItem
import com.example.fooddelivery.domain.repository.CartRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.cancellation.CancellationException

@Singleton
class CartRepositoryImpl @Inject constructor(
    private val cartDao: CartDao,
    private val cartApi: CartApi
) : CartRepository {

    override val cartItems: Flow<List<CartItem>> = cartDao.getAllCartItems().map { entities ->
        entities.map { it.toDomain() }
    }

    override suspend fun syncCart(): Result<Unit> {
        return try {
            val response = cartApi.getCart()
            if (response.isSuccessful && response.body() != null) {
                val cartResponse = response.body()!!
                val entities = cartResponse.items.map { itemDto ->
                    CartEntity(
                        foodId = itemDto.food.id.toString(),
                        restaurantId = itemDto.food.restaurantId.toString(),
                        quantity = itemDto.quantity,
                        unitPrice = itemDto.food.price,
                        restaurantName = itemDto.food.restaurantName ?: "Unknown",
                        foodName = itemDto.food.name,
                        foodPrice = itemDto.food.price,
                        foodImageUrl = itemDto.food.image,
                        foodImageRes = null,
                        categoryId = "", 
                        rating = 0f,
                        reviewCount = 0,
                        soldCount = 0,
                        promoTag = null,
                        foodSize = itemDto.food.size ?: "",
                        cartItemId = itemDto.id
                    )
                }
                cartDao.clearCart()
                entities.forEach { cartDao.insertCartItem(it) }
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to sync cart: ${response.code()}"))
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(e)
        }
    }

    override suspend fun addToCart(foodId: Int, quantity: Int, size: String?): Result<Unit> {
        return try {
            val request = AddToCartRequest(
                quantity = quantity,
                food = AddToCartFoodRequest(id = foodId, size = size)
            )
            val response = cartApi.addToCart(request)
            if (response.isSuccessful) {
                syncCart()
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to add to cart: ${response.code()}"))
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(e)
        }
    }

    override suspend fun updateQuantity(cartItemId: Int, quantity: Int): Result<Unit> {
        return try {
            val response = cartApi.updateCartItem(cartItemId, UpdateCartItemRequest(quantity))
            if (response.isSuccessful) {
                syncCart()
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to update cart: ${response.code()}"))
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(e)
        }
    }

    override suspend fun removeItem(cartItemId: Int): Result<Unit> {
        return try {
            val response = cartApi.deleteCartItem(cartItemId)
            if (response.isSuccessful) {
                syncCart()
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to remove item: ${response.code()}"))
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(e)
        }
    }

    override suspend fun clearCart(): Result<Unit> {
        return try {
            val response = cartApi.clearCart()
            if (response.isSuccessful) {
                cartDao.clearCart()
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to clear cart: ${response.code()}"))
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(e)
        }
    }
}
