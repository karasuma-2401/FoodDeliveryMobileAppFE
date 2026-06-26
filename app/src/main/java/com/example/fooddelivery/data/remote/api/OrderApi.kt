package com.example.fooddelivery.data.remote.api

import com.example.fooddelivery.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*
import kotlinx.serialization.json.JsonElement

interface OrderApi {
    @GET("orders/deliveryFee/{restaurantId}")
    suspend fun getDeliveryFee(
        @Path("restaurantId") restaurantId: Int,
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double
    ): Response<BaseResponse<DeliveryFeeResponse>>

    @POST("orders")
    suspend fun createOrder(@Body request: OrderRequest): Response<BaseResponse<OrderResponse>>

    @GET("orders/{orderId}/status")
    suspend fun getOrderStatus(@Path("orderId") orderId: Int): Response<BaseResponse<OrderStatusSummaryResponse>>

    @POST("orders/{orderId}/reorder")
    suspend fun reorder(@Path("orderId") orderId: String): Response<BaseResponse<ReorderResponse>>

    @GET("orders")
    suspend fun getOngoingOrders(
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0,
        @Query("status") status: String = "ongoing"
    ): Response<BaseResponse<OngoingOrdersResponse>>

    @GET("orders")
    suspend fun getHistoryOrders(
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0,
        @Query("status") status: String = "history"
    ): Response<BaseResponse<HistoryOrdersResponse>>

    @GET("orders/{orderId}")
    suspend fun getOrderDetail(@Path("orderId") orderId: Int): Response<BaseResponse<OrderDetailResponse>>

    @DELETE("orders/{orderId}")
    suspend fun cancelOrder(@Path("orderId") orderId: Int): Response<BaseResponse<MessageResponse>>

    @POST("orders/{orderId}/cancel")
    suspend fun cancelOrderPost(@Path("orderId") orderId: Int): Response<BaseResponse<CancelOrderResponse>>

    @PATCH("orders/{orderId}")
    suspend fun updateOrderStatus(
        @Path("orderId") orderId: Int,
        @Body request: UpdateOrderStatusRequest
    ): Response<BaseResponse<JsonElement>>

    @POST("orders/{orderId}/confirm-received")
    suspend fun confirmReceived(@Path("orderId") orderId: Int): Response<BaseResponse<MessageResponse>>
}
