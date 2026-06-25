package com.example.fooddelivery.ui.screens.admin.categories

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fooddelivery.domain.model.Category
import com.example.fooddelivery.domain.repository.CategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CategoryItem(
    val id: Int,
    val name: String,
    val imageUrl: String,
    val displayOrder: Int,
    val isActive: Boolean
)

data class CategoryListState(
    val categories: List<CategoryItem> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class CategoryListViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository
) : ViewModel() {
    private val _state = mutableStateOf(CategoryListState())
    val state: State<CategoryListState> = _state

    init {
        loadCategories()
    }

    fun loadCategories() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            categoryRepository.getCategories(
                keyword = _state.value.searchQuery.takeIf { it.isNotBlank() },
                limit = 100,
                offset = 0
            ).onSuccess { categories ->
                _state.value = _state.value.copy(
                    isLoading = false,
                    categories = categories.map { it.toCategoryItem() }
                )
            }.onFailure { error ->
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = error.message ?: "Failed to load categories"
                )
            }
        }
    }

    fun onSearchChange(query: String) {
        _state.value = _state.value.copy(searchQuery = query)
        loadCategories()
    }

    fun deleteCategory(id: Int) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            categoryRepository.deleteCategory(id)
                .onSuccess { loadCategories() }
                .onFailure { error ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to delete category"
                    )
                }
        }
    }

    private fun Category.toCategoryItem(): CategoryItem {
        return CategoryItem(
            id = id.toIntOrNull() ?: 0,
            name = name,
            imageUrl = imageUrl.orEmpty(),
            displayOrder = displayOrder,
            isActive = isActive
        )
    }
}
