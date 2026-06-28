package com.example.fooddelivery.ui.screens.customer.home.restaurant

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fooddelivery.domain.location.LocationTracker
import com.example.fooddelivery.domain.model.Category
import com.example.fooddelivery.domain.model.Restaurant
import com.example.fooddelivery.domain.model.RestaurantListQuery
import com.example.fooddelivery.domain.model.RestaurantMinRatingFilter
import com.example.fooddelivery.domain.model.RestaurantSortOption
import com.example.fooddelivery.domain.repository.DeliveryLocationRepository
import com.example.fooddelivery.domain.repository.RestaurantRepository
import com.example.fooddelivery.domain.usecase.EnrichRestaurantsWithVoucherBadgesUseCase
import com.example.fooddelivery.domain.usecase.GetCategoriesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AllRestaurantsState(
    val restaurants: List<Restaurant> = emptyList(),
    val categories: List<Category> = emptyList(),
    val keywordInput: String = "",
    val currentSortOption: RestaurantSortOption = RestaurantSortOption.NEWEST,
    val minRatingFilter: RestaurantMinRatingFilter = RestaurantMinRatingFilter.ANY,
    val selectedCategoryId: String? = null,
    val locationLabel: String = "",
    val hasLocation: Boolean = false,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isPaginating: Boolean = false,
    val isEndReached: Boolean = false,
    val showFilterSheet: Boolean = false,
    val errorMessage: String? = null,
) {
    val activeFilterCount: Int
        get() {
            var count = 0
            if (minRatingFilter != RestaurantMinRatingFilter.ANY) count++
            if (selectedCategoryId != null) count++
            if (keywordInput.isNotBlank()) count++
            if (currentSortOption != RestaurantSortOption.NEWEST) count++
            return count
        }
}

sealed interface AllRestaurantsEvent {
    object Refresh : AllRestaurantsEvent
    object LoadMore : AllRestaurantsEvent
    data class KeywordChanged(val keyword: String) : AllRestaurantsEvent
    data class SortChanged(val option: RestaurantSortOption) : AllRestaurantsEvent
    data class MinRatingChanged(val filter: RestaurantMinRatingFilter) : AllRestaurantsEvent
    data class CategorySelected(val categoryId: String?) : AllRestaurantsEvent
    data class ApplyFilters(
        val sort: RestaurantSortOption,
        val minRating: RestaurantMinRatingFilter,
    ) : AllRestaurantsEvent
    data class ToggleFilterSheet(val show: Boolean) : AllRestaurantsEvent
    data class ToggleFavorite(val restaurantId: String) : AllRestaurantsEvent
    object ErrorDismissed : AllRestaurantsEvent
}

