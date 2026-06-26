package com.example.fooddelivery.data.repository

import com.example.fooddelivery.data.remote.api.OrderApi
import com.example.fooddelivery.data.remote.dto.*
import com.example.fooddelivery.data.remote.unwrapData
import com.example.fooddelivery.domain.model.*
import com.example.fooddelivery.domain.repository.CartRepository
import com.example.fooddelivery.domain.repository.OrderRepository
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

class OrderRepositoryImpl @Inject constructor(
    private val api: OrderApi,
    private val cartRepository: CartRepository,
) : OrderRepository {
    override suspend fun getDeliveryFee(
        restaurantId: Int,
        latitude: Double,
        longitude: Double
    ): Result<Double> {
        return try {
            api.getDeliveryFee(restaurantId, latitude, longitude)
                .unwrapData("Failed to calculate delivery fee")
                .map { it.deliveryFee }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createOrder(request: OrderRequest): Result<OrderResponse> {
        return try {
            api.createOrder(request).unwrapData("Failed to create order")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun checkOrderStatus(orderId: String): Result<OrderStatusSummary> {
        return try {
            val id = orderId.toIntOrNull() ?: return Result.failure(Exception("Invalid order ID"))
            api.getOrderStatus(id)
                .unwrapData("Failed to check order status")
                .map { it.toDomain() }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun reorder(orderId: String): Result<ReorderResult> {
        return try {
            api.reorder(orderId)
                .unwrapData("Failed to reorder")
                .mapCatching { response ->
                    cartRepository.applyServerCart(response.cart).getOrThrow()
                    ReorderResult(
                        message = response.message ?: "Items added to cart",
                        addedCount = response.addedCount,
                        skippedItems = response.skippedItems.map {
                            SkippedReorderItem(foodId = it.foodId, reason = it.reason)
                        },
                    )
                }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getOrders(status: String?, limit: Int, offset: Int): Result<List<Order>> {
        return try {
            when (status) {
                "ongoing" -> {
                    api.getOngoingOrders(limit, offset)
                        .unwrapData("Failed to fetch ongoing orders")
                        .map { it.ongoing_orders.map { order -> order.toOrder() } }
                }
                "history" -> {
                    api.getHistoryOrders(limit, offset)
                        .unwrapData("Failed to fetch history orders")
                        .map { it.history_orders.map { order -> order.toOrder() } }
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
            api.getOrderDetail(orderId)
                .unwrapData("Failed to fetch order detail")
                .map { it.toOrderDetail() }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun cancelOrder(orderId: Int): Result<String> {
        return try {
            api.cancelOrder(orderId)
                .unwrapData("Failed to cancel order")
                .map { it.message }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun cancelOrderPost(orderId: Int): Result<String> {
        return try {
            api.cancelOrderPost(orderId)
                .unwrapData("Failed to cancel order")
                .map { it.message }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateOrderStatus(orderId: Int, status: String): Result<String> {
        return try {
            api.updateOrderStatus(orderId, UpdateOrderStatusRequest(status))
                .unwrapData("Failed to update order status")
                .map { "Success" }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun confirmReceived(orderId: Int): Result<String> {
        return try {
            api.confirmReceived(orderId)
                .unwrapData("Failed to confirm receipt")
                .map { it.message }
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
                "CONFIRMED", "COMPLETED" -> OrderStatus.COMPLETED
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
            deliveredAt = delivered_at?.let { formatDate(it) },
            autoConfirmAt = auto_confirm_at?.let { formatDate(it) },
            hoursUntilAutoConfirm = hours_until_auto_confirm,
            confirmedAt = confirmed_at?.let { formatDate(it) },
            confirmedBy = confirmed_by,
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
                    lineTotal = foodBrief.price,
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
            backendStatus = backend_status,
            deliveredAt = delivered_at?.let { formatDate(it) },
            autoConfirmAt = auto_confirm_at?.let { formatDate(it) },
            hoursUntilAutoConfirm = hours_until_auto_confirm,
            confirmedAt = confirmed_at?.let { formatDate(it) },
            confirmedBy = confirmed_by
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
