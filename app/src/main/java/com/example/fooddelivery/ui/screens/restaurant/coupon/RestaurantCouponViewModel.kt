package com.example.fooddelivery.ui.screens.restaurant.coupon

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fooddelivery.domain.model.Voucher
import com.example.fooddelivery.domain.model.VoucherType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RestaurantVoucherItem(
    val id: String,
    val code: String,
    val description: String,
    val usageText: String,
    val isActive: Boolean
)

data class RestaurantCouponUiState(
    val selectedTab: Int = 0,
    val searchQuery: String = "",
    val restaurantVouchers: List<RestaurantVoucherItem> = emptyList(),
    val systemVouchers: List<Voucher> = emptyList(),
    val currentPage: Int = 1,
    val totalItems: Int = 0,
    val isLoading: Boolean = false
)
class RestaurantCouponViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(RestaurantCouponUiState())
    val uiState: StateFlow<RestaurantCouponUiState> = _uiState.asStateFlow()

    private var allSystemVouchers = listOf(
        Voucher(
            id = "1", code = "SUMMER25", title = "25% Weekend Discount",
            description = "Get 25% off on all main dishes", discountAmount = 25.0,
            minOrderAmount = 30.0, expiryText = "31 Dec 2026",
            type = VoucherType.DISCOUNT, isApplicable = true
        ),
        Voucher(
            id = "2", code = "FREESHIP", title = "Free Delivery Code",
            description = "Free shipping across town", discountAmount = 5.0,
            minOrderAmount = 15.0, type = VoucherType.FREESHIP, isApplicable = true
        )
    )

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val mockRestaurantCoupons = listOf(
                RestaurantVoucherItem("r1", "SUMMER25", "25% off • Min. $30 order", "Used 45 / 100", true),
                RestaurantVoucherItem("r2", "FREESHIP", "Free Shipping • Min. $15 order", "Used 12 / 50", true),
                RestaurantVoucherItem("r3", "WINTER50", "50% off • Min. $100 order", "Used 100 / 100", false)
            )

            _uiState.update {
                it.copy(
                    restaurantVouchers = mockRestaurantCoupons,
                    systemVouchers = allSystemVouchers,
                    totalItems = allSystemVouchers.size,
                    isLoading = false
                )
            }
        }
    }

    fun onTabSelected(tabIndex: Int) {
        _uiState.update { it.copy(selectedTab = tabIndex) }
    }

    fun onSearchQueryChanged(newQuery: String) {
        _uiState.update { it.copy(searchQuery = newQuery) }
        filterSystemVouchers(newQuery)
    }

    private fun filterSystemVouchers(query: String) {
        val filtered = if (query.isBlank()) {
            allSystemVouchers
        } else {
            allSystemVouchers.filter {
                it.code.contains(query, ignoreCase = true) ||
                        it.title.contains(query, ignoreCase = true)
            }
        }
        _uiState.update { it.copy(systemVouchers = filtered) }
    }

    fun toggleRestaurantCoupon(voucherId: String) {
        _uiState.update { currentState ->
            val updatedList = currentState.restaurantVouchers.map { item ->
                if (item.id == voucherId) item.copy(isActive = !item.isActive) else item
            }
            currentState.copy(restaurantVouchers = updatedList)
        }
    }

    fun onPageChanged(page: Int) {
        _uiState.update { it.copy(currentPage = page) }
    }
}