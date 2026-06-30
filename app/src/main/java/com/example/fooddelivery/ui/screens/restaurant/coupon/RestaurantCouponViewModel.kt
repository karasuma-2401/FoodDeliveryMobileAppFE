package com.example.fooddelivery.ui.screens.restaurant.coupon

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fooddelivery.data.local.datastore.TokenManager
import com.example.fooddelivery.data.remote.dto.VoucherDto
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
    val restaurantVouchers: List<RestaurantVoucherItem> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null
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
        loadRestaurantVouchers()
    }

    fun loadRestaurantVouchers(isRefresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = !isRefresh,
                    isRefreshing = isRefresh,
                    errorMessage = null
                )
            }
            val restaurantId = tokenManager.getRestaurantId.first()
            val restaurantResult = voucherRepository.getVouchers(
                limit = pageSize,
                offset = 0,
                restaurantId = restaurantId,
                code = null,
                status = null
            )

            restaurantResult.onSuccess { dtoList ->
                val vouchers = dtoList.map { it.toRestaurantItem() }
                _uiState.update { state ->
                    state.copy(
                        restaurantVouchers = vouchers,
                        isLoading = false,
                        isRefreshing = false
                    )
                }
            }.onFailure { error ->
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        isRefreshing = false,
                        errorMessage = error.message ?: "Failed to load restaurant coupons"
                    )
                }
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
}