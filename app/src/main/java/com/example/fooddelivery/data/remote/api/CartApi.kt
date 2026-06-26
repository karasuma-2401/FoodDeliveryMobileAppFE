package com.example.fooddelivery.data.remote.api

import com.example.fooddelivery.data.remote.dto.AddToCartRequest
import com.example.fooddelivery.data.remote.dto.BaseResponse
import com.example.fooddelivery.data.remote.dto.CartResponse
import com.example.fooddelivery.data.remote.dto.UpdateCartItemRequest
import retrofit2.Response
import retrofit2.http.*

interface CartApi {
    @GET("cart")
    suspend fun getCart(): Response<BaseResponse<CartResponse>>

    @POST("cart")
    suspend fun addToCart(@Body request: AddToCartRequest): Response<BaseResponse<CartResponse>>

    @PATCH("cart/{cartItemId}")
    suspend fun updateCartItem(
        @Path("cartItemId") cartItemId: Int,
        @Body request: UpdateCartItemRequest
    ): Response<BaseResponse<CartResponse>>

    @DELETE("cart/{cartItemId}")
    suspend fun deleteCartItem(@Path("cartItemId") cartItemId: Int): Response<BaseResponse<CartResponse>>

    @DELETE("cart/restaurant/{restaurantId}")
    suspend fun clearCartByRestaurant(
        @Path("restaurantId") restaurantId: Int
    ): Response<BaseResponse<CartResponse>>

    @DELETE("cart")
    suspend fun clearCart(): Response<BaseResponse<Unit>>
}
