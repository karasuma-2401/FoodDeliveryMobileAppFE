package com.example.fooddelivery.ui.screens.restaurant.profile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RestaurantPersonalInfoViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    var uiState by mutableStateOf(RestaurantPersonalInfoState())
        private set

    init {
        val isFromSignUp: Boolean = savedStateHandle["isFromSignUp"] ?: false
        uiState = uiState.copy(isFromSignUp = isFromSignUp)

        if (!isFromSignUp) {
            loadCurrentRestaurantProfile()
        }
    }

    private fun loadCurrentRestaurantProfile() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true)
            uiState = RestaurantPersonalInfoState(
                name = "King Burger - District 1",
                phone = "0901234567",
                street = "123 Le Loi Street",
                district = "District 1",
                city = "Ho Chi Minh City",
                isFromSignUp = false
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
                uiState = uiState.copy(error = "Name and Phone Number cannot be empty!")
                return@launch
            }

            uiState = uiState.copy(isLoading = true, error = null)
            uiState = uiState.copy(isLoading = false, isSuccess = true)
        }
    }

    fun resetSuccessState() {
        uiState = uiState.copy(isSuccess = false)
    }
}