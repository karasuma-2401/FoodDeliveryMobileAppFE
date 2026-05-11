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

            // Vary restaurant data based on restaurantId
            val (mockRestaurant, mockFoodItems) = when (restaurantId) {
                "1" -> {
                    val restaurant = Restaurant(
                        id = restaurantId,
                        name = "Rose Garden Restaurant",
                        description = "Authentic Asian cuisine with fresh ingredients and traditional recipes.",
                        tags = listOf("Burger", "Chicken", "Rice", "Wings"),
                        rating = 4.7f,
                        deliveryFee = 0.0,
                        deliveryTime = "20 min",
                        imageRes = R.drawable.food_bowl
                    )
                    val items = listOf(
                        FoodItem(id = "1", name = "Burger Ferguson", restaurantId = restaurantId, restaurantName = "Rose Garden Restaurant", categoryId = "Burger", price = 40.0, imageRes = R.drawable.food_bowl, promoTag = "PROMOTION"),
                        FoodItem(id = "2", name = "Rockin' Burgers", restaurantId = restaurantId, restaurantName = "Rose Garden Restaurant", categoryId = "Burger", price = 40.0, imageRes = R.drawable.food_bowl, promoTag = "GIẢM 20%"),
                        FoodItem(id = "3", name = "Egg Burger", restaurantId = restaurantId, restaurantName = "Rose Garden Restaurant", categoryId = "Burger", price = 35.0, imageRes = R.drawable.food_bowl, promoTag = "FREESHIP"),
                        FoodItem(id = "5", name = "Club Sandwich", restaurantId = restaurantId, restaurantName = "Rose Garden Restaurant", categoryId = "Sandwich", price = 30.0, imageRes = R.drawable.food_bowl, promoTag = "BÁN CHẠY"),
                        FoodItem(id = "6", name = "Tuna Melt", restaurantId = restaurantId, restaurantName = "Rose Garden Restaurant", categoryId = "Sandwich", price = 32.0, imageRes = R.drawable.food_bowl, promoTag = "FREESHIP"),
                        FoodItem(id = "9", name = "Margherita Pizza", restaurantId = restaurantId, restaurantName = "Rose Garden Restaurant", categoryId = "Pizza", price = 120.0, imageRes = R.drawable.food_bowl, promoTag = "GIẢM 10%"),
                        FoodItem(id = "10", name = "Pepperoni Feast", restaurantId = restaurantId, restaurantName = "Rose Garden Restaurant", categoryId = "Pizza", price = 150.0, imageRes = R.drawable.food_bowl, promoTag = "HOT")
                    )
                    Pair(restaurant, items)
                }
                "2" -> {
                    val restaurant = Restaurant(
                        id = restaurantId,
                        name = "KFC - Ho Chi Minh",
                        description = "World famous fried chicken and fast food favorites.",
                        tags = listOf("Fast Food", "Fried Chicken"),
                        rating = 4.5f,
                        deliveryFee = 1.5,
                        deliveryTime = "15 min",
                        imageRes = R.drawable.food_bowl
                    )
                    val items = listOf(
                        FoodItem(id = "4", name = "BBQ Special", restaurantId = restaurantId, restaurantName = "KFC - Ho Chi Minh", categoryId = "Burger", price = 45.0, imageRes = R.drawable.food_bowl, promoTag = "HOT"),
                        FoodItem(id = "7", name = "Beef Pastrami", restaurantId = restaurantId, restaurantName = "KFC - Ho Chi Minh", categoryId = "Sandwich", price = 50.0, imageRes = R.drawable.food_bowl, promoTag = "NEW"),
                        FoodItem(id = "8", name = "Veggie Supreme", restaurantId = restaurantId, restaurantName = "KFC - Ho Chi Minh", categoryId = "Sandwich", price = 28.0, imageRes = R.drawable.food_bowl, promoTag = "HEALTHY")
                    )
                    Pair(restaurant, items)
                }
                "3" -> {
                    val restaurant = Restaurant(
                        id = restaurantId,
                        name = "Pizza Hut Deli",
                        description = "Premium pizzas and Italian specialties delivered hot and fresh.",
                        tags = listOf("Pizza", "Italian", "Pasta"),
                        rating = 4.8f,
                        deliveryFee = 0.0,
                        deliveryTime = "30 min",
                        imageRes = R.drawable.food_bowl
                    )
                    val items = listOf(
                        FoodItem(id = "11", name = "Seafood Black Pepper", restaurantId = restaurantId, restaurantName = "Pizza Hut Deli", categoryId = "Pizza", price = 180.0, imageRes = R.drawable.food_bowl, promoTag = "PROMOTION"),
                        FoodItem(id = "12", name = "Hawaiian Classic", restaurantId = restaurantId, restaurantName = "Pizza Hut Deli", categoryId = "Pizza", price = 140.0, imageRes = R.drawable.food_bowl, promoTag = "FREESHIP")
                    )
                    Pair(restaurant, items)
                }
                else -> {
                    // Default fallback
                    val restaurant = Restaurant(
                        id = restaurantId,
                        name = "Spicy Restaurant",
                        description = "Maecenas sed diam eget risus varius blandit sit amet non magna.",
                        tags = listOf("Burger", "Chicken", "Rice", "Wings"),
                        rating = 4.7f,
                        deliveryFee = 0.0,
                        deliveryTime = "20 min",
                        imageRes = R.drawable.food_bowl
                    )
                    val items = listOf(
                        FoodItem(id = "1", name = "Burger Ferguson", restaurantId = restaurantId, restaurantName = "Spicy Restaurant", categoryId = "Burger", price = 40.0, imageRes = R.drawable.food_bowl, promoTag = "PROMOTION"),
                        FoodItem(id = "2", name = "Rockin' Burgers", restaurantId = restaurantId, restaurantName = "Spicy Restaurant", categoryId = "Burger", price = 40.0, imageRes = R.drawable.food_bowl, promoTag = "GIẢM 20%"),
                        FoodItem(id = "3", name = "Egg Burger", restaurantId = restaurantId, restaurantName = "Spicy Restaurant", categoryId = "Burger", price = 35.0, imageRes = R.drawable.food_bowl, promoTag = "FREESHIP"),
                        FoodItem(id = "5", name = "Club Sandwich", restaurantId = restaurantId, restaurantName = "Spicy Restaurant", categoryId = "Sandwich", price = 30.0, imageRes = R.drawable.food_bowl, promoTag = "BÁN CHẠY"),
                        FoodItem(id = "9", name = "Margherita Pizza", restaurantId = restaurantId, restaurantName = "Spicy Restaurant", categoryId = "Pizza", price = 120.0, imageRes = R.drawable.food_bowl, promoTag = "GIẢM 10%")
                    )
                    Pair(restaurant, items)
                }
            }

            _state.update {
                it.copy(
                    restaurant = mockRestaurant,
                    foodItems = mockFoodItems,
                    categorizedFoodItem = mockFoodItems.groupBy { item -> item.categoryId },
                    isLoading = false
                )
            }
        }
    }
}
