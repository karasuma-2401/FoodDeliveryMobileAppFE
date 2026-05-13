package com.example.fooddelivery.ui.screens.profile.favourite

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fooddelivery.domain.model.Restaurant
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FavouriteState(
    val favouriteRestaurants: List<Restaurant> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

sealed interface FavouriteEvent {
    object LoadFavourites : FavouriteEvent
    data class ToggleFavourite(val restaurantId: String) : FavouriteEvent
    object ErrorDismissed : FavouriteEvent
}

@HiltViewModel
class FavouriteViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(FavouriteState())
    val state: StateFlow<FavouriteState> = _state.asStateFlow()
    private var mockFavourites = mutableListOf(
        Restaurant(
            id = "1",
            name = "Rose Garden Restaurant",
            tags = listOf("Burger", "Chicken", "Rice"),
            rating = 4.7f,
            deliveryFee = 2.0,
            imageRes = com.example.fooddelivery.R.drawable.food_bowl
        ),
        Restaurant(
            id = "3",
            name = "Pizza Hut Deli",
            tags = listOf("Pizza", "Italian"),
            rating = 4.8f,
            deliveryFee = 0.0,
            imageRes = com.example.fooddelivery.R.drawable.food_bowl
        )
    )

    init {
        onEvent(FavouriteEvent.LoadFavourites)
    }

    fun onEvent(event: FavouriteEvent) {
        when (event) {
            FavouriteEvent.LoadFavourites -> loadFavourites()
            is FavouriteEvent.ToggleFavourite -> toggleFavourite(event.restaurantId)
            FavouriteEvent.ErrorDismissed -> _state.update { it.copy(errorMessage = null) }
        }
    }

    private fun loadFavourites() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            delay(800)
            _state.update { 
                it.copy(
                    favouriteRestaurants = mockFavourites.toList(),
                    isLoading = false 
                ) 
            }
        }
    }

    private fun toggleFavourite(restaurantId: String) {
        val currentList = _state.value.favouriteRestaurants
        val restaurantToRemove = currentList.find { it.id == restaurantId }
        
        if (restaurantToRemove != null) {
            val updatedList = currentList.filterNot { it.id == restaurantId }
            _state.update { it.copy(favouriteRestaurants = updatedList) }

            viewModelScope.launch {
                val result = simulateToggleApi(restaurantId)
                if (result.isFailure) {
                    _state.update { 
                        it.copy(
                            favouriteRestaurants = currentList,
                            errorMessage = "Failed to update favourites. Please try again."
                        ) 
                    }
                } else {
                    mockFavourites.removeAll { it.id == restaurantId }
                }
            }
        }
    }

    private suspend fun simulateToggleApi(id: String): Result<Unit> {
        delay(1000)
        return if (Math.random() > 0.1) Result.success(Unit) else Result.failure(Exception("API Error"))
    }
}
