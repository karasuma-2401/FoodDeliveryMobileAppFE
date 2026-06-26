package com.example.fooddelivery.ui.screens.customer.home.restaurant

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fooddelivery.domain.model.Restaurant
import com.example.fooddelivery.domain.model.RestaurantSortOption
import com.example.fooddelivery.domain.repository.RestaurantRepository
import com.example.fooddelivery.domain.usecase.EnrichRestaurantsWithVoucherBadgesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
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
    val isEndReached: Boolean = false,
    val currentSortOption: RestaurantSortOption = RestaurantSortOption.RATING,
    val showSortSheet: Boolean = false,
    val errorMessage: String? = null
)

sealed interface AllRestaurantsEvent {
    object LoadMore : AllRestaurantsEvent
    data class SortChanged(val option: RestaurantSortOption) : AllRestaurantsEvent
    data class ToggleSortSheet(val show: Boolean) : AllRestaurantsEvent
}

@HiltViewModel
class AllRestaurantsViewModel @Inject constructor(
    private val restaurantRepository: RestaurantRepository,
    private val enrichRestaurantsWithVoucherBadgesUseCase: EnrichRestaurantsWithVoucherBadgesUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(AllRestaurantsState())
    val state: StateFlow<AllRestaurantsState> = _state.asStateFlow()

    private val pageSize = 10

    init {
        loadInitialRestaurants()
    }

    fun onEvent(event: AllRestaurantsEvent) {
        when (event) {
            is AllRestaurantsEvent.LoadMore -> loadMoreRestaurants()
            is AllRestaurantsEvent.SortChanged -> applySorting(event.option)
            is AllRestaurantsEvent.ToggleSortSheet -> _state.update { it.copy(showSortSheet = event.show) }
        }
    }

    private fun loadInitialRestaurants() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            restaurantRepository.getRestaurants(limit = pageSize, offset = 0)
                .onSuccess { data ->
                    val sorted = sortList(data, _state.value.currentSortOption)
                    _state.update {
                        it.copy(
                            restaurants = sorted,
                            isLoading = false,
                            isEndReached = data.size < pageSize
                        )
                    }
                    enrichVoucherBadges(sorted)
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(isLoading = false, errorMessage = error.message ?: "Failed to load restaurants")
                    }
                }
        }
    }

    private fun loadMoreRestaurants() {
        val currentState = _state.value
        if (currentState.isPaginating || currentState.isEndReached || currentState.isLoading) return

        viewModelScope.launch {
            _state.update { it.copy(isPaginating = true) }
            val offset = currentState.restaurants.size

            restaurantRepository.getRestaurants(limit = pageSize, offset = offset)
                .onSuccess { newData ->
                    _state.update { state ->
                        val combinedList = state.restaurants + newData
                        val sorted = sortList(combinedList, state.currentSortOption)
                        state.copy(
                            restaurants = sorted,
                            isPaginating = false,
                            isEndReached = newData.size < pageSize
                        )
                    }
                    enrichVoucherBadges(newData)
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isPaginating = false,
                            errorMessage = error.message ?: "Failed to load more restaurants"
                        )
                    }
                }
        }
    }

    private fun applySorting(option: RestaurantSortOption) {
        val currentList = _state.value.restaurants
        _state.update {
            it.copy(
                restaurants = sortList(currentList, option),
                currentSortOption = option,
                showSortSheet = false
            )
        }
    }

    private fun sortList(list: List<Restaurant>, option: RestaurantSortOption): List<Restaurant> {
        return when (option) {
            RestaurantSortOption.RATING -> list.sortedByDescending { it.rating }
            RestaurantSortOption.DELIVERY_FEE -> list.sortedBy { it.deliveryFee }
        }
    }

    private fun enrichVoucherBadges(restaurants: List<Restaurant>) {
        if (restaurants.isEmpty()) return
        viewModelScope.launch {
            val enrichedBatch = enrichRestaurantsWithVoucherBadgesUseCase(restaurants)
            val enrichedById = enrichedBatch.associateBy { it.id }
            _state.update { current ->
                current.copy(
                    restaurants = current.restaurants.map { restaurant ->
                        enrichedById[restaurant.id] ?: restaurant
                    }
                )
            }
        }
    }
}
