package com.example.fooddelivery.ui.screens.restaurant.food_management

import android.net.Uri
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fooddelivery.data.local.datastore.TokenManager
import com.example.fooddelivery.data.remote.dto.FoodSizeRequest
import com.example.fooddelivery.domain.model.Category
import com.example.fooddelivery.domain.repository.CategoryRepository
import com.example.fooddelivery.domain.repository.FoodRepository
import com.example.fooddelivery.domain.repository.RestaurantRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import javax.inject.Inject

data class IngredientItemState(
    val id: String,
    val name: String,
    val iconUrl: String? = null,
    val isSelected: Boolean = false
)

data class AddFoodState(
    val itemName: String = "",
    val details: String = "",
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
    val categories: List<String> = emptyList(),
    val selectedCategory: String = "",
    val selectedSizes: Map<String, String> = emptyMap(),
    val ingredients: List<IngredientItemState> = emptyList(),
    val selectedImageFile: File? = null,
    val selectedImageUri: Uri? = null
)

@HiltViewModel
class AddFoodViewModel @Inject constructor(
    private val repository: RestaurantRepository,
    private val categoryRepository: CategoryRepository,
    private val foodRepository: FoodRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _state = mutableStateOf(AddFoodState())
    val state: State<AddFoodState> = _state

    private var dynamicCategories: List<Category> = emptyList()

    init {
        loadCategoriesAndIngredients()
    }

    private fun loadCategoriesAndIngredients() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            try {
                val categoriesDeferred = async { categoryRepository.getCategories() }
                val ingredientsDeferred = async { foodRepository.getIngredients() }

                val categoriesResult = categoriesDeferred.await()
                val ingredientsResult = ingredientsDeferred.await()

                if (categoriesResult.isSuccess && ingredientsResult.isSuccess) {
                    val categories = categoriesResult.getOrNull() ?: emptyList()
                    dynamicCategories = categories
                    val ingredientsDto = ingredientsResult.getOrNull() ?: emptyList()
                    val remoteIngredients = ingredientsDto.map { dto ->
                        IngredientItemState(
                            id = dto.id.toString(),
                            name = dto.name,
                            iconUrl = dto.icon,
                            isSelected = false
                        )
                    }

                    _state.value = _state.value.copy(
                        isLoading = false,
                        categories = categories.map { it.name },
                        ingredients = remoteIngredients
                    )
                } else {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = "Failed to load dynamic data. Please try again."
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.localizedMessage ?: "Unknown error occurred"
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

    private fun sizeOrder(size: String): Int {
        return when (size.uppercase()) {
            "S" -> 1
            "M" -> 2
            "L" -> 3
            "XL" -> 4
            else -> 99
        }
    }

    fun saveFoodItem() {
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

            val restaurantId = tokenManager.getRestaurantId.first()
            if (restaurantId == null) {
                _state.value = _state.value.copy(isLoading = false, error = "Restaurant ID not found. Please log in again.")
                return@launch
            }

            val category = dynamicCategories.find { it.name.equals(currentState.selectedCategory, ignoreCase = true) }
            val categoryId = category?.id?.toIntOrNull() ?: 1

            val selectedIngredientIds = currentState.ingredients
                .filter { it.isSelected }
                .mapNotNull { it.id.toIntOrNull() }

            val sortedSizes = currentState.selectedSizes.entries
                .sortedBy { sizeOrder(it.key) }

            val sizesList = sortedSizes.mapIndexed { index, entry ->
                val sizeId = mapSizeToId(entry.key)
                val price = entry.value.toDoubleOrNull() ?: 0.0
                FoodSizeRequest(sizeId = sizeId, price = price, isDefault = index == 0)
            }

            val jsonStrict = Json {
                encodeDefaults = true
            }

            val sizesJson = jsonStrict.encodeToString(sizesList)

            val defaultPrice = sizesList.firstOrNull { it.isDefault }?.price ?: 0.0

            repository.addFood(
                name = currentState.itemName,
                description = currentState.details,
                categoryId = categoryId,
                restaurantId = restaurantId,
                price = defaultPrice,
                sizesJson = sizesJson,
                ingredientIds = selectedIngredientIds,
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

    fun resetState() {
        _state.value = AddFoodState()
        loadCategoriesAndIngredients()
    }
}