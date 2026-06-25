package com.example.fooddelivery.ui.screens.admin.categories

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fooddelivery.domain.repository.CategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CategoryFormState(
    val categoryName: String = "",
    val parentCategory: String = "No Parent (Top Level)",
    val displayOrder: String = "0",
    val isActive: Boolean = true,
    val imageUri: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)

sealed class CategoryEvent {
    data class NameChanged(val name: String) : CategoryEvent()
    data class ParentChanged(val parent: String) : CategoryEvent()
    data class OrderChanged(val order: String) : CategoryEvent()
    data class StatusChanged(val status: Boolean) : CategoryEvent()
    data class ImageSelected(val uri: String) : CategoryEvent()
    object SaveCategory : CategoryEvent()
}

@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository
) : ViewModel() {
    var uiState by mutableStateOf(CategoryFormState())
        private set

    fun onEvent(event: CategoryEvent) {
        when (event) {
            is CategoryEvent.NameChanged -> uiState = uiState.copy(categoryName = event.name, error = null)
            is CategoryEvent.ParentChanged -> uiState = uiState.copy(parentCategory = event.parent)
            is CategoryEvent.OrderChanged -> uiState = uiState.copy(displayOrder = event.order, error = null)
            is CategoryEvent.StatusChanged -> uiState = uiState.copy(isActive = event.status)
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

        val displayOrder = uiState.displayOrder.toIntOrNull() ?: 0

        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)
            categoryRepository.createCategory(
                name = name,
                description = name,
                displayOrder = displayOrder,
                isActive = uiState.isActive,
                imageUri = uiState.imageUri
            ).onSuccess {
                uiState = uiState.copy(isLoading = false, isSuccess = true)
            }.onFailure { error ->
                uiState = uiState.copy(
                    isLoading = false,
                    error = error.message ?: "Failed to create category"
                )
            }
        }
    }
}
