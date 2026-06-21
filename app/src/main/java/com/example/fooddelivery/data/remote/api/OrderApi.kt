package com.example.fooddelivery.data.remote.api

import com.example.fooddelivery.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface OrderApi {
    @POST("orders")
    suspend fun createOrder(@Body request: OrderRequest): Response<OrderResponse>

    @GET("orders/{orderId}/status")
    suspend fun getOrderStatus(@Path("orderId") orderId: Int): Response<OrderStatusSummaryResponse>

    @POST("orders/{orderId}/reorder")
    suspend fun reorder(@Path("orderId") orderId: String): Response<MessageResponse>

    @GET("orders")
    suspend fun getOngoingOrders(
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0,
        @Query("status") status: String = "ongoing"
    ): Response<OngoingOrdersResponse>

    @GET("orders")
    suspend fun getHistoryOrders(
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0,
        @Query("status") status: String = "history"
    ): Response<HistoryOrdersResponse>

    @GET("orders/{orderId}")
    suspend fun getOrderDetail(@Path("orderId") orderId: Int): Response<OrderDetailResponse>

    @DELETE("orders/{orderId}")
    suspend fun cancelOrder(@Path("orderId") orderId: Int): Response<MessageResponse>

    @POST("orders/{orderId}/cancel")
    suspend fun cancelOrderPost(@Path("orderId") orderId: Int): Response<CancelOrderResponse>

    @PATCH("orders/{orderId}")
    suspend fun updateOrderStatus(
        @Path("orderId") orderId: Int,
        @Body request: UpdateOrderStatusRequest
    ): Response<MessageResponse>
}
