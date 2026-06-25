package com.example.fooddelivery.ui.screens.restaurant.coupon

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fooddelivery.data.local.datastore.TokenManager
import com.example.fooddelivery.data.remote.dto.VoucherDto
import com.example.fooddelivery.domain.model.Voucher
import com.example.fooddelivery.domain.model.VoucherType
import com.example.fooddelivery.domain.repository.VoucherRepository
import dagger.hilt.android.lifecycle.HiltViewModel
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
    val currentPage: Int = 1,
    val totalItems: Int = 0,
    val isLoading: Boolean = false
)

@HiltViewModel
class RestaurantCouponViewModel @Inject constructor(
    private val voucherRepository: VoucherRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(RestaurantCouponUiState())
    val uiState: StateFlow<RestaurantCouponUiState> = _uiState.asStateFlow()

    private val pageSize = 20

    init {
        refresh()
    }

    private fun VoucherDto.toDomainVoucher(): Voucher {
        val discountType = when (type.uppercase()) {
            "MONEY" -> VoucherType.MONEY
            "PERCENT" -> VoucherType.PERCENT
            else -> VoucherType.FREE_SHIPPING
        }
        val discountAmount = sale
        return Voucher(
            id = id,
            code = code,
            title = name,
            description = description ?: "",
            discountAmount = discountAmount,
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

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val page = _uiState.value.currentPage.coerceAtLeast(1)
            val offset = (page - 1) * pageSize

            val restaurantId = tokenManager.getRestaurantId.first()

            val restaurantResult = voucherRepository.getVouchers(
                limit = pageSize,
                offset = 0,
                restaurantId = restaurantId,
                code = null,
                status = null
            )
            val systemResult = voucherRepository.getVouchers(
                limit = pageSize,
                offset = offset,
                restaurantId = 0,
                code = _uiState.value.searchQuery.takeIf { it.isNotBlank() },
                status = null
            )

            val restaurantVouchers = restaurantResult.getOrDefault(emptyList()).map { it.toRestaurantItem() }
            val systemVouchers = systemResult.getOrDefault(emptyList()).map { it.toDomainVoucher() }

            _uiState.update { state ->
                state.copy(
                    restaurantVouchers = restaurantVouchers,
                    systemVouchers = systemVouchers,
                    totalItems = if (systemVouchers.size == pageSize) (page * pageSize + 1) else (offset + systemVouchers.size),
                    isLoading = false
                )
            }
        }
    }

    fun onTabSelected(tabIndex: Int) {
        _uiState.update { it.copy(selectedTab = tabIndex) }
        if (tabIndex == 0 || tabIndex == 1) {
            refresh()
        }
    }

    fun onSearchQueryChanged(newQuery: String) {
        _uiState.update { it.copy(searchQuery = newQuery) }
        refresh()
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
            refresh()
        }
    }

    fun onPageChanged(page: Int) {
        _uiState.update { it.copy(currentPage = page) }
        refresh()
    }
}