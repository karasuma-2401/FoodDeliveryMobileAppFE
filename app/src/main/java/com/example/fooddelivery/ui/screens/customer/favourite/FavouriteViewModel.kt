package com.example.fooddelivery.ui.screens.customer.favourite

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fooddelivery.domain.model.Restaurant
import com.example.fooddelivery.domain.repository.RestaurantRepository
import com.example.fooddelivery.domain.repository.UserRepository
import com.example.fooddelivery.domain.usecase.EnrichRestaurantsWithVoucherBadgesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
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
class FavouriteViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val restaurantRepository: RestaurantRepository,
    private val enrichRestaurantsWithVoucherBadgesUseCase: EnrichRestaurantsWithVoucherBadgesUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(FavouriteState())
    val state: StateFlow<FavouriteState> = _state.asStateFlow()

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
            userRepository.getFavoriteRestaurants(limit = 50, offset = 0)
                .onSuccess { restaurants ->
                    _state.update {
                        it.copy(
                            favouriteRestaurants = restaurants,
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                    enrichVoucherBadges(restaurants)
                }
                .onFailure { error ->
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Failed to load favourites"
                        ) 
                    }
                }
        }
    }

    private fun toggleFavourite(restaurantId: String) {
        val currentList = _state.value.favouriteRestaurants
        val restaurantIdInt = restaurantId.toIntOrNull() ?: return
        
        viewModelScope.launch {
            // Optimistic UI update: remove from list immediately
            val updatedList = currentList.filterNot { it.id == restaurantId }
            _state.update { it.copy(favouriteRestaurants = updatedList) }

            restaurantRepository.toggleFavorite(restaurantIdInt)
                .onFailure { error ->
                    // Rollback if failed
                    _state.update { 
                        it.copy(
                            favouriteRestaurants = currentList,
                            errorMessage = error.message ?: "Failed to update favourites"
                        ) 
                    }
                }
        }
    }

    private fun enrichVoucherBadges(restaurants: List<Restaurant>) {
        if (restaurants.isEmpty()) return
        viewModelScope.launch {
            val enriched = enrichRestaurantsWithVoucherBadgesUseCase(restaurants)
            _state.update { current ->
                if (current.favouriteRestaurants.map { it.id } != restaurants.map { it.id }) {
                    current
                } else {
                    current.copy(favouriteRestaurants = enriched)
                }
            }
        }
    }
}
