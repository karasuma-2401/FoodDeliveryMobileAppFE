package com.example.fooddelivery.ui.screens.restaurant.profile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fooddelivery.domain.repository.RestaurantRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RestaurantPersonalInfoViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: RestaurantRepository
) : ViewModel() {

    var uiState by mutableStateOf(RestaurantPersonalInfoState())
        private set

    private var currentRestaurantId: Int = -1

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

            if (currentRestaurantId != -1) {
                val result = repository.getRestaurantById(currentRestaurantId)
                result.onSuccess { profile ->
                    uiState = uiState.copy(
                        name = profile.name,
                        phone = profile.phone ?: "",
                        description = profile.description ?: "",
                        isLoading = false
                    )
                }.onFailure { error ->
                    uiState = uiState.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to load profile"
                    )
                }
            } else {
                val myRestaurantsResult = repository.getMyRestaurants()
                myRestaurantsResult.onSuccess { list ->
                    val myRestaurant = list.firstOrNull()
                    if (myRestaurant != null) {
                        currentRestaurantId = myRestaurant.id
                        uiState = uiState.copy(
                            name = myRestaurant.name,
                            phone = myRestaurant.phone ?: "",
                            description = myRestaurant.description ?: "",
                            isLoading = false
                        )
                    }
                }.onFailure {
                    uiState = uiState.copy(isLoading = false, error = "Failed to load your restaurant")
                }
            }
        }
    }

    fun onEvent(event: RestaurantPersonalInfoEvent) {
        when (event) {
            is RestaurantPersonalInfoEvent.NameChanged -> uiState = uiState.copy(name = event.name, error = null)
            is RestaurantPersonalInfoEvent.PhoneChanged -> uiState = uiState.copy(phone = event.phone, error = null)
            is RestaurantPersonalInfoEvent.DescriptionChanged -> uiState = uiState.copy(description = event.description, error = null)
            RestaurantPersonalInfoEvent.Submit -> updateProfile()
        }
    }

    private fun updateProfile() {
        viewModelScope.launch {
            if (uiState.name.isBlank() || uiState.phone.isBlank()) {
                uiState = uiState.copy(error = "Name and Phone Number cannot be empty!")
                return@launch
            }

            if (currentRestaurantId == -1) {
                uiState = uiState.copy(error = "Restaurant ID is missing!")
                return@launch
            }

            uiState = uiState.copy(isLoading = true, error = null)

            // 🌟 Gọi API PATCH để cập nhật
            val result = repository.updateRestaurantProfile(
                restaurantId = currentRestaurantId,
                name = uiState.name,
                phone = uiState.phone,
                description = uiState.description
            )

            result.onSuccess {
                uiState = uiState.copy(isLoading = false, isSuccess = true)
            }.onFailure { error ->
                uiState = uiState.copy(
                    isLoading = false,
                    error = error.message ?: "Failed to update profile"
                )
            }
        }
    }

    fun resetSuccessState() {
        uiState = uiState.copy(isSuccess = false)
    }
}