@HiltViewModel
class AllRestaurantsViewModel @Inject constructor(
    private val restaurantRepository: RestaurantRepository,
    private val enrichRestaurantsWithVoucherBadgesUseCase: EnrichRestaurantsWithVoucherBadgesUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val deliveryLocationRepository: DeliveryLocationRepository,
    private val locationTracker: LocationTracker,
) : ViewModel() {

    private val _state = MutableStateFlow(AllRestaurantsState())
    val state: StateFlow<AllRestaurantsState> = _state.asStateFlow()

    private val pageSize = 10
    private var searchJob: Job? = null
    private var resolvedLat: Double? = null
    private var resolvedLng: Double? = null

    init {
        observeDeliveryLocation()
        loadCategories()
        viewModelScope.launch {
            deliveryLocationRepository.refreshAddresses()
            resolveCoordinates()
            loadRestaurants(reset = true)
        }
    }

    fun onEvent(event: AllRestaurantsEvent) {
        when (event) {
            AllRestaurantsEvent.Refresh -> loadRestaurants(reset = true, isRefresh = true)
            AllRestaurantsEvent.LoadMore -> loadMoreRestaurants()
            is AllRestaurantsEvent.KeywordChanged -> handleKeywordChange(event.keyword)
            is AllRestaurantsEvent.SortChanged -> {
                if (event.option == RestaurantSortOption.DISTANCE && !_state.value.hasLocation) {
                    _state.update {
                        it.copy(errorMessage = "Select a delivery address to sort by distance")
                    }
                    return
                }
                _state.update { it.copy(currentSortOption = event.option) }
                loadRestaurants(reset = true)
            }
            is AllRestaurantsEvent.MinRatingChanged -> {
                _state.update { it.copy(minRatingFilter = event.filter) }
                loadRestaurants(reset = true)
            }
            is AllRestaurantsEvent.CategorySelected -> {
                val newId = if (_state.value.selectedCategoryId == event.categoryId) null else event.categoryId
                _state.update { it.copy(selectedCategoryId = newId) }
                loadRestaurants(reset = true)
            }
            is AllRestaurantsEvent.ApplyFilters -> {
                if (event.sort == RestaurantSortOption.DISTANCE && !_state.value.hasLocation) {
                    _state.update {
                        it.copy(errorMessage = "Select a delivery address to sort by distance")
                    }
                    return
                }
                _state.update {
                    it.copy(
                        currentSortOption = event.sort,
                        minRatingFilter = event.minRating,
                        showFilterSheet = false,
                    )
                }
                loadRestaurants(reset = true)
            }
            is AllRestaurantsEvent.ToggleFilterSheet -> {
                _state.update { it.copy(showFilterSheet = event.show) }
            }
            is AllRestaurantsEvent.ToggleFavorite -> toggleFavorite(event.restaurantId)
            AllRestaurantsEvent.ErrorDismissed -> _state.update { it.copy(errorMessage = null) }
        }
    }

    private fun observeDeliveryLocation() {
        viewModelScope.launch {
            deliveryLocationRepository.deliveryLocation.collectLatest { location ->
                _state.update {
                    it.copy(
                        locationLabel = location.selectedAddressLabel,
                        hasLocation = location.lat != null && location.lng != null,
                    )
                }
            }
        }
        viewModelScope.launch {
            deliveryLocationRepository.deliveryLocation
                .map { it.selectedAddressId }
                .distinctUntilChanged()
                .drop(1)
                .collectLatest {
                    resolveCoordinates()
                    loadRestaurants(reset = true)
                }
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            getCategoriesUseCase(limit = 50)
                .onSuccess { categories ->
                    _state.update { it.copy(categories = categories) }
                }
        }
    }

    private fun handleKeywordChange(keyword: String) {
        _state.update { it.copy(keywordInput = keyword) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(400L)
            loadRestaurants(reset = true)
        }
    }

    private suspend fun resolveCoordinates() {
        val location = deliveryLocationRepository.deliveryLocation.value
        val addressLat = location.lat
        val addressLng = location.lng
        if (addressLat != null && addressLng != null) {
            resolvedLat = addressLat
            resolvedLng = addressLng
            return
        }
        val gps = locationTracker.getCurrentLocation()
        resolvedLat = addressLat ?: gps?.latitude
        resolvedLng = addressLng ?: gps?.longitude
        _state.update {
            it.copy(hasLocation = resolvedLat != null && resolvedLng != null)
        }
    }

    private fun loadRestaurants(reset: Boolean, isRefresh: Boolean = false) {
        viewModelScope.launch {
            val currentState = _state.value
            if (currentState.isLoading && !isRefresh) return@launch

            resolveCoordinates()

            if (reset) {
                _state.update {
                    it.copy(
                        restaurants = if (isRefresh) it.restaurants else emptyList(),
                        isLoading = !isRefresh,
                        isRefreshing = isRefresh,
                        isEndReached = false,
                        errorMessage = null,
                    )
                }
            }

            val query = buildQuery(offset = if (reset) 0 else currentState.restaurants.size)

            restaurantRepository.getRestaurants(query)
                .onSuccess { data ->
                    _state.update { state ->
                        state.copy(
                            restaurants = if (reset) data else state.restaurants + data,
                            isLoading = false,
                            isRefreshing = false,
                            isPaginating = false,
                            isEndReached = data.size < pageSize,
                        )
                    }
                    enrichVoucherBadges(data)
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            isPaginating = false,
                            errorMessage = error.message ?: "Failed to load restaurants",
                        )
                    }
                }
        }
    }

    private fun loadMoreRestaurants() {
        val currentState = _state.value
        if (currentState.isPaginating || currentState.isEndReached || currentState.isLoading) return

        viewModelScope.launch {
            _state.update { it.copy(isPaginating = true) }
            resolveCoordinates()

            val query = buildQuery(offset = currentState.restaurants.size)

            restaurantRepository.getRestaurants(query)
                .onSuccess { newData ->
                    _state.update { state ->
                        state.copy(
                            restaurants = state.restaurants + newData,
                            isPaginating = false,
                            isEndReached = newData.size < pageSize,
                        )
                    }
                    enrichVoucherBadges(newData)
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isPaginating = false,
                            errorMessage = error.message ?: "Failed to load more restaurants",
                        )
                    }
                }
        }
    }

    private fun buildQuery(offset: Int): RestaurantListQuery {
        val state = _state.value
        val sort = state.currentSortOption
        val effectiveSort = if (sort == RestaurantSortOption.DISTANCE && !state.hasLocation) {
            RestaurantSortOption.NEWEST
        } else {
            sort
        }

        return RestaurantListQuery(
            limit = pageSize,
            offset = offset,
            keyword = state.keywordInput.trim().takeIf { it.isNotEmpty() },
            categoryId = state.selectedCategoryId?.toIntOrNull(),
            latitude = resolvedLat,
            longitude = resolvedLng,
            minRating = state.minRatingFilter.value,
            sort = effectiveSort,
        )
    }

    private fun toggleFavorite(restaurantId: String) {
        val restaurantIdInt = restaurantId.toIntOrNull() ?: return
        val snapshot = _state.value.restaurants

        viewModelScope.launch {
            val target = snapshot.find { it.id == restaurantId } ?: return@launch
            val optimistic = target.copy(isLiked = !target.isLiked)
            _state.update { state ->
                state.copy(
                    restaurants = state.restaurants.map { restaurant ->
                        if (restaurant.id == restaurantId) optimistic else restaurant
                    }
                )
            }

            restaurantRepository.toggleFavorite(restaurantIdInt)
                .onSuccess { result ->
                    _state.update { state ->
                        state.copy(
                            restaurants = state.restaurants.map { restaurant ->
                                if (restaurant.id == restaurantId) {
                                    restaurant.copy(isLiked = result.isLiked)
                                } else {
                                    restaurant
                                }
                            }
                        )
                    }
                }
                .onFailure { error ->
                    _state.update { state ->
                        state.copy(
                            restaurants = snapshot,
                            errorMessage = error.message ?: "Failed to update favourite",
                        )
                    }
                }
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
