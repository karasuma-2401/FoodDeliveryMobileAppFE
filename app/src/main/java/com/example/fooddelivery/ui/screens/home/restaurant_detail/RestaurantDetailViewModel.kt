package com.example.fooddelivery.ui.screens.home.restaurant_detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.fooddelivery.R
import com.example.fooddelivery.domain.model.FoodItem
import com.example.fooddelivery.domain.model.Restaurant
import com.example.fooddelivery.ui.navigation.RestaurantDetailRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RestaurantDetailState(
    val restaurant: Restaurant? = null,
    val foodItems: List<FoodItem> = emptyList(),
    val categories: List<String> = listOf("Burger", "Sandwich", "Pizza"),
    val selectedCategory: String = "Burger",
    val isLoading: Boolean = false,
    val categorizedFoodItem: Map<String, List<FoodItem>> = emptyMap()
)

sealed interface RestaurantDetailEvent {
    data class CategorySelected(val category: String) : RestaurantDetailEvent
    data class AddFoodToCart(val foodItem: FoodItem) : RestaurantDetailEvent
}
sealed interface RestaurantDetailUiEffect {
    data class ShowSnackBar(val message: String) : RestaurantDetailUiEffect
}
@HiltViewModel
class RestaurantDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val restaurantId: String = savedStateHandle.toRoute<RestaurantDetailRoute>().restaurantId
    private val _state = MutableStateFlow(RestaurantDetailState())
    val state: StateFlow<RestaurantDetailState> = _state.asStateFlow()

    private val _uiEffect = MutableSharedFlow<RestaurantDetailUiEffect>()
    val uiEffect = _uiEffect.asSharedFlow()

    init {
        loadRestaurantDetails()
    }

    fun onEvent(event: RestaurantDetailEvent) {
        when(event) {
            is RestaurantDetailEvent.CategorySelected -> {
                _state.update { it.copy(selectedCategory = event.category) }
            }
            is RestaurantDetailEvent.AddFoodToCart -> {
                addToCart(event.foodItem)
            }
        }
    }

    private fun addToCart(foodItem: FoodItem) {
        viewModelScope.launch {
            // logic add to cart (repository/ usecase)
            _uiEffect.emit(RestaurantDetailUiEffect.ShowSnackBar("${foodItem.name} added to cart"))
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
                // --- BURGERS ---
                FoodItem("1", "Burger Ferguson", "Spicy Restaurant", "Burger", "40", R.drawable.food_bowl, "PROMOTION"),
                FoodItem("2", "Rockin' Burgers", "Cafecafachino", "Burger", "40", R.drawable.food_bowl, "GIẢM 20%"),
                FoodItem("3", "Egg Burger", "Spicy Restaurant", "Burger", "35", R.drawable.food_bowl, "FREESHIP"),
                FoodItem("4", "BBQ Special", "Flame Grills", "Burger", "45", R.drawable.food_bowl, "HOT"),

                // --- SANDWICHES ---
                FoodItem("5", "Club Sandwich", "Green Bakery", "Sandwich", "30", R.drawable.food_bowl, "BÁN CHẠY"),
                FoodItem("6", "Tuna Melt", "Ocean Delights", "Sandwich", "32", R.drawable.food_bowl, "FREESHIP"),
                FoodItem("7", "Beef Pastrami", "The Deli Shop", "Sandwich", "50", R.drawable.food_bowl, "NEW"),
                FoodItem("8", "Veggie Supreme", "Green Bakery", "Sandwich", "28", R.drawable.food_bowl, "HEALTHY"),

                // --- PIZZAS ---
                FoodItem("9", "Margherita Pizza", "Pizza Heaven", "Pizza", "120", R.drawable.food_bowl, "GIẢM 10%"),
                FoodItem("10", "Pepperoni Feast", "The Italian Job", "Pizza", "150", R.drawable.food_bowl, "HOT"),
                FoodItem("11", "Seafood Black Pepper", "Pizza Heaven", "Pizza", "180", R.drawable.food_bowl, "PROMOTION"),
                FoodItem("12", "Hawaiian Classic", "Tropical Slice", "Pizza", "140", R.drawable.food_bowl, "FREESHIP")
            )
            
            _state.update {
                it.copy(
                    restaurant = mockRestaurant,
                    foodItems = mockFoodItems,
                    categorizedFoodItem = mockFoodItems.groupBy { item -> item.category },
                    isLoading = false
                )
            }
        }
    }
}
