package com.example.fooddelivery.ui.screens.restaurant.food_management

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fooddelivery.data.remote.dto.FoodResponse
import com.example.fooddelivery.domain.repository.RestaurantRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MyFoodListState(
    val categories: List<String> = listOf("All", "Breakfast", "Lunch", "Dinner"),
    val selectedCategoryIndex: Int = 0,
    val foodList: List<FoodResponse> = emptyList(),
    val filteredFoodList: List<FoodResponse> = emptyList(),
    val totalItems: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class MyFoodListViewModel @Inject constructor(
    private val repository: RestaurantRepository
) : ViewModel() {
    private val _state = mutableStateOf(MyFoodListState())
    val state: State<MyFoodListState> = _state

    init {
        loadFoods()
    }

    fun loadFoods() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            repository.getFoods()
                .onSuccess { foods ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        foodList = foods,
                        filteredFoodList = foods,
                        totalItems = foods.size
                    )
                    filterByCategory(_state.value.selectedCategoryIndex)
                }
                .onFailure { error ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = error.message
                    )
                }
        }
    }

    fun onCategorySelected(index: Int) {
        _state.value = _state.value.copy(selectedCategoryIndex = index)
        filterByCategory(index)
    }

    private fun filterByCategory(index: Int) {
        val category = _state.value.categories[index]
        val filtered = if (category == "All") {
            _state.value.foodList
        } else {
            _state.value.foodList.filter { it.category.equals(category, ignoreCase = true) }
        }
        _state.value = _state.value.copy(
            filteredFoodList = filtered,
            totalItems = filtered.size
        )
    }

    fun deleteFood(id: String) {
        viewModelScope.launch {
            repository.deleteFood(id)
                .onSuccess {
                    loadFoods()
                }
        }
    }
}
