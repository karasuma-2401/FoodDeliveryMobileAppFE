package com.example.fooddelivery.data.repository

import com.example.fooddelivery.data.remote.api.OrderApi
import com.example.fooddelivery.data.remote.dto.OrderRequest
import com.example.fooddelivery.data.remote.dto.OrderResponse
import com.example.fooddelivery.domain.model.OrderStatus
import com.example.fooddelivery.domain.repository.OrderRepository
import javax.inject.Inject

class OrderRepositoryImpl @Inject constructor(
    private val api: OrderApi
) : OrderRepository {
    override suspend fun createOrder(request: OrderRequest): Result<OrderResponse> {
        return try {
            val response = api.createOrder(request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to create order"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun checkOrderStatus(orderId: String): Result<OrderStatus> {
        return try {
            val response = api.getOrderStatus(orderId)
            if (response.isSuccessful && response.body() != null) {
                val status = when (response.body()!!.status.uppercase()) {
                    "COMPLETED" -> OrderStatus.COMPLETED
                    "CANCELED" -> OrderStatus.CANCELED
                    else -> OrderStatus.ONGOING
                }
                Result.success(status)
            } else {
                Result.failure(Exception("Failed to check order status"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
