package com.example.fooddelivery.domain.repository

import com.example.fooddelivery.data.remote.dto.OrderRequest
import com.example.fooddelivery.data.remote.dto.OrderResponse
import com.example.fooddelivery.domain.model.Order
import com.example.fooddelivery.domain.model.OrderDetail
import com.example.fooddelivery.domain.model.OrderStatusSummary

interface OrderRepository {
    suspend fun createOrder(request: OrderRequest): Result<OrderResponse>
    suspend fun checkOrderStatus(orderId: String): Result<OrderStatusSummary>
    suspend fun reorder(orderId: String): Result<String>
    suspend fun getOrders(status: String? = null, limit: Int = 20, offset: Int = 0): Result<List<Order>>
    suspend fun getOrderDetail(orderId: Int): Result<OrderDetail>
    suspend fun cancelOrder(orderId: Int): Result<String>
    suspend fun cancelOrderPost(orderId: Int): Result<String>
    suspend fun updateOrderStatus(orderId: Int, status: String): Result<String>
    suspend fun confirmReceived(orderId: Int): Result<String>
}
