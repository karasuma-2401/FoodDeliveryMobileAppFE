package com.example.fooddelivery.ui.screens.admin.coupons

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fooddelivery.data.remote.dto.VoucherDto
import com.example.fooddelivery.domain.repository.VoucherRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SystemVoucher(
    val id: String,
    val code: String,
    val description: String,
    val expiryText: String,
    val isActive: Boolean
)

data class AdminCouponUiState(
    val isLoading: Boolean = false,
    val searchQuery: String = "",
    val systemVouchers: List<SystemVoucher> = emptyList(),
    val totalItems: Int = 0,
    val currentPage: Int = 1
)

@HiltViewModel
class AdminCouponViewModel @Inject constructor(
    private val voucherRepository: VoucherRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminCouponUiState())
    val uiState: StateFlow<AdminCouponUiState> = _uiState.asStateFlow()

    private val pageSize = 20

    init {
        loadSystemCoupons()
    }

    private fun VoucherDto.toSystemVoucher(): SystemVoucher {
        return SystemVoucher(
            id = id.toString(),
            code = code,
            description = description ?: name,
            expiryText = "Expires: ${endAt ?: "No expiry"}",
            isActive = status.uppercase() == "APPLYING"
        )
    }

    fun loadSystemCoupons() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val page = _uiState.value.currentPage.coerceAtLeast(1)
            val offset = (page - 1) * pageSize

            val result = voucherRepository.getVouchers(
                limit = pageSize,
                offset = offset,
                restaurantId = null, // System vouchers
                code = _uiState.value.searchQuery.takeIf { it.isNotBlank() },
                status = null
            )

            result.onSuccess { list ->
                val vouchers = list.map { it.toSystemVoucher() }
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        systemVouchers = vouchers,
                        // Simple workaround for totalItems until backend provides it
                        totalItems = if (vouchers.size == pageSize) (page * pageSize + 1) else (offset + vouchers.size)
                    )
                }
            }.onFailure {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun onSearchQueryChanged(newQuery: String) {
        _uiState.update { it.copy(searchQuery = newQuery) }
        loadSystemCoupons()
    }

    fun toggleSystemCoupon(couponId: String, isActive: Boolean) {
        val targetStatus = if (isActive) "APPLYING" else "ENDED"
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            voucherRepository.updateVoucherStatus(
                id = couponId.toIntOrNull() ?: return@launch,
                status = targetStatus
            )
            loadSystemCoupons()
        }
    }

    fun onPageChanged(newPage: Int) {
        _uiState.update { it.copy(currentPage = newPage) }
        loadSystemCoupons()
    }
}
