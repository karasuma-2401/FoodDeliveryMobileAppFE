package com.example.fooddelivery.data.remote.api

import com.example.fooddelivery.data.remote.dto.AddToCartRequest
import com.example.fooddelivery.data.remote.dto.CartResponse
import com.example.fooddelivery.data.remote.dto.UpdateCartItemRequest
import retrofit2.Response
import retrofit2.http.*

interface CartApi {
    @GET("cart")
    suspend fun getCart(): Response<CartResponse>

    @POST("cart")
    suspend fun addToCart(@Body request: AddToCartRequest): Response<Unit>

    @PATCH("cart/{cartItemId}")
    suspend fun updateCartItem(
        @Path("cartItemId") cartItemId: Int,
        @Body request: UpdateCartItemRequest
    ): Response<Unit>

    @DELETE("cart/{cartItemId}")
    suspend fun deleteCartItem(@Path("cartItemId") cartItemId: Int): Response<Unit>

    @DELETE("cart")
    suspend fun clearCart(): Response<Unit>
}
