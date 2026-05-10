package com.example.fooddelivery.ui.screens.admin

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

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

class CategoryListViewModel : ViewModel() {
    private val _state = mutableStateOf(CategoryListState())
    val state: State<CategoryListState> = _state

    init {
        loadCategories()
    }

    fun loadCategories() {
        _state.value = _state.value.copy(isLoading = true)
        viewModelScope.launch {
            // Giả lập delay load dữ liệu
            _state.value = _state.value.copy(
                isLoading = false,
                categories = listOf(
                    CategoryItem(1, "Pizza", "", 1, true),
                    CategoryItem(2, "Burgers", "", 2, true),
                    CategoryItem(3, "Drinks", "", 3, false)
                )
            )
        }
    }

    fun onSearchChange(query: String) {
        _state.value = _state.value.copy(searchQuery = query)
        // Cập nhật danh sách hiển thị dựa trên query nếu cần
    }

    fun deleteCategory(id: Int) {
        val currentCategories = _state.value.categories.toMutableList()
        currentCategories.removeIf { it.id == id }
        _state.value = _state.value.copy(categories = currentCategories)
    }
}
