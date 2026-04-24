package com.example.fooddelivery.ui.screens.profile.address

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Work
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fooddelivery.domain.model.Address
import com.example.fooddelivery.domain.usecase.GetAddressesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class CustomerAddressState(
    val addresses: List<AddressItem> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class CustomerAddressViewModel @Inject constructor(
    private val getAddressesUseCase: GetAddressesUseCase
) : ViewModel() {

    private val _state = mutableStateOf(CustomerAddressState())
    val state: State<CustomerAddressState> = _state

    init {
        loadAddresses()
    }

    fun loadAddresses() {
        if (_state.value.isLoading) return

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)

            val result = withContext(Dispatchers.IO) {
                getAddressesUseCase()
            }

            result.onSuccess { domainAddresses ->
                _state.value = _state.value.copy(
                    addresses = domainAddresses.map { it.toUiItem() },
                    isLoading = false
                )
            }.onFailure { exception ->
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = exception.message ?: "Failed to load addresses"
                )
            }
        }
    }

    fun deleteAddress(addressId: String) {
        // Logic xoá địa chỉ sẽ được thêm sau khi có DeleteAddressUseCase
    }

    private fun Address.toUiItem(): AddressItem {
        val (icon, color, bgColor) = when (type.uppercase()) {
            "HOME" -> Triple(Icons.Outlined.Home, Color(0xFF4285F4), Color(0xFFE8F0FE))
            "WORK" -> Triple(Icons.Outlined.Work, Color(0xFF9C27B0), Color(0xFFF3E5F5))
            else -> Triple(Icons.Outlined.LocationOn, Color(0xFFFF9800), Color(0xFFFFF4E5))
        }
        return AddressItem(
            id = id,
            type = type,
            title = title,
            detail = detail,
            icon = icon,
            iconColor = color,
            iconBgColor = bgColor
        )
    }
}
