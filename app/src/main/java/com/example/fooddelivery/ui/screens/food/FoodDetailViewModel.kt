package com.example.fooddelivery.ui.screens.food

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.fooddelivery.R
import com.example.fooddelivery.domain.model.CartItem
import com.example.fooddelivery.domain.model.FoodItem
import com.example.fooddelivery.domain.model.Restaurant
import com.example.fooddelivery.domain.repository.CartRepository
import com.example.fooddelivery.ui.navigation.FoodDetailRoute
import com.example.fooddelivery.ui.util.GlobalSnackbarManager
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
    private val foodId: Int = savedStateHandle.toRoute<FoodDetailRoute>().foodId
    private val _state = MutableStateFlow(FoodDetailState())
    val state: StateFlow<FoodDetailState> = _state.asStateFlow()

    private val _uiEffect = MutableSharedFlow<FoodDetailUiEffect>()
    val uiEffect = _uiEffect.asSharedFlow()

    init {
        loadFoodDetail()
    }

    fun onEvent(event: FoodDetailEvent) {
        when(event) {
            is FoodDetailEvent.UpdateQuantity -> {
                _state.update {
                    val newQuantity = (it.quantity + event.delta).coerceAtLeast(1)
                    val basePrice = it.food?.price?.toDoubleOrNull() ?: 0.0
                    it.copy(
                        quantity = newQuantity,
                        totalPrice = calculatePrice(basePrice, it.selectedSize, newQuantity)
                    )
                }
            }
            is FoodDetailEvent.SelectSize -> {
                _state.update {
                    val basePrice = it.food?.price?.toDoubleOrNull() ?: 0.0
                    it.copy(
                        selectedSize = event.size,
                        totalPrice = calculatePrice(basePrice, event.size, it.quantity)
                    )
                }
            }
            is FoodDetailEvent.ToggleFavorite -> {
                _state.update { it.copy(isFavorite = !it.isFavorite) }
            }
            is FoodDetailEvent.AddToCart -> {
                val currentState = _state.value
                val food = currentState.food
                val restaurant = currentState.restaurant
                if (food != null && restaurant != null) {
                    val basePrice = food.price.toDoubleOrNull() ?: 0.0
                    val unitPrice = calculateUnitPrice(basePrice, currentState.selectedSize)
                    
                    val cartItem = CartItem(
                        food = food,
                        size = currentState.selectedSize,
                        quantity = currentState.quantity,
                        unitPrice = unitPrice,
                        restaurantId = restaurant.id,
                        restaurantName = restaurant.name
                    )
                    cartRepository.addToCart(cartItem)
                    viewModelScope.launch {
                        snackbarManager.showSnackbar("Added ${food.name} to cart")
                        _uiEffect.emit(FoodDetailUiEffect.NavigateBack)
                    }
                }
            }
        }
    }

    private fun calculateUnitPrice(basePrice: Double, size: String): Double {
        val sizeMultiplier = when (size) {
            "Small" -> 0.8
            "Large" -> 1.2
            else -> 1.0
        }
        return basePrice * sizeMultiplier
    }

    private fun calculatePrice(basePrice: Double, size: String, quantity: Int): Double {
        return calculateUnitPrice(basePrice, size) * quantity
    }

    private fun loadFoodDetail() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            
            val mockFood = FoodItem(
                id = foodId.toString(),
                name = "Pizza Calzone European",
                restaurantName = "Uttora Coffe House",
                category = "Pizza",
                price = "32.0",
                imageRes = R.drawable.food_bowl
            )

            val mockRestaurant = Restaurant(
                id = "res1",
                name = "Uttora Coffe House",
                description = "Prosciutto e funghi is a pizza variety that is topped with tomato sauce.",
                tags = listOf("Pizza", "Italian"),
                rating = 4.7f,
                deliveryFee = "Free",
                deliveryTime = "20 min",
                imageRes = R.drawable.food_bowl
            )
            
            val basePrice = mockFood.price.toDoubleOrNull() ?: 0.0
            
            _state.update {
                it.copy(
                    food = mockFood,
                    restaurant = mockRestaurant,
                    totalPrice = calculatePrice(basePrice, it.selectedSize, it.quantity),
                    isLoading = false
                )
            }
        }
    }
}
