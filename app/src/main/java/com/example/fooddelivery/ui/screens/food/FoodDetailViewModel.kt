package com.example.fooddelivery.ui.screens.food

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.fooddelivery.domain.model.FoodItem
import com.example.fooddelivery.domain.model.Restaurant
import com.example.fooddelivery.domain.repository.CartRepository
import com.example.fooddelivery.ui.navigation.FoodDetailRoute
import com.example.fooddelivery.ui.utils.GlobalSnackbarManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FoodDetailState(
    val food: FoodItem? = null,
    val restaurant: Restaurant? = null,
    val quantity: Int = 1,
    val selectedSize: String = "Medium",
    val totalPrice: Double = 0.0,
    val isFavorite: Boolean = false,
    val isLoading: Boolean = false
)

sealed interface FoodDetailEvent {
    data class UpdateQuantity(val delta: Int) : FoodDetailEvent
    data class SelectSize(val size: String): FoodDetailEvent
    data object ToggleFavorite: FoodDetailEvent
    data object AddToCart: FoodDetailEvent
}

sealed interface FoodDetailUiEffect {
    data object NavigateBack : FoodDetailUiEffect
}

@HiltViewModel
class FoodDetailViewModel @Inject constructor(
    private val cartRepository: CartRepository,
    private val snackbarManager: GlobalSnackbarManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val foodId: String = savedStateHandle.toRoute<FoodDetailRoute>().foodId
    private val _state = MutableStateFlow(FoodDetailState())
    val state: StateFlow<FoodDetailState> = _state.asStateFlow()

    private val _uiEffect = MutableSharedFlow<FoodDetailUiEffect>()
    val uiEffect = _uiEffect.asSharedFlow()

    init {
        // loadFoodDetail() should be implemented to fetch from API
    }

    fun onEvent(event: FoodDetailEvent) {
        when(event) {
            is FoodDetailEvent.UpdateQuantity -> {
                _state.update {
                    val newQuantity = (it.quantity + event.delta).coerceAtLeast(1)
                    val basePrice = it.food?.price ?: 0.0
                    it.copy(
                        quantity = newQuantity,
                        totalPrice = basePrice * newQuantity
                    )
                }
            }
            is FoodDetailEvent.SelectSize -> {
                _state.update { it.copy(selectedSize = event.size) }
            }
            is FoodDetailEvent.ToggleFavorite -> {
                _state.update { it.copy(isFavorite = !it.isFavorite) }
            }
            is FoodDetailEvent.AddToCart -> {
                val currentState = _state.value
                val food = currentState.food
                if (food != null) {
                    _state.update { it.copy(isLoading = true) }
                    viewModelScope.launch {
                        // Logic: Truyền size vào giỏ hàng
                        val result = cartRepository.addToCart(
                            foodId = food.id.toIntOrNull() ?: 0,
                            quantity = currentState.quantity,
                            size = currentState.selectedSize
                        )
                        
                        result.onSuccess {
                            _state.update { it.copy(isLoading = false) }
                            snackbarManager.showSnackbar("Added ${food.name} (${currentState.selectedSize}) to cart")
                            _uiEffect.emit(FoodDetailUiEffect.NavigateBack)
                        }.onFailure { error ->
                            _state.update { it.copy(isLoading = false) }
                            snackbarManager.showSnackbar(error.message ?: "Failed to add to cart")
                        }
                    }
                }
            }
        }
    }
}
