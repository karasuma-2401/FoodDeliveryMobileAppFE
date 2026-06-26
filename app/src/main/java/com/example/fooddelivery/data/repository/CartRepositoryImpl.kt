package com.example.fooddelivery.data.repository

import com.example.fooddelivery.data.local.room.dao.CartDao
import com.example.fooddelivery.data.local.room.entity.CartEntity
import com.example.fooddelivery.data.local.room.entity.toDomain
import com.example.fooddelivery.data.remote.api.CartApi
import com.example.fooddelivery.data.remote.dto.AddToCartRequest
import com.example.fooddelivery.data.remote.dto.CartItemResponse
import com.example.fooddelivery.data.remote.dto.CartResponse
import com.example.fooddelivery.data.remote.dto.UpdateCartItemRequest
import com.example.fooddelivery.data.remote.dto.toRestaurantGroups
import com.example.fooddelivery.data.remote.unwrapData
import com.example.fooddelivery.data.remote.unwrapUnit
import com.example.fooddelivery.domain.model.CartRestaurantGroup
import com.example.fooddelivery.domain.repository.CartRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.cancellation.CancellationException

@Singleton
class CartRepositoryImpl @Inject constructor(
    private val cartDao: CartDao,
    private val cartApi: CartApi
) : CartRepository {

    private val _restaurantGroups = MutableStateFlow<List<CartRestaurantGroup>>(emptyList())
    override val restaurantGroups: Flow<List<CartRestaurantGroup>> = _restaurantGroups.asStateFlow()

    override val cartItems: Flow<List<com.example.fooddelivery.domain.model.CartItem>> =
        cartDao.getAllCartItems().map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun syncCart(): Result<Unit> {
        return try {
            cartApi.getCart()
                .unwrapData("Failed to sync cart")
                .mapCatching { applyServerCart(it).getOrThrow() }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(e)
        }
    }

    override suspend fun applyServerCart(cartResponse: CartResponse): Result<Unit> {
        return try {
            updateLocalCart(cartResponse)
            Result.success(Unit)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(e)
        }
    }

    private suspend fun updateLocalCart(cartResponse: CartResponse) {
        val entities = cartResponse.items.map { it.toEntity() }
        cartDao.clearCart()
        entities.forEach { cartDao.insertCartItem(it) }
        _restaurantGroups.value = cartResponse.toRestaurantGroups()
    }

    private fun CartItemResponse.toEntity(): CartEntity {
        val perUnitPrice = if (quantity > 0) lineTotal / quantity else food.price
        return CartEntity(
            foodId = food.id.toString(),
            restaurantId = food.restaurantId.toString(),
            quantity = quantity,
            lineTotal = lineTotal,
            unitPrice = perUnitPrice,
            restaurantName = food.restaurant?.name ?: "Unknown",
            foodName = food.name,
            foodPrice = food.price,
            foodImageUrl = food.image,
            foodImageRes = null,
            categoryId = food.category?.id?.toString() ?: "",
            rating = 0f,
            reviewCount = 0,
            soldCount = 0,
            promoTag = food.label,
            foodSize = sizeName ?: "",
            cartItemId = id,
            note = fullText,
            foodSizeId = foodSizeId?.toString()
        )
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
            cartApi.addToCart(request)
                .unwrapData("Failed to add to cart")
                .mapCatching { applyServerCart(it).getOrThrow() }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(e)
        }
    }

    override suspend fun updateQuantity(cartItemId: Int, quantity: Int): Result<Unit> {
        return try {
            cartApi.updateCartItem(cartItemId, UpdateCartItemRequest(quantity))
                .unwrapData("Failed to update cart")
                .mapCatching { applyServerCart(it).getOrThrow() }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(e)
        }
    }

    override suspend fun removeItem(cartItemId: Int): Result<Unit> {
        return try {
            cartApi.deleteCartItem(cartItemId)
                .unwrapData("Failed to remove item")
                .mapCatching { applyServerCart(it).getOrThrow() }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(e)
        }
    }

    override suspend fun clearCartByRestaurant(restaurantId: Int): Result<Unit> {
        return try {
            cartApi.clearCartByRestaurant(restaurantId)
                .unwrapData("Failed to clear restaurant cart")
                .mapCatching { applyServerCart(it).getOrThrow() }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(e)
        }
    }

    override suspend fun clearCart(): Result<Unit> {
        return try {
            cartApi.clearCart()
                .unwrapUnit("Failed to clear cart")
                .mapCatching {
                    cartDao.clearCart()
                    _restaurantGroups.value = emptyList()
                }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(e)
        }
    }
}
