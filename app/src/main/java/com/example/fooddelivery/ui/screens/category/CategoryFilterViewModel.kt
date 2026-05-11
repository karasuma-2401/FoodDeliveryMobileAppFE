package com.example.fooddelivery.ui.screens.category

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fooddelivery.domain.model.Category
import com.example.fooddelivery.domain.model.FoodItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
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
    val isLoading: Boolean = false
)
sealed interface CategoryFilterEvent {
    data class SelectCategory(val categoryId: String): CategoryFilterEvent
}
@HiltViewModel
class CategoryFilterViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
): ViewModel() {
    private val _state = MutableStateFlow(CategoryFilterState())
    val state: StateFlow<CategoryFilterState> = _state.asStateFlow()

    init {
        loadInitialData(savedStateHandle.get<String>("categoryId") ?: "")
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
            _state.update { it.copy(isLoading = true) }
            val mockCategories = listOf(
                Category(id = "1", name = "Burger", startingPrice = 5.0),
                Category(id = "2", name = "Pizza", startingPrice = 8.0),
                Category(id = "3", name = "Drink", startingPrice = 2.0),
                Category(id = "4", name = "Sushi", startingPrice = 12.0),
                Category(id = "5", name = "Desert", startingPrice = 4.0)
            )
            val actualSelectedId = initialId.ifEmpty { mockCategories.firstOrNull()?.id ?: ""}
            _state.update { it.copy(categories = mockCategories, selectedCategoryId = actualSelectedId) }
            loadFoodsByCategory(initialId)
        }
    }
    private fun loadFoodsByCategory(categoryId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            delay(800)
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