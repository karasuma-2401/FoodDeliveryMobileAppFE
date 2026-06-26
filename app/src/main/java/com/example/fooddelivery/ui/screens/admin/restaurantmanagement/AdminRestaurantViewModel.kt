package com.example.fooddelivery.ui.screens.admin.restaurantmanagement

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fooddelivery.data.remote.dto.RestaurantItemDto
import com.example.fooddelivery.domain.repository.AdminRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminRestaurantState(
    val isLoading: Boolean = false,
    val restaurants: List<RestaurantItemDto> = emptyList(),
    val searchQuery: String = "",
    val error: String? = null
)

@HiltViewModel
class AdminRestaurantViewModel @Inject constructor(
    private val repository: AdminRepository
) : ViewModel() {

    private val _state = mutableStateOf(AdminRestaurantState())
    val state: State<AdminRestaurantState> = _state

    private var masterList = emptyList<RestaurantItemDto>()

    init {
        fetchRestaurants()
    }

    fun fetchRestaurants() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            repository.getMyRestaurants()
                .onSuccess { list ->
                    masterList = list
                    _state.value = _state.value.copy(isLoading = false, restaurants = list)
                }
                .onFailure { error ->
                    _state.value = _state.value.copy(isLoading = false, error = error.message)
                }
        }
    }

    fun onSearchQueryChange(query: String) {
        _state.value = _state.value.copy(searchQuery = query)
        val filtered = if (query.isBlank()) masterList else {
            masterList.filter {
                it.name.contains(query, ignoreCase = true) || it.phone.contains(query)
            }
        }
        _state.value = _state.value.copy(restaurants = filtered)
    }

    fun updateApprovalStatus(restaurantId: Int, status: String) {
        viewModelScope.launch {
            val updatedList = _state.value.restaurants.map {
                if (it.id == restaurantId) it.copy(status = status) else it // 🌟 Sửa ở đây
            }
            _state.value = _state.value.copy(restaurants = updatedList)

            repository.updateRestaurantApproval(restaurantId, status)
                .onFailure {
                    fetchRestaurants()
                }
        }
    }
}