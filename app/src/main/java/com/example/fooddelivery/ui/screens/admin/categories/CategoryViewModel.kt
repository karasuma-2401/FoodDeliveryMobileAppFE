package com.example.fooddelivery.ui.screens.admin.categories

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fooddelivery.domain.repository.CategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CategoryFormState(
    val categoryId: Int? = null,
    val isEditMode: Boolean = false,
    val categoryName: String = "",
    // Vẫn giữ lại trong state để lưu giá trị ngầm định (hoặc giá trị cũ lúc edit)
    val displayOrder: Int = 0,
    val isActive: Boolean = true,
    val imageUri: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)

sealed class CategoryEvent {
    data class NameChanged(val name: String) : CategoryEvent()
    data class ImageSelected(val uri: String) : CategoryEvent()
    object SaveCategory : CategoryEvent()
}

@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    var uiState by mutableStateOf(CategoryFormState())
        private set

    init {
        // Lấy categoryId từ Navigation
        val categoryId = savedStateHandle.get<String>("categoryId")?.toIntOrNull()
        if (categoryId != null && categoryId != -1) {
            uiState = uiState.copy(categoryId = categoryId, isEditMode = true)
            loadCategoryDetailsForEdit(categoryId)
        }
    }

    private fun loadCategoryDetailsForEdit(id: Int) {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)

            // Dùng getCategories để lấy được đầy đủ field displayOrder và isActive
            categoryRepository.getCategories(limit = 100)
                .onSuccess { categories ->
                    val category = categories.find { it.id == id.toString() }
                    if (category != null) {
                        uiState = uiState.copy(
                            isLoading = false,
                            categoryName = category.name,
                            displayOrder = category.displayOrder, // Giữ lại order cũ
                            isActive = category.isActive,         // Giữ lại status cũ
                            imageUri = category.imageUrl
                        )
                    } else {
                        uiState = uiState.copy(isLoading = false, error = "Category not found")
                    }
                }
                .onFailure { error ->
                    uiState = uiState.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to load category"
                    )
                }
        }
    }

    fun onEvent(event: CategoryEvent) {
        when (event) {
            is CategoryEvent.NameChanged -> uiState = uiState.copy(categoryName = event.name, error = null)
            is CategoryEvent.ImageSelected -> uiState = uiState.copy(imageUri = event.uri)
            is CategoryEvent.SaveCategory -> saveCategory()
        }
    }

    private fun saveCategory() {
        val name = uiState.categoryName.trim()
        if (name.isBlank()) {
            uiState = uiState.copy(error = "Category name is required")
            return
        }

        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)

            val result = if (uiState.isEditMode && uiState.categoryId != null) {
                categoryRepository.updateCategory(
                    id = uiState.categoryId!!,
                    name = name,
                    description = name, // Tạm tái sử dụng name cho description
                    displayOrder = uiState.displayOrder,
                    isActive = uiState.isActive,
                    imageUri = uiState.imageUri
                )
            } else {
                categoryRepository.createCategory(
                    name = name,
                    description = name,
                    displayOrder = uiState.displayOrder,
                    isActive = uiState.isActive,
                    imageUri = uiState.imageUri
                )
            }

            result.onSuccess {
                uiState = uiState.copy(isLoading = false, isSuccess = true)
            }.onFailure { error ->
                uiState = uiState.copy(
                    isLoading = false,
                    error = error.message ?: "Failed to save category"
                )
            }
        }
    }
}