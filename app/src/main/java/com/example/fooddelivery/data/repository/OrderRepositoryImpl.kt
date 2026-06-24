package com.example.fooddelivery.data.repository

import com.example.fooddelivery.data.remote.api.OrderApi
import com.example.fooddelivery.data.remote.parseErrorMessage
import com.example.fooddelivery.data.remote.dto.*
import com.example.fooddelivery.domain.model.*
import com.example.fooddelivery.domain.repository.OrderRepository
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
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
                Result.failure(Exception(response.parseErrorMessage("Failed to create order")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun checkOrderStatus(orderId: String): Result<OrderStatusSummary> {
        return try {
            val id = orderId.toIntOrNull() ?: return Result.failure(Exception("Invalid order ID"))
            val response = api.getOrderStatus(id)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.toDomain())
            } else {
                Result.failure(Exception(response.parseErrorMessage("Failed to check order status")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun reorder(orderId: String): Result<String> {
        return try {
            val response = api.reorder(orderId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.message)
            } else {
                Result.failure(Exception(response.parseErrorMessage("Failed to reorder")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getOrders(status: String?, limit: Int, offset: Int): Result<List<Order>> {
        return try {
            when (status) {
                "ongoing" -> {
                    val response = api.getOngoingOrders(limit, offset)
                    if (response.isSuccessful && response.body() != null) {
                        Result.success(response.body()!!.ongoing_orders.map { it.toOrder() })
                    } else {
                        Result.failure(Exception(response.parseErrorMessage("Failed to fetch ongoing orders")))
                    }
                }
                "history" -> {
                    val response = api.getHistoryOrders(limit, offset)
                    if (response.isSuccessful && response.body() != null) {
                        Result.success(response.body()!!.history_orders.map { it.toOrder() })
                    } else {
                        Result.failure(Exception(response.parseErrorMessage("Failed to fetch history orders")))
                    }
                }
                else -> {
                    Result.failure(Exception("Status parameter is required for this API version (ongoing/history)"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getOrderDetail(orderId: Int): Result<OrderDetail> {
        return try {
            val response = api.getOrderDetail(orderId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.toOrderDetail())
            } else {
                Result.failure(Exception(response.parseErrorMessage("Failed to fetch order detail")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun cancelOrder(orderId: Int): Result<String> {
        return try {
            val response = api.cancelOrder(orderId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.message)
            } else {
                Result.failure(Exception(response.parseErrorMessage("Failed to cancel order")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun cancelOrderPost(orderId: Int): Result<String> {
        return try {
            val response = api.cancelOrderPost(orderId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.message)
            } else {
                Result.failure(Exception(response.parseErrorMessage("Failed to cancel order")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateOrderStatus(orderId: Int, status: String): Result<String> {
        return try {
            val response = api.updateOrderStatus(orderId, UpdateOrderStatusRequest(status))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.message)
            } else {
                Result.failure(Exception(response.parseErrorMessage("Failed to update order status")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun OrderListDto.toOrder(): Order {
        return Order(
            id = id.toString(),
            restaurantId = restaurant?.id?.toString() ?: "",
            restaurantName = restaurant?.name ?: "Unknown",
            restaurantImage = restaurant?.image ?: "",
            price = totalPrice,
            itemCount = item_count,
            type = if (type.uppercase() == "DRINK") OrderType.DRINK else OrderType.FOOD,
            status = when (status.uppercase()) {
                "DELIVERED", "COMPLETED" -> OrderStatus.COMPLETED
                "CANCELED", "CANCELLED" -> OrderStatus.CANCELED
                else -> OrderStatus.ONGOING
            },
            date = formatDate(date)
        )
    }

    private fun OrderDetailResponse.toOrderDetail(): OrderDetail {
        return OrderDetail(
            id = id.toString(),
            totalPrice = totalPrice,
            status = status,
            statusStep = status_step ?: 0,
            backendStatus = backend_status ?: "",
            expectedArrival = expected_arrival?.let { formatDate(it) },
            restaurantId = restaurant?.id ?: 0,
            restaurantName = restaurant?.name ?: "",
            restaurantImage = restaurant?.image ?: "",
            restaurantPhone = restaurant?.phone,
            items = orderFoods.map { foodBrief ->
                OrderItemDetail(
                    id = foodBrief.id,
                    foodId = foodBrief.food?.id ?: 0,
                    name = foodBrief.food?.name ?: "Unknown",
                    image = foodBrief.food?.image ?: "",
                    quantity = foodBrief.quantity,
                    price = foodBrief.price,
                    size = foodBrief.sizeName,
                    note = foodBrief.fullText,
                    description = foodBrief.food?.description
                )
            },
            address = OrderAddress(
                id = address?.id ?: 0,
                title = address?.title ?: "",
                fullText = address?.fullText ?: "",
                latitude = address?.latitude ?: 0.0,
                longitude = address?.longitude ?: 0.0
            ),
            note = note,
            paymentMethod = payment?.method ?: "",
            paymentStatus = payment?.paymentStatus ?: "",
            paymentDate = payment?.createdAt?.let { formatDate(it) },
            customerName = user?.name ?: "",
            customerPhone = user?.phone,
            customerEmail = user?.email,
            conversationId = conversation?.id,
            voucherInfo = voucher?.let {
                VoucherSummary(
                    id = it.id,
                    name = it.name,
                    sale = it.sale ?: 0.0,
                    type = it.type ?: "MONEY"
                )
            }
        )
    }

    private fun OrderStatusSummaryResponse.toDomain(): OrderStatusSummary {
        return OrderStatusSummary(
            orderId = order_id,
            status = status,
            statusStep = status_step,
            updatedAt = formatDate(updated_at),
            backendStatus = backend_status
        )
    }

    private fun formatDate(dateStr: String): String {
        return try {
            // Check if it's ISO format or other
            val inputFormat = if (dateStr.contains("T")) {
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }
            } else {
                SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            }
            
            val date = inputFormat.parse(dateStr)
            val outputFormat = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
            outputFormat.format(date!!)
        } catch (e: Exception) {
            dateStr
        }
    }
}
