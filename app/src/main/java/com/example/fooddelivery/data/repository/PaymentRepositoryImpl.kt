package com.example.fooddelivery.data.repository

import com.example.fooddelivery.data.remote.api.PaymentApi
import com.example.fooddelivery.data.remote.dto.CheckPaymentRequest
import com.example.fooddelivery.data.remote.dto.PaymentStatusResponse
import com.example.fooddelivery.data.remote.unwrapData
import com.example.fooddelivery.domain.model.Payment
import com.example.fooddelivery.domain.repository.PaymentRepository
import javax.inject.Inject

class PaymentRepositoryImpl @Inject constructor(
    private val api: PaymentApi
) : PaymentRepository {
    override suspend fun getPaymentDetail(orderId: Int): Result<Payment> {
        return try {
            api.getPaymentDetail(orderId)
                .unwrapData("Failed to fetch payment details")
                .map { it.toDomain() }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun checkPayment(request: CheckPaymentRequest): Result<Payment> {
        return try {
            api.checkPayment(request)
                .unwrapData("Failed to check payment status")
                .map { it.toDomain() }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun confirmPayment(paymentId: Int): Result<Payment> {
        return try {
            api.confirmPayment(paymentId)
                .unwrapData("Failed to confirm payment")
                .map { it.toDomain() }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun PaymentStatusResponse.toDomain(): Payment {
        return Payment(
            id = id,
            orderId = orderId,
            amount = amount,
            method = method,
            paymentStatus = paymentStatus,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
}
