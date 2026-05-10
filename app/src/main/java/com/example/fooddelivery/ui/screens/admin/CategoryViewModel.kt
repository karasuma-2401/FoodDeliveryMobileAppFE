package com.example.fooddelivery.ui.screens.admin

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
data class CategoryFormState(
    val categoryName: String = "",
    val parentCategory: String = "No Parent (Top Level)",
    val displayOrder: String = "0",
    val isActive: Boolean = true,
    val imageUri: String? = null, // Lưu path của ảnh sau khi chọn
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)

// CategoryEvent.kt
sealed class CategoryEvent {
    data class NameChanged(val name: String) : CategoryEvent()
    data class ParentChanged(val parent: String) : CategoryEvent()
    data class OrderChanged(val order: String) : CategoryEvent()
    data class StatusChanged(val status: Boolean) : CategoryEvent()
    data class ImageSelected(val uri: String) : CategoryEvent()
    object SaveCategory : CategoryEvent()
}
class CategoryViewModel : ViewModel() {
    var uiState by mutableStateOf(CategoryFormState())
        private set

    fun onEvent(event: CategoryEvent) {
        when (event) {
            is CategoryEvent.NameChanged -> uiState = uiState.copy(categoryName = event.name)
            is CategoryEvent.ParentChanged -> uiState = uiState.copy(parentCategory = event.parent)
            is CategoryEvent.OrderChanged -> uiState = uiState.copy(displayOrder = event.order)
            is CategoryEvent.StatusChanged -> uiState = uiState.copy(isActive = event.status)
            is CategoryEvent.ImageSelected -> uiState = uiState.copy(imageUri = event.uri)
            is CategoryEvent.SaveCategory -> saveCategory()
        }
    }

    private fun saveCategory() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true)
            // Giả lập gọi API
            delay(1500)
            uiState = uiState.copy(isLoading = false, isSuccess = true)
        }
    }
}