package com.example.fooddelivery.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fooddelivery.R
import com.example.fooddelivery.domain.model.Category
import com.example.fooddelivery.domain.model.Restaurant
import com.example.fooddelivery.domain.model.User
import com.example.fooddelivery.domain.repository.CartRepository
import com.example.fooddelivery.domain.usecase.GetUserProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeState(
    val user: User = User(fullName = "Halal"),
    val categories: List<Category> = emptyList(),
    val restaurants: List<Restaurant> = emptyList(),
    val cartItemCount: Int = 0,
    val selectedLocation: String = "Halal Lab office",
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

sealed interface HomeEvent {
    object LoadHomeData : HomeEvent
    object CartClicked : HomeEvent
    object MenuClicked : HomeEvent
    object LocationClicked : HomeEvent
    data class CategoryClicked(val categoryId: String) : HomeEvent
    data class RestaurantClicked(val restaurantId: String) : HomeEvent
    object SeeAllCategoriesClicked : HomeEvent
    object SeeAllRestaurantsClicked : HomeEvent
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val cartRepository: CartRepository
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    private var fullCategories: List<Category> = emptyList()
    private var fullRestaurants: List<Restaurant> = emptyList()
    
    private var searchJob: Job? = null

    init {
        onEvent(HomeEvent.LoadHomeData)
        observeCart()
    }

    private fun observeCart() {
        viewModelScope.launch {
            cartRepository.cartItems.collectLatest { items ->
                val totalCount = items.sumOf { it.quantity }
                _state.update { it.copy(cartItemCount = totalCount) }
            }
        }
    }

    fun onEvent(event: HomeEvent) {
        when (event) {
            HomeEvent.LoadHomeData -> loadData()
            HomeEvent.CartClicked -> { /* logic */ }
            HomeEvent.MenuClicked -> { /* logic */ }
            HomeEvent.LocationClicked -> { /* logic */ }
            is HomeEvent.CategoryClicked -> { /* logic */ }
            is HomeEvent.RestaurantClicked -> { /* logic */ }
            HomeEvent.SeeAllCategoriesClicked -> { /* logic */ }
            HomeEvent.SeeAllRestaurantsClicked -> { /* logic */ }
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            delay(1000)

            fullCategories = listOf(
                Category(id = "1", name = "Pizza", imageRes = R.drawable.food_bowl, startingPrice = 70.0, promoText = "Giảm 20%"),
                Category(id = "2", name = "Burger", imageRes = R.drawable.food_bowl, startingPrice = 50.0, promoText = "PROMO"),
                Category(id = "3", name = "Pasta", imageRes = R.drawable.food_bowl, startingPrice = 60.0),
                Category(id = "4", name = "Drink", imageRes = R.drawable.food_bowl, startingPrice = 20.0),
                Category(id = "5", name = "Chicken", imageRes = R.drawable.food_bowl, startingPrice = 45.0)
            )
            
            fullRestaurants = listOf(
                Restaurant(
                    id = "1",
                    name = "Rose Garden Restaurant",
                    tags = listOf("Burger", "Chicken", "Rice", "Wings"),
                    rating = 4.7f,
                    deliveryFee = 0.0,
                    deliveryTime = "20 min",
                    imageRes = R.drawable.food_bowl,
                    promoTags = listOf("PROMO", "Freeship")
                ),
                Restaurant(
                    id = "2",
                    name = "KFC - Ho Chi Minh",
                    tags = listOf("Fast Food", "Fried Chicken"),
                    rating = 4.5f,
                    deliveryFee = 1.5,
                    deliveryTime = "15 min",
                    imageRes = R.drawable.food_bowl,
                    promoTags = listOf("Giảm 50%")
                ),
                Restaurant(
                    id = "3",
                    name = "Pizza Hut Deli",
                    tags = listOf("Pizza", "Italian", "Pasta"),
                    rating = 4.8f,
                    deliveryFee = 0.0,
                    deliveryTime = "30 min",
                    imageRes = R.drawable.food_bowl
                )
            )

            _state.update { it.copy(
                categories = fullCategories,
                restaurants = fullRestaurants,
                isLoading = false
            ) }
        }
    }
}
