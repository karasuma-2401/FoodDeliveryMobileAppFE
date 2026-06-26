package com.example.fooddelivery.ui.screens.category

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.fooddelivery.domain.model.Category
import com.example.fooddelivery.domain.model.FoodItem
import com.example.fooddelivery.domain.usecase.GetCategoriesUseCase
import com.example.fooddelivery.domain.usecase.GetCategoryDetailUseCase
import com.example.fooddelivery.ui.navigation.CategoryFilterRoute
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
    data class SelectCategory(val categoryId: String) : CategoryFilterEvent
}

@HiltViewModel
class CategoryFilterViewModel @Inject constructor(
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val getCategoryDetailUseCase: GetCategoryDetailUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val _state = MutableStateFlow(CategoryFilterState())
    val state: StateFlow<CategoryFilterState> = _state.asStateFlow()

    init {
        val initialId = savedStateHandle.toRoute<CategoryFilterRoute>().categoryId
        loadInitialData(initialId)
    }

    fun onEvent(event: CategoryFilterEvent) {
        when (event) {
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

            getCategoriesUseCase()
                .onSuccess { categories ->
                    val actualSelectedId = initialId.ifEmpty { categories.firstOrNull()?.id ?: "" }
                    _state.update {
                        it.copy(
                            categories = categories,
                            selectedCategoryId = actualSelectedId
                        )
                    }
                    loadFoodsByCategory(actualSelectedId)
                }
                .onFailure { e ->
                    _state.update { it.copy(isLoading = false, errorMessage = e.message) }
                }
        }
    }

    private fun loadFoodsByCategory(categoryId: String) {
        if (categoryId.isEmpty()) {
            _state.update { it.copy(foods = emptyList(), isLoading = false) }
            return
        }

        val categoryIdInt = categoryId.toIntOrNull()
        if (categoryIdInt == null) {
            _state.update { it.copy(foods = emptyList(), isLoading = false, errorMessage = "Invalid category") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }

            getCategoryDetailUseCase(categoryIdInt)
                .onSuccess { detail ->
                    _state.update { it.copy(foods = detail.foods, isLoading = false) }
                }
                .onFailure { e ->
                    _state.update {
                        it.copy(
                            foods = emptyList(),
                            isLoading = false,
                            errorMessage = e.message ?: "Failed to load category"
                        )
                    }
                }
        }
    }
}
