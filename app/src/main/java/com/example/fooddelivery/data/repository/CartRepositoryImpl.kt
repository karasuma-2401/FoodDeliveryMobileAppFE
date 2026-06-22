package com.example.fooddelivery.data.repository

import com.example.fooddelivery.data.local.room.dao.CartDao
import com.example.fooddelivery.data.local.room.entity.CartEntity
import com.example.fooddelivery.data.local.room.entity.toDomain
import com.example.fooddelivery.data.remote.api.CartApi
import com.example.fooddelivery.data.remote.dto.AddToCartRequest
import com.example.fooddelivery.data.remote.dto.CartResponse
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
                updateLocalCart(response.body()!!)
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to sync cart: ${response.code()}"))
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(e)
        }
    }

    private suspend fun updateLocalCart(cartResponse: CartResponse) {
        val entities = cartResponse.items.map { itemDto ->
            CartEntity(
                foodId = itemDto.food.id.toString(),
                restaurantId = itemDto.food.restaurantId.toString(),
                quantity = itemDto.quantity,
                unitPrice = itemDto.food.price,
                restaurantName = itemDto.food.restaurant?.name ?: "Unknown",
                foodName = itemDto.food.name,
                foodPrice = itemDto.food.price,
                foodImageUrl = itemDto.food.image,
                foodImageRes = null,
                categoryId = itemDto.food.category?.id?.toString() ?: "", 
                rating = 0f,
                reviewCount = 0,
                soldCount = 0,
                promoTag = itemDto.food.label,
                foodSize = itemDto.sizeName ?: "",
                cartItemId = itemDto.id,
                note = itemDto.fullText,
                foodSizeId = itemDto.foodSizeId?.toString()
            )
        }
        cartDao.clearCart()
        entities.forEach { cartDao.insertCartItem(it) }
    }

    override suspend fun addToCart(foodId: Int, quantity: Int, size: String?, note: String?): Result<Unit> {
        return try {
            val foodSizeId = size?.toIntOrNull()
            
            val request = AddToCartRequest(
                foodId = foodId,
                quantity = quantity,
                foodSizeId = foodSizeId,
                fullText = note
            )
            val response = cartApi.addToCart(request)
            if (response.isSuccessful && response.body() != null) {
                updateLocalCart(response.body()!!)
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
            if (response.isSuccessful && response.body() != null) {
                updateLocalCart(response.body()!!)
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
            if (response.isSuccessful && response.body() != null) {
                updateLocalCart(response.body()!!)
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
