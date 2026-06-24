package com.example.fooddelivery.ui.screens.admin.coupons

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.fooddelivery.ui.navigation.CreateCouponRoute
import com.example.fooddelivery.domain.repository.VoucherRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class CreateCouponUiState(
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

@HiltViewModel
class CreateCouponViewModel @Inject constructor(
    private val voucherRepository: VoucherRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    val restaurantId: Int? = try {
        savedStateHandle.toRoute<CreateCouponRoute>().restaurantId
    } catch (e: Exception) {
        null
    }

    private val _uiState = MutableStateFlow(CreateCouponUiState())
    val uiState: StateFlow<CreateCouponUiState> = _uiState.asStateFlow()

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

    private fun toIsoDateTimeOrNull(mmddyyyy: String): String? {
        return try {
            val formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy")
            val date = LocalDate.parse(mmddyyyy.trim(), formatter)
            // Backend expects ISO 8601 date string (IsDateString). We send Zulu midnight.
            "${date}T00:00:00.000Z"
        } catch (_: Exception) {
            null
        }
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
                val type = if (currentState.discountType.contains("percent", ignoreCase = true)) {
                    "PERCENT"
                } else {
                    "MONEY"
                }

                val sale = currentState.discountValue.toDoubleOrNull() ?: 0.0
                val minOrder = currentState.minOrder.toDoubleOrNull() ?: 0.0
                val maxDiscount = currentState.maxDiscount.toDoubleOrNull()?.takeIf { it > 0 }

                val startAt = toIsoDateTimeOrNull(currentState.startDate)
                val endAt = if (currentState.neverExpires) null else toIsoDateTimeOrNull(currentState.endDate)

                val name = currentState.couponCode.trim().uppercase()
                val code = currentState.couponCode.trim().uppercase()
                val description = currentState.description.trim().ifBlank { null }

                val result = voucherRepository.createVoucher(
                    name = name,
                    code = code,
                    description = description,
                    sale = sale,
                    type = type,
                    status = "APPLYING",
                    restaurantId = restaurantId,
                    minimumOrderAmount = minOrder,
                    maximumDiscountAmount = maxDiscount,
                    startAt = startAt,
                    endAt = endAt
                )

                result.onSuccess {
                    _uiState.update { it.copy(isSaving = false, isSavedSuccessfully = true) }
                }.onFailure { e ->
                    _uiState.update { it.copy(isSaving = false, errorMessage = e.localizedMessage ?: "Failed to save coupon") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, errorMessage = e.localizedMessage ?: "Failed to save coupon") }
            }
        }
    }

    fun clearNavigationFlag() {
        _uiState.update { it.copy(isSavedSuccessfully = false) }
    }
}