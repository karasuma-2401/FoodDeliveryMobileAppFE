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

    private var allRestaurants: List<Restaurant> = emptyList()
    private var allFoodItems: List<FoodItem> = emptyList()

    init {
        onEvent(SearchEvent.LoadSearchData)
    }

    fun onEvent(event: SearchEvent) {
        when(event) {
            is SearchEvent.QueryChanged -> {
                _state.update { it.copy(searchQuery = event.query) }
                filterData(event.query)
            }
            is SearchEvent.KeywordClicked -> {
                _state.update { it.copy(searchQuery = event.keyword) }
                filterData(event.keyword)
            }
            SearchEvent.ClearSearch -> {
                _state.update { it.copy(searchQuery = "") }
                filterData("")
            }
            SearchEvent.LoadSearchData -> loadInitialData()
        }
    }

    private fun filterRestaurants(query: String): List<Restaurant> {
        if (query.isEmpty()) return allRestaurants
        return allRestaurants.filter { restaurant ->
            restaurant.name.contains(query, ignoreCase = true) ||
            restaurant.tags.any { it.contains(query, ignoreCase = true) }
        }
    }

    private fun filterPopularFood(query: String): List<FoodItem> {
        if (query.isEmpty()) return allFoodItems
        return allFoodItems.filter { food ->
            food.name.contains(query, ignoreCase = true) ||
            food.category.contains(query, ignoreCase = true)
        }
    }

    private fun filterData(query: String) {
        _state.update {
            it.copy(
                suggestedRestaurants = filterRestaurants(query),
                popularFood = filterPopularFood(query)
            )
        }
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            delay(500)

            allRestaurants = listOf(
                Restaurant(
                    id = "1",
                    name = "Spicy Restaurant",
                    description = "Maecenas sed diam eget risus varius blandit sit amet non magna. Integer posuere erat a ante venenatis dapibus posuere velit aliquet.",
                    tags = listOf("Burger", "Chicken", "Rice", "Wings"),
                    rating = 4.7f,
                    deliveryFee = "Free",
                    deliveryTime = "20 min",
                    imageRes = R.drawable.food_bowl
                ),
                Restaurant(
                    id = "2",
                    name = "KFC - Ho Chi Minh",
                    description = "World famous fried chicken and fast food.",
                    tags = listOf("Fast Food", "Fried Chicken"),
                    rating = 4.5f,
                    deliveryFee = "$1.5",
                    deliveryTime = "15 min",
                    imageRes = R.drawable.food_bowl
                ),
                Restaurant(
                    id = "3",
                    name = "Pizza Hut Deli",
                    description = "Premium pizzas and Italian food.",
                    tags = listOf("Pizza", "Italian", "Pasta"),
                    rating = 4.8f,
                    deliveryFee = "Free",
                    deliveryTime = "30 min",
                    imageRes = R.drawable.food_bowl
                )
            )

            allFoodItems = listOf(
                FoodItem("1", "Burger Ferguson", "Spicy Restaurant", "Burger", "40", R.drawable.food_bowl, "PROMOTION"),
                FoodItem("2", "Rockin' Burgers", "Cafecafachino", "Burger", "40", R.drawable.food_bowl, "GIẢM 20%"),
                FoodItem("3", "Egg Burger", "Spicy Restaurant", "Burger", "35", R.drawable.food_bowl, "FREESHIP"),
                FoodItem("4", "BBQ Special", "Flame Grills", "Burger", "45", R.drawable.food_bowl, "HOT"),
                FoodItem("5", "Margherita Pizza", "Pizza Hut Deli", "Pizza", "120", R.drawable.food_bowl, "GIẢM 10%"),
                FoodItem("6", "Pepperoni Pizza", "Pizza Hut Deli", "Pizza", "150", R.drawable.food_bowl, "HOT")
            )

            _state.update {
                it.copy(
                    suggestedRestaurants = allRestaurants,
                    popularFood = allFoodItems,
                    isLoading = false
                )
            }
        }
    }
}
