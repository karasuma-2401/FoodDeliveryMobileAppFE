package com.example.fooddelivery.ui.screens.home.restaurant

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fooddelivery.R
import com.example.fooddelivery.domain.model.Restaurant
import com.example.fooddelivery.domain.model.RestaurantSortOption
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AllRestaurantsState(
    val restaurants: List<Restaurant> = emptyList(),
    val isLoading: Boolean = false,
    val isPaginating: Boolean = false,
    val page: Int = 1,
    val isEndReached: Boolean = false ,
    val currentSortOption: RestaurantSortOption = RestaurantSortOption.RATING,
    val showSortSheet: Boolean = false
)
sealed interface AllRestaurantsEvent {
    object LoadMore: AllRestaurantsEvent
    data class SortChanged(val option: RestaurantSortOption) : AllRestaurantsEvent
    data class ToggleSortSheet(val show: Boolean) : AllRestaurantsEvent
}
@HiltViewModel
class AllRestaurantsViewModel @Inject constructor() : ViewModel() {
    private val _state = MutableStateFlow(AllRestaurantsState())
    val state: StateFlow<AllRestaurantsState> = _state.asStateFlow()

    init {
        loadInitialRestaurants()
    }
    fun onEvent(event: AllRestaurantsEvent) {
        when(event) {
            is AllRestaurantsEvent.LoadMore -> loadMoreRestaurants()
            is AllRestaurantsEvent.SortChanged -> applySorting(event.option)
            is AllRestaurantsEvent.ToggleSortSheet -> _state.update { it.copy(showSortSheet = event.show) }
        }
    }
    private fun loadInitialRestaurants() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            delay(800)
            val initialData = getMockRestaurants(1)
            _state.update {
                it.copy(
                    restaurants = initialData,
                    isLoading = false,
                    page = 1,
                    isEndReached = initialData.isEmpty()
                )
            }
        }
    }
    private fun loadMoreRestaurants() {
        val currentState = _state.value
        if (currentState.isPaginating || currentState.isEndReached || currentState.isLoading) return

        viewModelScope.launch {
            _state.update { it.copy(isPaginating = true) }
            delay(800)

            val nextPage = currentState.page + 1
            val newData = getMockRestaurants(nextPage)

            _state.update { state ->
                state.copy(
                    restaurants = state.restaurants + newData,
                    page = nextPage,
                    isPaginating = false,
                    isEndReached = newData.isEmpty()
                )
            }
        }
    }
    private fun applySorting(option: RestaurantSortOption) {
        val currentList = _state.value.restaurants
        val sortedList = when(option) {
            RestaurantSortOption.RATING -> currentList.sortedByDescending { it.rating }
            RestaurantSortOption.DELIVERY_FEE -> currentList.sortedBy { it.deliveryFee }
        }
        _state.update { it.copy(
            restaurants = sortedList,
            currentSortOption = option,
            showSortSheet = false
        ) }
    }

    private fun getMockRestaurants(page: Int): List<Restaurant> {
        if (page > 3) return emptyList()
        return List(5) { index ->
            val id = "p${page}_$index"
            Restaurant(
                id = id,
                name = "Restaurant Page $page - #$index",
                tags = listOf("Fast Food", "Burger"),
                rating = 4.0f + (index * 0.1f),
                deliveryFee = if (index % 2 == 0) 0.0 else 1.5,
                imageRes = R.drawable.food_bowl,
                promoTags = if (index % 3 == 0) listOf("PROMO", "Freeship") else emptyList()
            )
        }
    }
}