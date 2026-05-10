package com.example.fooddelivery.ui.screens.restaurant.food_management

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fooddelivery.data.remote.dto.FoodRequest
import com.example.fooddelivery.domain.repository.RestaurantRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddFoodState(
    val itemName: String = "",
    val price: String = "",
    val details: String = "",
    val category: String = "Breakfast",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)

@HiltViewModel
class AddFoodViewModel @Inject constructor(
    private val repository: RestaurantRepository
) : ViewModel() {
    private val _state = mutableStateOf(AddFoodState())
    val state: State<AddFoodState> = _state

    fun onNameChange(newName: String) { _state.value = _state.value.copy(itemName = newName) }
    fun onPriceChange(newPrice: String) { _state.value = _state.value.copy(price = newPrice) }
    fun onDetailsChange(newDetails: String) { _state.value = _state.value.copy(details = newDetails) }
    fun onCategoryChange(newCategory: String) { _state.value = _state.value.copy(category = newCategory) }

    fun saveFoodItem() {
        val currentState = _state.value
        if (currentState.itemName.isBlank() || currentState.price.isBlank()) {
            _state.value = _state.value.copy(error = "Please fill in all required fields")
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            val request = FoodRequest(
                name = currentState.itemName,
                price = currentState.price.toDoubleOrNull() ?: 0.0,
                details = currentState.details,
                category = currentState.category
            )
            
            repository.addFood(request)
                .onSuccess {
                    _state.value = _state.value.copy(isLoading = false, isSuccess = true)
                }
                .onFailure { error ->
                    _state.value = _state.value.copy(isLoading = false, error = error.message)
                }
        }
    }
    
    fun resetState() {
        _state.value = AddFoodState()
    }
}
