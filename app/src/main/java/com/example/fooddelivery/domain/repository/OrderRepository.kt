package com.example.fooddelivery.domain.repository

import com.example.fooddelivery.data.remote.dto.OrderRequest
import com.example.fooddelivery.data.remote.dto.OrderResponse
import com.example.fooddelivery.domain.model.OrderStatus

interface OrderRepository {
    suspend fun createOrder(request: OrderRequest): Result<OrderResponse>
    suspend fun checkOrderStatus(orderId: String): Result<OrderStatus>
}
