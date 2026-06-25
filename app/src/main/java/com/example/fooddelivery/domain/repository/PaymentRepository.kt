package com.example.fooddelivery.domain.repository

import com.example.fooddelivery.data.remote.dto.CheckPaymentRequest
import com.example.fooddelivery.domain.model.Payment

interface PaymentRepository {
    suspend fun getPaymentDetail(orderId: Int): Result<Payment>
    suspend fun checkPayment(request: CheckPaymentRequest): Result<Payment>
    suspend fun confirmPayment(paymentId: Int): Result<Payment>
}
