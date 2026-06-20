package com.example.fooddelivery.ui.screens.admin.coupons
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


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

class AdminCouponViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(AdminCouponUiState())
    val uiState: StateFlow<AdminCouponUiState> = _uiState.asStateFlow()

    init {
        loadSystemCoupons()
    }

    private fun loadSystemCoupons() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val mockVouchers = listOf(
                SystemVoucher("1", "DFOOD50", "Giảm 50k cho đơn từ 150k", "Hết hạn: 31/12/2026", true),
                SystemVoucher("2", "FREESHIP", "Miễn phí vận chuyển toàn quốc", "Hết hạn: 30/09/2026", false),
                SystemVoucher("3", "WELCOME20", "Giảm 20% cho người dùng mới", "Hết hạn: 15/08/2026", true)
            )

            _uiState.update {
                it.copy(
                    isLoading = false,
                    systemVouchers = mockVouchers,
                    totalItems = mockVouchers.size
                )
            }
        }
    }

    fun onSearchQueryChanged(newQuery: String) {
        _uiState.update { it.copy(searchQuery = newQuery) }
    }

    fun toggleSystemCoupon(couponId: String, isActive: Boolean) {
        _uiState.update { currentState ->
            val updatedList = currentState.systemVouchers.map { voucher ->
                if (voucher.id == couponId) voucher.copy(isActive = isActive) else voucher
            }
            currentState.copy(systemVouchers = updatedList)
        }
    }

    fun onPageChanged(newPage: Int) {
        _uiState.update { it.copy(currentPage = newPage) }
    }
}