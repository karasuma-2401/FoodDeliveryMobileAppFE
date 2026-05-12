package com.example.fooddelivery.ui.screens.home.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fooddelivery.R
import com.example.fooddelivery.domain.model.FoodItem
import com.example.fooddelivery.domain.model.Restaurant
import com.example.fooddelivery.domain.repository.CartRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchState(
    val searchQuery: String = "",
    val recentKeyWords: List<String> = listOf("Burger", "Pizza"),
    val suggestedRestaurants: List<Restaurant> = emptyList(),
    val popularFood: List<FoodItem> = emptyList(),
    val cartItemCount: Int = 0,
    val isLoading: Boolean = false,
    val selectedLocation: String = "Home",
    val availableLocations: List<String> = listOf("Home", "Work", "Other")
)

sealed interface SearchEvent {
    data class QueryChanged(val query: String): SearchEvent
    data class KeywordClicked(val keyword: String): SearchEvent
    object ClearSearch: SearchEvent
    object LoadSearchData: SearchEvent
    data class LocationSelected(val location: String) : SearchEvent
}
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val cartRepository: CartRepository
) : ViewModel() {
    private val _state = MutableStateFlow(SearchState())
    val state: StateFlow<SearchState> = _state.asStateFlow()

    private var allRestaurants: List<Restaurant> = emptyList()
    private var allFoodItems: List<FoodItem> = emptyList()

    private var searchJob: Job? = null

    init {
        onEvent(SearchEvent.LoadSearchData)
        observeCart()
    }
    private fun observeCart() {
        viewModelScope.launch {
            cartRepository.getCartItems().collectLatest { items ->
                val totalCount = items.sumOf { it.quantity }
                _state.update { it.copy(cartItemCount = totalCount) }
            }
        }
    }

    fun onEvent(event: SearchEvent) {
        when(event) {
            is SearchEvent.QueryChanged -> {
                _state.update { it.copy(searchQuery = event.query, isLoading = true) }
                searchJob?.cancel()
                searchJob = viewModelScope.launch {
                    delay(500L)
                    filterData(event.query)

                    if (event.query.isNotBlank() && !state.value.recentKeyWords.contains(event.query)) {
                        _state.update {
                            val newList = (listOf(event.query) + it.recentKeyWords).take(5)
                            it.copy(recentKeyWords = newList)
                        }
                    }
                }
            }
            is SearchEvent.KeywordClicked -> {
                _state.update { it.copy(searchQuery = event.keyword, isLoading = true) }
                filterData(event.keyword)
            }
            SearchEvent.ClearSearch -> {
                searchJob?.cancel()
                _state.update { it.copy(searchQuery = "") }
                filterData("")
            }
            SearchEvent.LoadSearchData -> loadInitialData()
            is SearchEvent.LocationSelected -> {
                _state.update { it.copy(selectedLocation = event.location) }
            }
        }
    }

    private fun filterRestaurants(query: String): List<Restaurant> {
        if (query.isEmpty()) return allRestaurants.take(3)
        return allRestaurants.filter { restaurant ->
            restaurant.name.contains(query, ignoreCase = true) ||
            restaurant.tags.any { it.contains(query, ignoreCase = true) }
        }
    }

    private fun filterPopularFood(query: String): List<FoodItem> {
        if (query.isEmpty()) return allFoodItems.take(4)
        return allFoodItems.filter { food ->
            food.name.contains(query, ignoreCase = true) ||
            food.restaurantName.contains(query, ignoreCase = true)
        }
    }

    private fun filterData(query: String) {
        _state.update {
            it.copy(
                suggestedRestaurants = filterRestaurants(query),
                popularFood = filterPopularFood(query),
                isLoading = false
            )
        }
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            delay(500)

            allRestaurants = listOf(
                Restaurant(id = "1", name = "Spicy Restaurant", tags = listOf("Burger", "Chicken"), rating = 4.7f, deliveryFee = 0.0, imageRes = R.drawable.food_bowl),
                Restaurant(id = "2", name = "KFC - Ho Chi Minh", tags = listOf("Fast Food", "Fried Chicken"), rating = 4.5f, deliveryFee = 1.5, imageRes = R.drawable.food_bowl),
                Restaurant(id = "3", name = "Pizza Hut Deli", tags = listOf("Pizza", "Italian"), rating = 4.8f, deliveryFee = 0.0, imageRes = R.drawable.food_bowl)
            )

            allFoodItems = listOf(
                FoodItem(id = "1", name = "Burger Ferguson", restaurantId = "1", restaurantName = "Spicy Restaurant", price = 15.0, imageRes = R.drawable.food_bowl, promoTag = "PROMOTION"),
                FoodItem(id = "2", name = "Rockin' Burgers", restaurantId = "4", restaurantName = "Cafecafachino", price = 12.0, imageRes = R.drawable.food_bowl, promoTag = "GIẢM 20%"),
                FoodItem(id = "5", name = "Margherita Pizza", restaurantId = "3", restaurantName = "Pizza Hut Deli", price = 20.0, imageRes = R.drawable.food_bowl)
            )
            filterData("")
        }
    }
}
