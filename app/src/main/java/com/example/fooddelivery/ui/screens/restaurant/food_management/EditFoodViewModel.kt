package com.example.fooddelivery.ui.screens.restaurant.food_management

import android.net.Uri
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fooddelivery.data.remote.dto.FoodSizeRequest
import com.example.fooddelivery.domain.model.Category
import com.example.fooddelivery.domain.repository.CategoryRepository
import com.example.fooddelivery.domain.repository.RestaurantRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import java.io.File

data class EditFoodState(
    val foodId: Int = 0,
    val itemName: String = "",
    val details: String = "",
    val imageUrl: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false,
    val categories: List<String> = emptyList(),
    val selectedCategory: String = "",
    val selectedSizes: Map<String, String> = emptyMap(),
    val ingredients: List<IngredientItemState> = emptyList(),
    val selectedImageFile: File? = null,
    val selectedImageUri: Uri? = null
)

@HiltViewModel
class EditFoodViewModel @Inject constructor(
    private val repository: RestaurantRepository,
    private val categoryRepository: CategoryRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = mutableStateOf(EditFoodState())
    val state: State<EditFoodState> = _state

    private var dynamicCategories: List<Category> = emptyList()

    init {
        savedStateHandle.get<String>("foodId")?.let { foodIdStr ->
            val foodId = foodIdStr.toIntOrNull() ?: 0
            loadFoodDetails(foodId)
        }
    }

    private fun loadFoodDetails(id: Int) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, foodId = id)
            
            // Tải danh mục trước để mapping hiển thị
            categoryRepository.getCategories()
                .onSuccess { categories ->
                    dynamicCategories = categories
                    repository.getFoodById(id)
                        .onSuccess { food ->
                            val matchedCategoryName = categories.find { it.id == food.categoryId.toString() }?.name ?: ""
                            _state.value = _state.value.copy(
                                isLoading = false,
                                itemName = food.name,
                                details = food.description,
                                selectedCategory = matchedCategoryName,
                                imageUrl = food.image,
                                categories = categories.map { it.name },
                                selectedSizes = mapOf("M" to food.price.toString()),
                                ingredients = defaultIngredientItems()
                            )
                        }
                        .onFailure { error ->
                            _state.value = _state.value.copy(
                                isLoading = false,
                                error = error.message
                            )
                        }
                }
                .onFailure { error ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to load categories"
                    )
                }
        }
    }

    fun onNameChange(newName: String) {
        _state.value = _state.value.copy(itemName = newName)
    }

    fun onDetailsChange(newDetails: String) {
        _state.value = _state.value.copy(details = newDetails)
    }

    fun onCategorySelect(category: String) {
        _state.value = _state.value.copy(selectedCategory = category)
    }

    fun onImageSelected(file: File, uri: Uri) {
        _state.value = _state.value.copy(selectedImageFile = file, selectedImageUri = uri)
    }

    fun onSizeToggle(size: String, isSelected: Boolean) {
        val currentState = _state.value
        val updatedSizes = currentState.selectedSizes.toMutableMap()
        if (isSelected) {
            updatedSizes[size] = ""
        } else {
            updatedSizes.remove(size)
        }
        _state.value = currentState.copy(selectedSizes = updatedSizes)
    }

    fun onSizePriceChange(size: String, price: String) {
        val currentState = _state.value
        val updatedSizes = currentState.selectedSizes.toMutableMap()
        if (updatedSizes.containsKey(size)) {
            updatedSizes[size] = price
        }
        _state.value = currentState.copy(selectedSizes = updatedSizes)
    }

    fun onIngredientToggle(index: Int) {
        val currentState = _state.value
        val updatedIngredients = currentState.ingredients.mapIndexed { i, item ->
            if (i == index) item.copy(isSelected = !item.isSelected) else item
        }
        _state.value = currentState.copy(ingredients = updatedIngredients)
    }

    private fun mapSizeToId(size: String): Int {
        return when (size.uppercase()) {
            "S" -> 1
            "M" -> 2
            "L" -> 3
            "XL" -> 4
            else -> 2
        }
    }

    fun updateFoodItem() {
        val currentState = _state.value

        if (currentState.itemName.isBlank() || currentState.selectedCategory.isBlank()) {
            _state.value = _state.value.copy(error = "Please fill in item name and category")
            return
        }
        if (currentState.selectedSizes.isEmpty()) {
            _state.value = _state.value.copy(error = "Please select at least one size")
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            val category = dynamicCategories.find { it.name.equals(currentState.selectedCategory, ignoreCase = true) }
            val categoryId = category?.id?.toIntOrNull() ?: 1

            val selectedIngredientIds = currentState.ingredients
                .filter { it.isSelected }
                .map { it.id }
            val ingredientIdsCsv = if (selectedIngredientIds.isNotEmpty()) selectedIngredientIds.joinToString(",") else null

            val sizesList = currentState.selectedSizes.entries.mapIndexed { index, entry ->
                val sizeId = mapSizeToId(entry.key)
                val price = entry.value.toDoubleOrNull() ?: 0.0
                val isDefault = index == 0
                FoodSizeRequest(sizeId = sizeId, price = price, isDefault = isDefault)
            }
            val sizesJson = Json.encodeToString(sizesList)
            val defaultPrice = sizesList.firstOrNull { it.isDefault }?.price ?: 0.0

            repository.updateFood(
                id = currentState.foodId,
                name = currentState.itemName,
                description = currentState.details,
                categoryId = categoryId,
                price = defaultPrice,
                sizesJson = sizesJson,
                ingredientIdsCsv = ingredientIdsCsv,
                imageFile = currentState.selectedImageFile
            )
                .onSuccess {
                    _state.value = _state.value.copy(isLoading = false, isSuccess = true)
                }
                .onFailure { error ->
                    _state.value = _state.value.copy(isLoading = false, error = error.message)
                }
        }
    }
}
