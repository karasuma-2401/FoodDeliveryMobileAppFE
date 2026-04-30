package com.example.fooddelivery.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fooddelivery.R
import com.example.fooddelivery.domain.model.Category
import com.example.fooddelivery.domain.model.Restaurant
import com.example.fooddelivery.domain.model.User
import com.example.fooddelivery.domain.usecase.GetUserProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeState(
    val user: User = User(fullName = "Halal"),
    val categories: List<Category> = emptyList(),
    val restaurants: List<Restaurant> = emptyList(),
    val cartItemCount: Int = 2,
    val selectedLocation: String = "Halal Lab office",
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

sealed interface HomeEvent {
    object LoadHomeData : HomeEvent
    data class SearchQueryChanged(val query: String) : HomeEvent
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
    private val getUserProfileUseCase: GetUserProfileUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    // Giữ danh sách gốc để phục vụ việc lọc (Search)
    private var fullCategories: List<Category> = emptyList()
    private var fullRestaurants: List<Restaurant> = emptyList()
    
    private var searchJob: Job? = null

    init {
        onEvent(HomeEvent.LoadHomeData)
    }

    fun onEvent(event: HomeEvent) {
        when (event) {
            HomeEvent.LoadHomeData -> loadData()
            is HomeEvent.SearchQueryChanged -> {
                updateSearchQuery(event.query)
            }
            HomeEvent.CartClicked -> { /* logic */ }
            HomeEvent.MenuClicked -> { /* logic */ }
            HomeEvent.LocationClicked -> { /* logic */ }
            is HomeEvent.CategoryClicked -> { /* logic */ }
            is HomeEvent.RestaurantClicked -> { /* logic */ }
            HomeEvent.SeeAllCategoriesClicked -> { /* logic */ }
            HomeEvent.SeeAllRestaurantsClicked -> { /* logic */ }
        }
    }

    private fun updateSearchQuery(query: String) {
        _state.update { it.copy(searchQuery = query) }

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(250)
            performSearch(query)
        }
    }

    private fun performSearch(query: String) {
        if (query.isBlank()) {
            _state.update { it.copy(
                categories = fullCategories,
                restaurants = fullRestaurants
            ) }
            return
        }

        val filteredCategories = fullCategories.filter {
            it.name.contains(query, ignoreCase = true)
        }
        val filteredRestaurants = fullRestaurants.filter {
            it.name.contains(query, ignoreCase = true) ||
            it.tags.any { tag -> tag.contains(query, ignoreCase = true) }
        }

        _state.update { it.copy(
            categories = filteredCategories,
            restaurants = filteredRestaurants
        ) }
    }

    private fun loadData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            delay(1000)

            fullCategories = listOf(
                Category("1", "Pizza", R.drawable.food_bowl, "$70"),
                Category("2", "Burger", R.drawable.food_bowl, "$50"),
                Category("3", "Pasta", R.drawable.food_bowl, "$60"),
                Category("4", "Drink", R.drawable.food_bowl, "$20"),
                Category("5", "Chicken", R.drawable.food_bowl, "$45")
            )
            
            fullRestaurants = listOf(
                Restaurant(
                    id = "1",
                    name = "Rose Garden Restaurant",
                    tags = listOf("Burger", "Chicken", "Rice", "Wings"),
                    rating = 4.7f,
                    deliveryFee = "Free",
                    deliveryTime = "20 min",
                    imageRes = R.drawable.food_bowl
                ),
                Restaurant(
                    id = "2",
                    name = "KFC - Ho Chi Minh",
                    tags = listOf("Fast Food", "Fried Chicken"),
                    rating = 4.5f,
                    deliveryFee = "$1.5",
                    deliveryTime = "15 min",
                    imageRes = R.drawable.food_bowl
                ),
                Restaurant(
                    id = "3",
                    name = "Pizza Hut Deli",
                    tags = listOf("Pizza", "Italian", "Pasta"),
                    rating = 4.8f,
                    deliveryFee = "Free",
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
