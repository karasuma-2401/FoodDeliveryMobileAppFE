package com.example.fooddelivery.ui.screens.home.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fooddelivery.domain.location.LocationTracker
import com.example.fooddelivery.domain.model.Category
import com.example.fooddelivery.domain.model.FoodItem
import com.example.fooddelivery.domain.model.Restaurant
import com.example.fooddelivery.domain.model.SearchHistory
import com.example.fooddelivery.domain.model.SearchSortOption
import com.example.fooddelivery.domain.repository.CartRepository
import com.example.fooddelivery.domain.repository.CategoryRepository
import com.example.fooddelivery.domain.repository.ChatRepository
import com.example.fooddelivery.domain.repository.SearchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
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
    val recentKeyWords: List<SearchHistory> = emptyList(),
    val suggestedRestaurants: List<Restaurant> = emptyList(),
    val popularFood: List<FoodItem> = emptyList(),
    val categories: List<Category> = emptyList(),
    val cartItemCount: Int = 0,
    val unreadMessageCount: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedLocation: String = "Current Location",
    val lat: Double? = null,
    val lng: Double? = null,
    val availableLocations: List<String> = listOf("Home", "Work", "Other"),
    val selectedSort: SearchSortOption? = null,
    val selectedCategoryId: String? = null
)

sealed interface SearchEvent {
    data class QueryChanged(val query: String): SearchEvent
    data class KeywordClicked(val keyword: String): SearchEvent
    object PerformSearch: SearchEvent
    object ClearSearch: SearchEvent
    object LoadSearchData: SearchEvent
    data class LocationSelected(val location: String) : SearchEvent
    data class DeleteHistoryItem(val id: Int) : SearchEvent
    object ClearAllHistory : SearchEvent
    data class SortSelected(val sort: SearchSortOption?) : SearchEvent
    data class CategorySelected(val categoryId: String?) : SearchEvent
}

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val cartRepository: CartRepository,
    private val chatRepository: ChatRepository,
    private val categoryRepository: CategoryRepository,
    private val searchRepository: SearchRepository,
    private val locationTracker: LocationTracker
) : ViewModel() {
    private val _state = MutableStateFlow(SearchState())
    val state: StateFlow<SearchState> = _state.asStateFlow()

    private var searchJob: Job? = null

    init {
        loadInitialData()
        observeCart()
        observeUnreadMessages()
        loadCategories()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            categoryRepository.getCategories().onSuccess { cats ->
                _state.update { it.copy(categories = cats) }
            }
        }
    }

    private fun observeCart() {
        viewModelScope.launch {
            cartRepository.cartItems.collectLatest { items ->
                val totalCount = items.sumOf { it.quantity }
                _state.update { it.copy(cartItemCount = totalCount) }
            }
        }
    }

    private fun observeUnreadMessages() {
        viewModelScope.launch {
            chatRepository.getConversations().collectLatest { conversations ->
                val totalUnread = conversations.sumOf { it.unreadCount }
                _state.update { it.copy(unreadMessageCount = totalUnread) }
            }
        }
    }

    fun onEvent(event: SearchEvent) {
        when(event) {
            is SearchEvent.QueryChanged -> {
                _state.update { it.copy(searchQuery = event.query) }
                searchJob?.cancel()
                searchJob = viewModelScope.launch {
                    if (event.query.isBlank()) {
                        loadInitialData()
                    } else {
                        delay(500L)
                        performSearch()
                    }
                }
            }
            is SearchEvent.KeywordClicked -> {
                _state.update { it.copy(searchQuery = event.keyword) }
                performSearch()
            }
            SearchEvent.PerformSearch -> performSearch()
            SearchEvent.ClearSearch -> {
                searchJob?.cancel()
                _state.update { it.copy(searchQuery = "") }
                loadInitialData()
            }
            SearchEvent.LoadSearchData -> loadInitialData()
            is SearchEvent.LocationSelected -> {
                _state.update { it.copy(selectedLocation = event.location) }
                // In real app, you would update lat/lng here
            }
            is SearchEvent.DeleteHistoryItem -> {
                viewModelScope.launch {
                    searchRepository.deleteHistoryItem(event.id).onSuccess {
                        _state.update { s ->
                            s.copy(recentKeyWords = s.recentKeyWords.filter { it.id != event.id })
                        }
                    }
                }
            }
            SearchEvent.ClearAllHistory -> {
                viewModelScope.launch {
                    searchRepository.clearAllHistory().onSuccess {
                        _state.update { it.copy(recentKeyWords = emptyList()) }
                    }
                }
            }
            is SearchEvent.SortSelected -> {
                _state.update { it.copy(selectedSort = event.sort) }
                if (state.value.searchQuery.isNotBlank()) performSearch()
            }
            is SearchEvent.CategorySelected -> {
                val newCategoryId = if (state.value.selectedCategoryId == event.categoryId) null else event.categoryId
                _state.update { it.copy(selectedCategoryId = newCategoryId) }
                if (state.value.searchQuery.isNotBlank()) performSearch()
            }
        }
    }

    private fun performSearch() {
        val query = state.value.searchQuery
        if (query.isBlank()) return

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            
            searchRepository.unifiedSearch(
                query = query,
                lat = state.value.lat,
                lng = state.value.lng,
                sort = state.value.selectedSort,
                categoryId = state.value.selectedCategoryId
            ).onSuccess { (foods, restaurants) ->
                _state.update { it.copy(
                    popularFood = foods,
                    suggestedRestaurants = restaurants,
                    isLoading = false
                ) }
                // Save to history after successful search
                searchRepository.saveHistory(query)
                refreshHistory()
            }.onFailure { e ->
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    private fun refreshHistory() {
        viewModelScope.launch {
            searchRepository.getHistory().onSuccess { history ->
                _state.update { it.copy(recentKeyWords = history) }
            }
        }
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            
            // 1. Get Location
            val location = locationTracker.getCurrentLocation()
            _state.update { it.copy(lat = location?.latitude, lng = location?.longitude) }

            // 2. Fetch History
            refreshHistory()

            // 3. Fetch Suggestions
            searchRepository.getSuggestions(
                lat = state.value.lat,
                lng = state.value.lng
            ).onSuccess { (foods, restaurants) ->
                _state.update { it.copy(
                    popularFood = foods,
                    suggestedRestaurants = restaurants,
                    isLoading = false
                ) }
            }.onFailure { e ->
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}
