package com.example.fooddelivery.ui.screens.food

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.fooddelivery.R
import com.example.fooddelivery.domain.model.FoodItem
import com.example.fooddelivery.domain.model.Restaurant
import com.example.fooddelivery.ui.navigation.FoodDetailRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FoodDetailState(
    val food: FoodItem? = null,
    val restaurant: Restaurant? = null,
    val quantity: Int = 0,
    val selectedSize: String = "",
    val isFavorite: Boolean = false,
    val isLoading: Boolean = false
)
sealed interface FoodDetailEvent {
    data class UpdateQuantity(val quantity: Int) : FoodDetailEvent
    data class SelectSize(val size: String): FoodDetailEvent
    data object ToggleFavorite: FoodDetailEvent
    data object AddToCart: FoodDetailEvent
}
@HiltViewModel
class FoodDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val foodId: Int = savedStateHandle.toRoute<FoodDetailRoute>().foodId
    private val _state = MutableStateFlow(FoodDetailState())
    val state: StateFlow<FoodDetailState> = _state.asStateFlow()

    init {
        loadFoodDetail()
    }
    fun onEvent(event: FoodDetailEvent) {
        when(event) {
            is FoodDetailEvent.UpdateQuantity -> {
                _state.update {
                    val newQuantity = (it.quantity + event.quantity).coerceAtLeast(1)
                    it.copy(quantity = newQuantity)
                }
            }
            is FoodDetailEvent.SelectSize -> {
                _state.update { it.copy(selectedSize = event.size) }
            }
            is FoodDetailEvent.ToggleFavorite -> {
                _state.update { it.copy(isFavorite = !it.isFavorite) }
            }
            is FoodDetailEvent.AddToCart -> {}
        }
    }
    private fun loadFoodDetail() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val mockFood = FoodItem(
                id = foodId.toString(),
                name = "Pizza Calzone European",
                restaurantName = "Uttora Coffe House",
                category = "Pizza",
                price = "32",
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
            _state.update {
                it.copy(
                    food = mockFood,
                    restaurant = mockRestaurant,
                    isLoading = false
                )
            }
        }
    }
}