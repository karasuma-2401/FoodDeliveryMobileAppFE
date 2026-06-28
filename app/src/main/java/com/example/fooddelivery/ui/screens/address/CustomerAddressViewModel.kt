package com.example.fooddelivery.ui.screens.address

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Work
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fooddelivery.domain.model.Address
import com.example.fooddelivery.domain.model.AddressItem
import com.example.fooddelivery.domain.usecase.DeleteAddressUseCase
import com.example.fooddelivery.domain.usecase.GetAddressesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CustomerAddressState(
    val addresses: List<AddressItem> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

sealed interface CustomerAddressEvent {
    object LoadAddresses : CustomerAddressEvent
    data class DeleteAddress(val id: Int) : CustomerAddressEvent
    object ErrorDismissed : CustomerAddressEvent
}

@HiltViewModel
class CustomerAddressViewModel @Inject constructor(
    private val getAddressesUseCase: GetAddressesUseCase,
    private val deleteAddressUseCase: DeleteAddressUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(CustomerAddressState())
    val state: StateFlow<CustomerAddressState> = _state.asStateFlow()

    init {
        onEvent(CustomerAddressEvent.LoadAddresses)
    }

    fun onEvent(event: CustomerAddressEvent) {
        when(event) {
            CustomerAddressEvent.LoadAddresses -> loadAddresses()
            is CustomerAddressEvent.DeleteAddress -> deleteAddress(event.id)
            CustomerAddressEvent.ErrorDismissed -> _state.update { it.copy(errorMessage = null) }
        }
    }

    private fun loadAddresses() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            getAddressesUseCase().onSuccess { addresses ->
                _state.update {
                    it.copy(
                        addresses = addresses.map { addr -> addr.toUiItem() },
                        isLoading = false
                    )
                }
            }.onFailure { error ->
                _state.update { it.copy(isLoading = false, errorMessage = error.message) }
            }
        }
    }

    private fun deleteAddress(addressId: Int) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            deleteAddressUseCase(addressId).onSuccess {
                loadAddresses()
            }.onFailure { error ->
                _state.update { it.copy(isLoading = false, errorMessage = error.message) }
            }
        }
    }

    private fun Address.toUiItem(): AddressItem {
        // Mapping icon dựa trên type (title từ API: "Nhà riêng", "Văn phòng",...)
        val (icon, color, bgColor) = when {
            type.contains("Nhà", ignoreCase = true) || type.contains("Home", ignoreCase = true) -> 
                Triple(Icons.Outlined.Home, Color(0xFF4285F4), Color(0xFFE8F0FE))
            type.contains("Văn phòng", ignoreCase = true) || type.contains("Work", ignoreCase = true) -> 
                Triple(Icons.Outlined.Work, Color(0xFF9C27B0), Color(0xFFF3E5F5))
            else -> 
                Triple(Icons.Outlined.LocationOn, Color(0xFFFF9800), Color(0xFFFFF4E5))
        }
        return AddressItem(
            id = id,
            type = type,
            title = title,
            detail = detail,
            deliveryNote = deliveryNote,
            icon = icon,
            iconColor = color,
            iconBgColor = bgColor
        )
    }
}
