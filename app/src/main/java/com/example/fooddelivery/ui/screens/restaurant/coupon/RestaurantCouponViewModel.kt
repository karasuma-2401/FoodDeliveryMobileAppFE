package com.example.fooddelivery.ui.screens.restaurant.coupon

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fooddelivery.data.local.datastore.TokenManager
import com.example.fooddelivery.data.remote.dto.VoucherDto
import com.example.fooddelivery.domain.model.Voucher
import com.example.fooddelivery.domain.model.VoucherType
import com.example.fooddelivery.domain.repository.VoucherRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

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
    val systemTotalCount: Int = 0,
    val systemPage: Int = 1,
    val isLoading: Boolean = false,
    val isSystemLoading: Boolean = false,
    val isSystemPaginating: Boolean = false,
    val isSystemRefreshing: Boolean = false,
    val isSystemEndReached: Boolean = false,
    val systemErrorMessage: String? = null,
)

sealed interface RestaurantCouponEvent {
    data object LoadSystemVouchers : RestaurantCouponEvent
    data object LoadMoreSystemVouchers : RestaurantCouponEvent
    data object RefreshSystemVouchers : RestaurantCouponEvent
}

@HiltViewModel
class RestaurantCouponViewModel @Inject constructor(
    private val voucherRepository: VoucherRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(RestaurantCouponUiState())
    val uiState: StateFlow<RestaurantCouponUiState> = _uiState.asStateFlow()

    private val pageSize = 20
    private var searchJob: Job? = null

    init {
        loadRestaurantVouchers()
    }

    private fun VoucherDto.toDomainVoucher(): Voucher {
        val discountType = when (type.uppercase()) {
            "PERCENT" -> VoucherType.PERCENT
            else -> VoucherType.MONEY
        }
        return Voucher(
            id = id,
            code = code,
            title = name,
            description = description ?: "",
            discountAmount = sale,
            minOrderAmount = minimumOrderAmount,
            expiryText = endAt,
            type = discountType,
            isApplicable = status.uppercase() == "APPLYING",
            conditionMessage = null
        )
    }

    private fun VoucherDto.toRestaurantItem(): RestaurantVoucherItem {
        val active = status.uppercase() == "APPLYING"
        val desc = buildString {
            append(description?.takeIf { it.isNotBlank() } ?: name)
            append(" • Min. $")
            append(String.format(Locale.US, "%.2f", minimumOrderAmount))
            if (type.uppercase() == "PERCENT") {
                append(" • ")
                append(String.format(Locale.US, "%.0f", sale))
                append("%")
            } else {
                append(" • $")
                append(String.format(Locale.US, "%.2f", sale))
            }
        }
        return RestaurantVoucherItem(
            id = id.toString(),
            code = code,
            description = desc,
            usageText = "—",
            isActive = active
        )
    }

    fun onEvent(event: RestaurantCouponEvent) {
        when (event) {
            RestaurantCouponEvent.LoadSystemVouchers -> loadSystemVouchersInitial()
            RestaurantCouponEvent.LoadMoreSystemVouchers -> loadMoreSystemVouchers()
            RestaurantCouponEvent.RefreshSystemVouchers -> loadSystemVouchersInitial(isRefresh = true)
        }
    }

    private fun loadRestaurantVouchers() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val restaurantId = tokenManager.getRestaurantId.first()
            val restaurantResult = voucherRepository.getVouchers(
                limit = pageSize,
                offset = 0,
                restaurantId = restaurantId,
                code = null,
                status = null
            )
            val restaurantVouchers = restaurantResult.getOrDefault(emptyList()).map { it.toRestaurantItem() }
            _uiState.update { state ->
                state.copy(
                    restaurantVouchers = restaurantVouchers,
                    isLoading = false,
                )
            }
        }
    }

    private fun loadSystemVouchersInitial(isRefresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSystemLoading = !isRefresh,
                    isSystemRefreshing = isRefresh,
                    systemPage = 1,
                    isSystemEndReached = false,
                    systemErrorMessage = null,
                )
            }
            val result = voucherRepository.getVouchersPage(
                limit = pageSize,
                offset = 0,
                restaurantId = null,
                code = _uiState.value.searchQuery.takeIf { it.isNotBlank() },
                status = null,
            )
            result.onSuccess { page ->
                val vouchers = page.items.map { it.toDomainVoucher() }
                _uiState.update { state ->
                    state.copy(
                        systemVouchers = vouchers,
                        systemTotalCount = page.total,
                        systemPage = 1,
                        isSystemLoading = false,
                        isSystemRefreshing = false,
                        isSystemEndReached = vouchers.size >= page.total || vouchers.size < pageSize,
                        systemErrorMessage = null,
                    )
                }
            }.onFailure { error ->
                _uiState.update { state ->
                    state.copy(
                        isSystemLoading = false,
                        isSystemRefreshing = false,
                        systemErrorMessage = error.message ?: "Failed to load system coupons",
                    )
                }
            }
        }
    }

    private fun loadMoreSystemVouchers() {
        val currentState = _uiState.value
        if (currentState.isSystemPaginating || currentState.isSystemEndReached || currentState.isSystemLoading) {
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSystemPaginating = true) }
            val nextPage = currentState.systemPage + 1
            val offset = (nextPage - 1) * pageSize
            val result = voucherRepository.getVouchersPage(
                limit = pageSize,
                offset = offset,
                restaurantId = null,
                code = currentState.searchQuery.takeIf { it.isNotBlank() },
                status = null,
            )
            result.onSuccess { page ->
                val newVouchers = page.items.map { it.toDomainVoucher() }
                _uiState.update { state ->
                    val combined = state.systemVouchers + newVouchers
                    state.copy(
                        systemVouchers = combined,
                        systemTotalCount = page.total,
                        systemPage = nextPage,
                        isSystemPaginating = false,
                        isSystemEndReached = combined.size >= page.total || newVouchers.size < pageSize,
                    )
                }
            }.onFailure { error ->
                _uiState.update { state ->
                    state.copy(
                        isSystemPaginating = false,
                        systemErrorMessage = error.message ?: "Failed to load more coupons",
                    )
                }
            }
        }
    }

    fun onTabSelected(tabIndex: Int) {
        _uiState.update { it.copy(selectedTab = tabIndex) }
        when (tabIndex) {
            0 -> loadRestaurantVouchers()
            1 -> loadSystemVouchersInitial()
        }
    }

    fun onSearchQueryChanged(newQuery: String) {
        _uiState.update { it.copy(searchQuery = newQuery) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300)
            if (_uiState.value.selectedTab == 1) {
                loadSystemVouchersInitial()
            }
        }
    }

    fun toggleRestaurantCoupon(voucherId: String) {
        val current = _uiState.value.restaurantVouchers.firstOrNull { it.id == voucherId } ?: return
        val targetStatus = if (current.isActive) "ENDED" else "APPLYING"
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            voucherRepository.updateVoucherStatus(
                id = voucherId.toIntOrNull() ?: return@launch,
                status = targetStatus
            )
            loadRestaurantVouchers()
        }
    }
}
