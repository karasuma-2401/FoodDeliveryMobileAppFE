package com.example.fooddelivery.ui.screens.category

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fooddelivery.domain.model.Category
import com.example.fooddelivery.domain.model.FoodItem
import com.example.fooddelivery.domain.usecase.GetCategoriesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CategoryFilterState(
    val categories: List<Category> = emptyList(),
    val selectedCategoryId: String = "",
    val foods: List<FoodItem> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

sealed interface CategoryFilterEvent {
    data class SelectCategory(val categoryId: String): CategoryFilterEvent
}

@HiltViewModel
class CategoryFilterViewModel @Inject constructor(
    private val getCategoriesUseCase: GetCategoriesUseCase,
    savedStateHandle: SavedStateHandle
): ViewModel() {
    private val _state = MutableStateFlow(CategoryFilterState())
    val state: StateFlow<CategoryFilterState> = _state.asStateFlow()

    init {
        val initialId = savedStateHandle.get<String>("categoryId") ?: ""
        loadInitialData(initialId)
    }

    fun onEvent (event: CategoryFilterEvent) {
        when(event) {
            is CategoryFilterEvent.SelectCategory -> {
                if (_state.value.selectedCategoryId != event.categoryId) {
                    _state.update { it.copy(selectedCategoryId = event.categoryId) }
                    loadFoodsByCategory(event.categoryId)
                }
            }
        }
    }

    private fun loadInitialData(initialId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            
            val result = getCategoriesUseCase()
            
            result.onSuccess { categories ->
                val actualSelectedId = initialId.ifEmpty { categories.firstOrNull()?.id ?: "" }
                _state.update { it.copy(
                    categories = categories, 
                    selectedCategoryId = actualSelectedId 
                ) }
                loadFoodsByCategory(actualSelectedId)
            }.onFailure { e ->
                _state.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    private fun loadFoodsByCategory(categoryId: String) {
        if (categoryId.isEmpty()) return
        
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            // NOTE: Here we would typically call a getFoodsByCategoryUseCase
            // For now, keeping the mock food list but it will be filtered by real category IDs
            val allFoods = listOf(
                FoodItem(
                    id = "f1", name = "Cheese Burger", restaurantId = "r1",
                    restaurantName = "Burger King", categoryId = "1", price = 15.0, soldCount = 120
                ),
                FoodItem(
                    id = "f2", name = "Pepperoni Pizza", restaurantId = "r2",
                    restaurantName = "Pizza Hut", categoryId = "2", price = 20.0, soldCount = 85
                ),
                FoodItem(
                    id = "f3", name = "Coca Cola", restaurantId = "r3",
                    restaurantName = "Store A", categoryId = "3", price = 5.0, soldCount = 300
                )
            )
            val filteredFoods = allFoods.filter { it.categoryId == categoryId }

            _state.update { it.copy(foods = filteredFoods, isLoading = false) }
        }
    }
}
