package com.example.fooddelivery.ui.screens.restaurant.food_management

import android.net.Uri
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
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

data class EditFoodState(
    val foodId: Int? = null,
    val isCreatingNew: Boolean = true,
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
    private val foodRepository: FoodRepository,
    private val tokenManager: TokenManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = mutableStateOf(EditFoodState())
    val state: State<EditFoodState> = _state

    private var dynamicCategories: List<Category> = emptyList()

    init {
        savedStateHandle.get<String>("foodId")?.let { foodIdStr ->
            val foodId = foodIdStr.toIntOrNull()
            if (foodId != null && foodId > 0) {
                loadFoodDetails(foodId)
            } else {
                loadCategoriesAndIngredients()
            }
        } ?: loadCategoriesAndIngredients()
    }

    private fun loadCategoriesAndIngredients() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null, isCreatingNew = true)

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
                            iconKey = dto.icon,
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
                        error = "Failed to load data. Please try again."
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

    private fun loadFoodDetails(id: Int) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, foodId = id, error = null)

            try {
                // Chạy song song cả 3 API cho tốc độ tối đa
                val categoriesDeferred = async { categoryRepository.getCategories() }
                val ingredientsDeferred = async { foodRepository.getIngredients() }
                val foodDeferred = async { repository.getFoodById(id) }

                val categoriesResult = categoriesDeferred.await()
                val ingredientsResult = ingredientsDeferred.await()
                val foodResult = foodDeferred.await()

                if (categoriesResult.isSuccess && ingredientsResult.isSuccess && foodResult.isSuccess) {
                    val categories = categoriesResult.getOrNull() ?: emptyList()
                    dynamicCategories = categories

                    val ingredientsDto = ingredientsResult.getOrNull() ?: emptyList()
                    val food = foodResult.getOrNull()!!

                    val savedIngredientIds = food.foodIngredients.orEmpty()
                        .map { it.id.toString() }
                        .toSet()

                    val remoteIngredients = ingredientsDto.map { dto ->
                        IngredientItemState(
                            id = dto.id.toString(),
                            name = dto.name,
                            iconKey = dto.icon,
                            isSelected = dto.id.toString() in savedIngredientIds
                        )
                    }

                    val initialSizes = food.sizes?.associate { sizeDto ->
                        sizeDto.name to sizeDto.price.toString()
                    } ?: mapOf("M" to food.price.toString())

                    val matchedCategoryName = categories.find { it.id == food.categoryId.toString() }?.name ?: ""

                    _state.value = _state.value.copy(
                        isLoading = false,
                        itemName = food.name,
                        details = food.description,
                        selectedCategory = matchedCategoryName,
                        imageUrl = food.image,
                        categories = categories.map { it.name },
                        selectedSizes = initialSizes,
                        ingredients = remoteIngredients
                    )
                } else {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = "Failed to load complete food data. Please try again."
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

            try {
                val category = dynamicCategories.find {
                    it.name.equals(currentState.selectedCategory, ignoreCase = true)
                }
                val categoryId = category?.id?.toIntOrNull() ?: 1

                val selectedIngredientIds = currentState.ingredients
                    .filter { it.isSelected }
                    .mapNotNull { it.id.toIntOrNull() }

                val sortedSizes = currentState.selectedSizes.entries
                    .sortedBy { foodSizeSortKey(it.key) }

                val sizesList = sortedSizes.mapIndexed { index, entry ->
                    val sizeId = mapFoodSizeToId(entry.key)
                    val price = entry.value.toDoubleOrNull() ?: 0.0
                    FoodSizeRequest(sizeId = sizeId, price = price, isDefault = index == 0)
                }

                val sizesJson = Json.encodeToString(sizesList)
                val defaultPrice = sizesList.firstOrNull { it.isDefault }?.price ?: 0.0

                if (currentState.isCreatingNew) {
                    // Create new food
                    val restaurantId = tokenManager.getRestaurantId.first()
                    if (restaurantId == null) {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            error = "Restaurant ID not found. Please log in again."
                        )
                        return@launch
                    }

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
                } else {
                    // Update existing food
                    val ingredientIdsPayload = if (selectedIngredientIds.isNotEmpty()) {
                        selectedIngredientIds.joinToString(",")
                    } else {
                        null
                    }

                    repository.updateFood(
                        id = currentState.foodId ?: 0,
                        name = currentState.itemName,
                        description = currentState.details,
                        categoryId = categoryId,
                        price = defaultPrice,
                        sizesJson = sizesJson,
                        ingredientIdsCsv = ingredientIdsPayload,
                        imageFile = currentState.selectedImageFile
                    )
                        .onSuccess {
                            _state.value = _state.value.copy(isLoading = false, isSuccess = true)
                        }
                        .onFailure { error ->
                            _state.value = _state.value.copy(isLoading = false, error = error.message)
                        }
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.localizedMessage ?: "Unknown error occurred"
                )
            }
        }
    }
}