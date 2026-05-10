package com.example.fooddelivery.ui.screens.restaurant.food_management

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fooddelivery.data.remote.dto.FoodRequest
import com.example.fooddelivery.domain.repository.RestaurantRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EditFoodState(
    val foodId: String = "",
    val itemName: String = "",
    val price: String = "",
    val details: String = "",
    val category: String = "",
    val imageUrl: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)

@HiltViewModel
class EditFoodViewModel @Inject constructor(
    private val repository: RestaurantRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val _state = mutableStateOf(EditFoodState())
    val state: State<EditFoodState> = _state

    init {
        savedStateHandle.get<String>("foodId")?.let { foodId ->
            loadFoodDetails(foodId)
        }
    }

    private fun loadFoodDetails(id: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, foodId = id)
            repository.getFoodById(id)
                .onSuccess { food ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        itemName = food.name,
                        price = food.price.toString(),
                        details = food.details,
                        category = food.category,
                        imageUrl = food.imageUrl
                    )
                }
                .onFailure { error ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = error.message
                    )
                }
        }
    }

    fun onNameChange(newName: String) { _state.value = _state.value.copy(itemName = newName) }
    fun onPriceChange(newPrice: String) { _state.value = _state.value.copy(price = newPrice) }
    fun onDetailsChange(newDetails: String) { _state.value = _state.value.copy(details = newDetails) }

    fun updateFoodItem() {
        val currentState = _state.value
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            val request = FoodRequest(
                name = currentState.itemName,
                price = currentState.price.toDoubleOrNull() ?: 0.0,
                details = currentState.details,
                category = currentState.category,
                imageUrl = currentState.imageUrl
            )
            
            repository.updateFood(currentState.foodId, request)
                .onSuccess {
                    _state.value = _state.value.copy(isLoading = false, isSuccess = true)
                }
                .onFailure { error ->
                    _state.value = _state.value.copy(isLoading = false, error = error.message)
                }
        }
    }
}
