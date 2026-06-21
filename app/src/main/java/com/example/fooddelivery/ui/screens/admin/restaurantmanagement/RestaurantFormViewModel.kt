package com.example.fooddelivery.ui.screens.admin.restaurantmanagement
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import androidx.compose.runtime.getValue

data class RestaurantFormState(
    val name: String = "",
    val phone: String = "",
    val imageUrl: String = "",
    val isApproved: Boolean = false,
    val street: String = "",
    val district: String = "",
    val city: String = "",
    val ownerId: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)
class RestaurantFormViewModel : ViewModel() {
    var uiState by mutableStateOf(RestaurantFormState())
        private set

    fun onEvent(event: RestaurantFormEvent) {
        when (event) {
            is RestaurantFormEvent.NameChanged -> uiState = uiState.copy(name = event.name)
            is RestaurantFormEvent.PhoneChanged -> uiState = uiState.copy(phone = event.phone)
            is RestaurantFormEvent.ApprovedChanged -> uiState =
                uiState.copy(isApproved = event.isApproved)

            is RestaurantFormEvent.StreetChanged -> uiState = uiState.copy(street = event.street)
            is RestaurantFormEvent.DistrictChanged -> uiState =
                uiState.copy(district = event.district)

            is RestaurantFormEvent.CityChanged -> uiState = uiState.copy(city = event.city)
            is RestaurantFormEvent.OwnerIdChanged -> uiState = uiState.copy(ownerId = event.ownerId)
            is RestaurantFormEvent.Submit -> saveRestaurant()
        }
    }

    private fun saveRestaurant() {
        // Logic gọi API lưu vào DB ở đây
        uiState = uiState.copy(isLoading = true)
    }

    fun loadRestaurantData(id: Int) {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true)

            val mockData = RestaurantFormState(
                name = "King Burger $id",
                phone = "0901234567",
                isApproved = true,
                street = "123 Main St",
                district = "District 1",
                city = "Ho Chi Minh City",
                ownerId = "USER_99",
                isLoading = false
            )

            uiState = mockData
        }
    }
}
sealed class RestaurantFormEvent {
    data class NameChanged(val name: String) : RestaurantFormEvent()
    data class PhoneChanged(val phone: String) : RestaurantFormEvent()
    data class ApprovedChanged(val isApproved: Boolean) : RestaurantFormEvent()
    data class StreetChanged(val street: String) : RestaurantFormEvent()
    data class DistrictChanged(val district: String) : RestaurantFormEvent()
    data class CityChanged(val city: String) : RestaurantFormEvent()
    data class OwnerIdChanged(val ownerId: String) : RestaurantFormEvent()
    object Submit : RestaurantFormEvent()
}