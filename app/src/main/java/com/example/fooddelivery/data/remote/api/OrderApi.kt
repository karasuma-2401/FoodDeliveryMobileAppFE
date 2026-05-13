package com.example.fooddelivery.data.remote.api

import com.example.fooddelivery.data.remote.dto.OrderRequest
import com.example.fooddelivery.data.remote.dto.OrderResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface OrderApi {
    @POST("orders")
    suspend fun createOrder(@Body request: OrderRequest): Response<OrderResponse>

    @GET("orders/{id}/status")
    suspend fun getOrderStatus(@Path("id") id: String): Response<OrderResponse>
}
