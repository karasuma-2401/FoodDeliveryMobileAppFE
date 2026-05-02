package com.example.fooddelivery.ui.screens.restaurant_detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fooddelivery.R
import com.example.fooddelivery.domain.model.FoodItem
import com.example.fooddelivery.domain.model.Restaurant
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RestaurantDetailState(
    val restaurant: Restaurant? = null,
    val foodItems: List<FoodItem> = emptyList(),
    val categories: List<String> = listOf("Burger", "Sandwich", "Pizza"),
    val selectedCategory: String = "Burger",
    val isLoading: Boolean = false
)
sealed interface RestaurantDetailEvent {
    data class CategorySelected(val category: String) : RestaurantDetailEvent
    data class AddFoodToCart(val foodItem: FoodItem) : RestaurantDetailEvent
}
@HiltViewModel
class RestaurantDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val restaurantId: String = checkNotNull(savedStateHandle["restaurantId"])
    private val _state = MutableStateFlow(RestaurantDetailState())
    val state: StateFlow<RestaurantDetailState> = _state.asStateFlow()

    init {
        loadRestaurantDetails()
    }
    fun onEvent(event: RestaurantDetailEvent) {
        when(event) {
            is RestaurantDetailEvent.CategorySelected -> {
                _state.update { it.copy(selectedCategory = event.category) }
                filterFoodByCategory(event.category)
            }
            is RestaurantDetailEvent.AddFoodToCart -> {
                // logic add to cart
            }
        }
    }
    private fun loadRestaurantDetails () {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            delay(1000)
            val mockRestaurant = Restaurant(
                id = restaurantId,
                name = "Spicy Restaurant",
                description = "Maecenas sed diam eget risus varius blandit sit amet non magna. Integer posuere erat a ante venenatis dapibus posuere velit aliquet.",
                tags = listOf("Burger", "Chicken", "Rice", "Wings"),
                rating = 4.7f,
                deliveryFee = "Free",
                deliveryTime = "20 min",
                imageRes = R.drawable.food_bowl
            )
            val mockFoodItems = listOf(
                FoodItem("1", "Burger Ferguson", "Spicy Restaurant", "Burger", "40", R.drawable.food_bowl, "PROMOTION"),
                FoodItem("2", "Rockin' Burgers", "Cafecafachino", "Burger", "40", R.drawable.food_bowl, "GIẢM 20%"),
                FoodItem("3", "Egg Burger", "Spicy Restaurant", "Burger", "35", R.drawable.food_bowl, "FREESHIP"),
                FoodItem("4", "BBQ Special", "Flame Grills", "Burger", "45", R.drawable.food_bowl, "HOT")
            )
            _state.update {
                it.copy(
                    restaurant = mockRestaurant,
                    foodItems = mockFoodItems,
                    isLoading = false
                )
            }
        }
    }
    private fun filterFoodByCategory(category: String) {
        // logic filter food items based on category
    }
}