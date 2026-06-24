package com.example.fooddelivery.ui.screens.restaurant.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fooddelivery.data.local.datastore.TokenManager
import com.example.fooddelivery.domain.model.BestSellerItem
import com.example.fooddelivery.domain.repository.RestaurantRepository
import com.example.fooddelivery.domain.repository.VoucherRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

data class RecentOrder(
    val id: String,
    val orderNumber: String,
    val customerName: String,
    val totalPrice: Double,
    val status: String,
    val time: String
)

data class VoucherPreviewItem(
    val code: String,
    val usage: String
)

data class DashboardState(
    val isLoading: Boolean = false,
    val restaurantName: String = "",
    val runningOrders: Int = 0,
    val orderRequest: Int = 0,
    val revenue: Double = 0.0,
    val rating: Double = 0.0,
    val totalReviews: Int = 0,
    val totalOrders: Int = 0,
    val activeVouchers: Int = 0,
    val voucherPreviews: List<VoucherPreviewItem> = emptyList(),
    val recentOrders: List<RecentOrder> = emptyList(),
    val bestSellers: List<BestSellerItem> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: RestaurantRepository,
    private val voucherRepository: VoucherRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state.asStateFlow()

    init {
        loadDashboard()
    }

    fun loadDashboard() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val restaurantId = tokenManager.getRestaurantId.first()
            if (restaurantId == null) {
                repository.getMyRestaurants()
                    .onSuccess { list ->
                        val firstRestaurant = list.firstOrNull()
                        if (firstRestaurant != null) {
                            tokenManager.saveRestaurantId(firstRestaurant.id)
                            loadDashboardForId(firstRestaurant.id, firstRestaurant.name)
                        } else {
                            _state.update {
                                it.copy(
                                    isLoading = false,
                                    error = "No restaurant found for this merchant"
                                )
                            }
                        }
                    }
                    .onFailure { error ->
                        _state.update {
                            it.copy(
                                isLoading = false,
                                error = error.message ?: "Failed to load restaurant"
                            )
                        }
                    }
            } else {
                val restaurantName = repository.getMyRestaurants()
                    .getOrNull()
                    ?.firstOrNull { it.id == restaurantId }
                    ?.name
                    .orEmpty()
                loadDashboardForId(restaurantId, restaurantName)
            }
        }
    }

    private suspend fun loadDashboardForId(restaurantId: Int, restaurantName: String) {
        coroutineScope {
            val dashboardDeferred = async { repository.generateDashboard(restaurantId) }
            val vouchersDeferred = async {
                voucherRepository.getVouchers(
                    limit = 2,
                    offset = 0,
                    restaurantId = restaurantId,
                    status = "APPLYING"
                )
            }

            dashboardDeferred.await()
                .onSuccess { dashboard ->
                    val voucherPreviews = vouchersDeferred.await().getOrNull()
                        ?.map { voucher ->
                            VoucherPreviewItem(
                                code = voucher.code,
                                usage = voucher.description?.takeIf { it.isNotBlank() }
                                    ?: if (voucher.type.equals("PERCENT", ignoreCase = true)) {
                                        "${voucher.sale.toInt()}% off"
                                    } else {
                                        "$${String.format(Locale.US, "%.2f", voucher.sale)} off"
                                    }
                            )
                        }
                        .orEmpty()

                    _state.update {
                        it.copy(
                            isLoading = false,
                            restaurantName = restaurantName,
                            runningOrders = dashboard.runningOrders,
                            orderRequest = dashboard.orderRequest,
                            revenue = dashboard.revenue,
                            rating = dashboard.rating,
                            totalReviews = dashboard.totalReviews,
                            totalOrders = dashboard.totalOrders,
                            activeVouchers = dashboard.activeVouchers,
                            voucherPreviews = voucherPreviews,
                            recentOrders = dashboard.recentOrders.map { order ->
                                RecentOrder(
                                    id = order.id,
                                    orderNumber = order.orderNumber,
                                    customerName = order.customerName,
                                    totalPrice = order.totalPrice,
                                    status = order.status,
                                    time = order.time
                                )
                            },
                            bestSellers = dashboard.bestSellers.map { item ->
                                BestSellerItem(
                                    name = item.name,
                                    price = "$${String.format(Locale.US, "%.2f", item.price)}",
                                    rating = item.rating.toFloat(),
                                    soldCount = item.soldCount,
                                    imageUrl = item.imageUrl
                                )
                            }
                        )
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "An unknown error occurred"
                        )
                    }
                }
        }
    }
}
