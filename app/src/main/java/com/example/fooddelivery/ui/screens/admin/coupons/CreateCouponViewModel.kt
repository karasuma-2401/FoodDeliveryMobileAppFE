package com.example.fooddelivery.ui.screens.admin.coupons

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.fooddelivery.ui.navigation.CreateCouponRoute
import com.example.fooddelivery.domain.repository.VoucherRepository
import com.example.fooddelivery.data.local.datastore.TokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

data class CreateCouponUiState(
    val couponCode: String = "",
    val description: String = "",
    val discountType: String = "Percentage Discount",
    val discountValue: String = "0",
    val maxDiscount: String = "0.00",
    val minOrder: String = "25",
    val perUserLimit: String = "1",
    val totalUsageLimit: String = "500",
    val startDate: String = "01/10/2026",
    val endDate: String = "31/12/2026",
    val neverExpires: Boolean = false,
    val isSaving: Boolean = false,
    val isSavedSuccessfully: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class CreateCouponViewModel @Inject constructor(
    private val voucherRepository: VoucherRepository,
    private val tokenManager: TokenManager,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val routeRestaurantId: Int? = try {
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

    private fun toIsoDateTimeOrNull(ddmmyyyy: String): String? {
        return try {
            val inputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.US)

            val outputFormat = SimpleDateFormat("yyyy-MM-dd'T'00:00:00.000'Z'", Locale.US)
            outputFormat.timeZone = TimeZone.getTimeZone("UTC")

            val date = inputFormat.parse(ddmmyyyy.trim())
            if (date != null) {
                outputFormat.format(date)
            } else {
                null
            }
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

        val sale = currentState.discountValue.toDoubleOrNull()
        if (sale == null || sale <= 0) {
            _uiState.update { it.copy(errorMessage = "Invalid discount value") }
            return
        }

        val isPercent = currentState.discountType.contains("percent", ignoreCase = true)
        if (isPercent && sale > 100) {
            _uiState.update { it.copy(errorMessage = "Percentage discount cannot exceed 100%") }
            return
        }

        val minOrder = currentState.minOrder.toDoubleOrNull()
        if (minOrder == null || minOrder < 0) {
            _uiState.update { it.copy(errorMessage = "Invalid minimum order amount") }
            return
        }

        val maxDiscount = if (isPercent) {
            currentState.maxDiscount.toDoubleOrNull()
        } else {
            null
        }
        if (isPercent && maxDiscount != null && maxDiscount < 0) {
            _uiState.update { it.copy(errorMessage = "Invalid maximum discount amount") }
            return
        }

        val perUserLimit = currentState.perUserLimit.toIntOrNull()
        if (perUserLimit == null || perUserLimit <= 0) {
            _uiState.update { it.copy(errorMessage = "Invalid per user limit") }
            return
        }

        val totalUsageLimit = currentState.totalUsageLimit.toIntOrNull()
        if (totalUsageLimit == null || totalUsageLimit <= 0) {
            _uiState.update { it.copy(errorMessage = "Invalid total usage limit") }
            return
        }

        val startAt = toIsoDateTimeOrNull(currentState.startDate)
        if (startAt == null) {
            _uiState.update { it.copy(errorMessage = "Invalid start date format (dd/MM/yyyy)") }
            return
        }

        val endAt = if (currentState.neverExpires) null else {
            val date = toIsoDateTimeOrNull(currentState.endDate)
            if (date == null) {
                _uiState.update { it.copy(errorMessage = "Invalid end date format (dd/MM/yyyy)") }
                return
            }
            date
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            try {
                // For system coupons (admin), restaurantId can be null
                // For restaurant-specific coupons, use the route parameter or tokenManager
                val finalRestaurantId = routeRestaurantId ?: tokenManager.getRestaurantId.first()

                val type = if (isPercent) "PERCENT" else "MONEY"
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
                    restaurantId = finalRestaurantId,
                    minimumOrderAmount = minOrder,
                    maximumDiscountAmount = if (isPercent) maxDiscount?.takeIf { it > 0 } else null,
                    startAt = startAt,
                    endAt = endAt,
                    usageLimit = totalUsageLimit,
                    userLimit = perUserLimit
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