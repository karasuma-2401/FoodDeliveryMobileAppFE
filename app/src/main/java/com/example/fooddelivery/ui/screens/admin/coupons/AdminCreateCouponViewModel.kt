package com.example.fooddelivery.ui.screens.admin.coupons

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AdminCreateCouponUiState(
    val couponCode: String = "",
    val description: String = "",
    val discountType: String = "Percentage Discount",
    val discountValue: String = "0",
    val maxDiscount: String = "0.00",
    val minOrder: String = "25",
    val perUserLimit: String = "1",
    val totalUsageLimit: String = "500",
    val startDate: String = "10/01/2026",
    val endDate: String = "12/31/2026",
    val neverExpires: Boolean = false,
    val isSaving: Boolean = false,
    val isSavedSuccessfully: Boolean = false,
    val errorMessage: String? = null
)

class AdminCreateCouponViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(AdminCreateCouponUiState())
    val uiState: StateFlow<AdminCreateCouponUiState> = _uiState.asStateFlow()

    // --- Các hàm cập nhật State từ UI thay cho remember mutableStateOf ---
    fun onCouponCodeChange(value: String) { _uiState.update { it.copy(couponCode = value) } }
    fun onDescriptionChange(value: String) { _uiState.update { it.copy(description = value) } }
    fun onDiscountTypeChange(value: String) { _uiState.update { it.copy(discountType = value) } }
    fun onDiscountValueChange(value: String) { _uiState.update { it.copy(discountValue = value) } }
    fun onMaxDiscountChange(value: String) { _uiState.update { it.copy(maxDiscount = value) } }
    fun onMinOrderChange(value: String) { _uiState.update { it.copy(minOrder = value) } }
    fun onPerUserLimitChange(value: String) { _uiState.update { it.copy(perUserLimit = value) } }
    fun onTotalUsageLimitChange(value: String) { _uiState.update { it.copy(totalUsageLimit = value) } }
    fun onStartDateChange(value: String) { _uiState.update { it.copy(startDate = value) } }
    fun onEndDateChange(value: String) { _uiState.update { it.copy(endDate = value) } }

    fun onNeverExpiresChange(value: Boolean) {
        _uiState.update { it.copy(neverExpires = value) }
    }
    fun saveCoupon() {
        val currentState = _uiState.value
        if (currentState.couponCode.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Coupon Code cannot be empty!") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            try {
                _uiState.update { it.copy(isSaving = false, isSavedSuccessfully = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, errorMessage = e.localizedMessage ?: "Failed to save coupon") }
            }
        }
    }

    fun clearNavigationFlag() {
        _uiState.update { it.copy(isSavedSuccessfully = false) }
    }
}