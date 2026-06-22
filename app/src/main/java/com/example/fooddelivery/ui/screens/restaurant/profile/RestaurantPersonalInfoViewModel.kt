package com.example.fooddelivery.ui.screens.restaurant.profile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RestaurantPersonalInfoViewModel @Inject constructor() : ViewModel() {

    var uiState by mutableStateOf(RestaurantPersonalInfoState())
        private set

    init {
        loadCurrentRestaurantProfile()
    }

    private fun loadCurrentRestaurantProfile() {
        // Giả lập lấy thông tin nhà hàng hiện tại của User đang đăng nhập từ API/Database
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true)
            delay(1000) // Giả lập mạng
            uiState = RestaurantPersonalInfoState(
                name = "King Burger - Nhánh Quận 1",
                phone = "0901234567",
                street = "123 Lê Lợi",
                district = "Quận 1",
                city = "TP Hồ Chí Minh"
            )
        }
    }

    fun onEvent(event: RestaurantPersonalInfoEvent) {
        when (event) {
            is RestaurantPersonalInfoEvent.NameChanged -> uiState = uiState.copy(name = event.name)
            is RestaurantPersonalInfoEvent.PhoneChanged -> uiState = uiState.copy(phone = event.phone)
            is RestaurantPersonalInfoEvent.StreetChanged -> uiState = uiState.copy(street = event.street)
            is RestaurantPersonalInfoEvent.DistrictChanged -> uiState = uiState.copy(district = event.district)
            is RestaurantPersonalInfoEvent.CityChanged -> uiState = uiState.copy(city = event.city)
            RestaurantPersonalInfoEvent.Submit -> updateProfile()
        }
    }

    private fun updateProfile() {
        viewModelScope.launch {
            if (uiState.name.isBlank() || uiState.phone.isBlank()) {
                uiState = uiState.copy(error = "Tên và Số điện thoại không được để trống!")
                return@launch
            }

            uiState = uiState.copy(isLoading = true, error = null)
            delay(1500) // Giả lập gọi API cập nhật thông tin
            uiState = uiState.copy(isLoading = false, isSuccess = true)
        }
    }

    fun resetSuccessState() {
        uiState = uiState.copy(isSuccess = false)
    }
}
