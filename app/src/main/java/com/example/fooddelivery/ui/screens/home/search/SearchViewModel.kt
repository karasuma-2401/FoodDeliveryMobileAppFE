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
//    private fun updateSearchQuery(query: String) {
//        _state.update { it.copy(searchQuery = query) }
//
//        searchJob?.cancel()
//        searchJob = viewModelScope.launch {
//            delay(250)
//            performSearch(query)
//        }
//    }
//
//    private fun performSearch(query: String) {
//        if (query.isBlank()) {
//            _state.update { it.copy(
//                categories = fullCategories,
//                restaurants = fullRestaurants
//            ) }
//            return
//        }
//
//        val filteredCategories = fullCategories.filter {
//            it.name.contains(query, ignoreCase = true)
//        }
//        val filteredRestaurants = fullRestaurants.filter {
//            it.name.contains(query, ignoreCase = true) ||
//                    it.tags.any { tag -> tag.contains(query, ignoreCase = true) }
//        }
//
//        _state.update { it.copy(
//            categories = filteredCategories,
//            restaurants = filteredRestaurants
//        ) }
//    }
    private fun loadInitialData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            val mockRestaurants = listOf(
                Restaurant("1", "Pansi Restaurant", listOf("Asian"), 4.7f, "Free", "20 min", imageRes = R.drawable.food_bowl),
                Restaurant("2", "American Spicy Burger Shop", listOf("Fast Food"), 4.3f, "$2.0", "15 min", imageRes = R.drawable.food_bowl),
                Restaurant("3", "Cafenio Coffee Club", listOf("Coffee"), 4.0f, "$1.0", "10 min", imageRes = R.drawable.food_bowl)
            )

            val mockFood = listOf(
                FoodItem("European Pizza", "Uttora Coffe House", "$70", 4.5f, 100, R.drawable.food_bowl),
                FoodItem("Buffalo Pizza", "Cafenio Coffee Club", "$50", 4.2f, 80, R.drawable.food_bowl)
            )
            _state.update {
                it.copy(
                    suggestedRestaurants = mockRestaurants,
                    popularFood = mockFood,
                    isLoading = false
                )
            }
        }
    }
}
