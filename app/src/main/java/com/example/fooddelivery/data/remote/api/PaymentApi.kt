package com.example.fooddelivery.data.remote.api

import com.example.fooddelivery.data.remote.dto.BaseResponse
import com.example.fooddelivery.data.remote.dto.CheckPaymentRequest
import com.example.fooddelivery.data.remote.dto.PaymentStatusResponse
import retrofit2.Response
import retrofit2.http.*

interface PaymentApi {
    @GET("payment/{orderId}")
    suspend fun getPaymentDetail(
        @Path("orderId") orderId: Int
    ): Response<BaseResponse<PaymentStatusResponse>>

    @POST("payment/check-payment")
    suspend fun checkPayment(
        @Body request: CheckPaymentRequest
    ): Response<BaseResponse<PaymentStatusResponse>>

    @PATCH("payment/manage/{paymentId}/confirm")
    suspend fun confirmPayment(
        @Path("paymentId") paymentId: Int
    ): Response<BaseResponse<PaymentStatusResponse>>
}
