package com.example.fooddelivery.ui.screens.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fooddelivery.domain.model.Category
import com.example.fooddelivery.domain.usecase.GetCategoriesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AllCategoriesState(
    val categories: List<Category> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class AllCategoriesViewModel @Inject constructor(
    private val getCategoriesUseCase: GetCategoriesUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(AllCategoriesState())
    val state: StateFlow<AllCategoriesState> = _state.asStateFlow()

    init {
        loadAllCategories()
    }

    private fun loadAllCategories() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            
            val result = getCategoriesUseCase(limit = 100)
            
            result.onSuccess { categories ->
                _state.update { it.copy(categories = categories, isLoading = false) }
            }.onFailure { e ->
                _state.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }
}