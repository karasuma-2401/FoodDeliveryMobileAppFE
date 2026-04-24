package com.example.fooddelivery.ui.screens.profile.address

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fooddelivery.domain.model.Address
import com.example.fooddelivery.domain.usecase.AddAddressUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class AddAddressState(
    val title: String = "",
    val city: String = "",
    val streetName: String = "",
    val type: String = "Home",
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class AddAddressViewModel @Inject constructor(
    private val addAddressUseCase: AddAddressUseCase
) : ViewModel() {

    private val _state = mutableStateOf(AddAddressState())
    val state: State<AddAddressState> = _state

    fun onTitleChange(newTitle: String) {
        _state.value = _state.value.copy(title = newTitle)
    }

    fun onCityChange(newCity: String) {
        _state.value = _state.value.copy(city = newCity)
    }

    fun onStreetNameChange(newStreet: String) {
        _state.value = _state.value.copy(streetName = newStreet)
    }

    fun onTypeChange(newType: String) {
        _state.value = _state.value.copy(type = newType)
    }

    fun saveAddress() {
        val currentState = _state.value
        if (currentState.streetName.isBlank()) {
            _state.value = currentState.copy(errorMessage = "Street name is required")
            return
        }

        viewModelScope.launch {
            _state.value = currentState.copy(isLoading = true, errorMessage = null)
            
            val result = withContext(Dispatchers.IO) {
                addAddressUseCase(
                    Address(
                        title = currentState.title.ifBlank { currentState.type },
                        detail = "${currentState.streetName}${if (currentState.city.isNotBlank()) ", ${currentState.city}" else ""}",
                        type = currentState.type.uppercase()
                    )
                )
            }

            result.onSuccess {
                _state.value = _state.value.copy(isLoading = false, isSuccess = true)
            }.onFailure { exception ->
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = exception.message ?: "Failed to save address"
                )
            }
        }
    }
}
