package com.example.fooddelivery.ui.screens.profile.address

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
import kotlinx.coroutines.delay
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
    data class DeleteAddress(val id: String) : CustomerAddressEvent
    object ErrorDismissed : CustomerAddressEvent
}
@HiltViewModel
class CustomerAddressViewModel @Inject constructor(
    private val getAddressesUseCase: GetAddressesUseCase,
    private val deleteAddressUseCase: DeleteAddressUseCase
) : ViewModel()
{
    private val _state = MutableStateFlow(CustomerAddressState())
    val state: StateFlow<CustomerAddressState> = _state.asStateFlow()

    private var mockAddresses = mutableListOf(
        Address(id = "1", type = "HOME", title = "Home", streetName = "123 Main St", city = "New York", isDefault = true, detail = "123 Main St, New York"),
        Address(id = "2", type = "WORK", title = "Office", streetName = "456 Business Rd", city = "New York", isDefault = false, detail = "456 Business Rd, New York"),
        Address(id = "3", type = "OTHER", title = "Gym", streetName = "789 Fitness Way", city = "New York", isDefault = false, detail = "789 Fitness Way, New York")
    )

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

    fun loadAddresses() {
        if (_state.value.isLoading) return
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            delay(1000)
            
            _state.update {
                it.copy(
                    addresses = mockAddresses.map { address -> address.toUiItem() },
                    isLoading = false
                )
            }
        }
    }

    private fun deleteAddress(addressId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            delay(1000)
            mockAddresses.removeAll { it.id == addressId }
            
            loadAddresses()
        }
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
