package com.example.fooddelivery.ui.screens.home.search

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

data class SearchState(
    val searchQuery: String = "",
    val recentKeyWords: List<String> = listOf("Burger", "Sandwich", "Pizza", "Salad" ),
    val suggestedRestaurants: List<Restaurant> = emptyList(),
    val popularFood: List<FoodItem> = emptyList(),
    val cartItemCount: Int = 2,
    val isLoading: Boolean = false
)

sealed interface SearchEvent {
    data class QueryChanged(val query: String): SearchEvent
    data class KeywordClicked(val keyword: String): SearchEvent
    object ClearSearch: SearchEvent
    object LoadSearchData: SearchEvent
}
@HiltViewModel
class SearchViewModel @Inject constructor() : ViewModel() {
    private val _state = MutableStateFlow(SearchState())
    val state: StateFlow<SearchState> = _state.asStateFlow()

    init {
        onEvent(SearchEvent.LoadSearchData)
    }
    fun onEvent(event: SearchEvent) {
        when(event) {
            is SearchEvent.QueryChanged -> {
                _state.update { it.copy(searchQuery = event.query) }
            }
            is SearchEvent.KeywordClicked -> {
                _state.update { it.copy(searchQuery = event.keyword) }
            }
            SearchEvent.ClearSearch -> {
                _state.update { it.copy(searchQuery = "") }
            }
            SearchEvent.LoadSearchData -> loadInitialData()
        }
    }
    private fun loadInitialData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            val mockRestaurant = Restaurant(
                id = "1",
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
                FoodItem("4", "BBQ Special", "Flame Grills", "Burger", "45", R.drawable.food_bowl, "HOT"),
            )
        }
    }
}
