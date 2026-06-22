package com.example.fooddelivery.ui.screens.restaurant.food_management

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BakeryDining
import androidx.compose.material.icons.filled.Egg
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.LocalPizza
import androidx.compose.material.icons.filled.SetMeal
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fooddelivery.data.remote.dto.FoodRequest
import com.example.fooddelivery.domain.repository.RestaurantRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
data class EditFoodState(
    val foodId: String = "",
    val itemName: String = "",
    val details: String = "",
    val imageUrl: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false,

    val categories: List<String> = listOf("Fast Food", "Pizza", "Beverages", "Dessert"),
    val selectedCategory: String = "",
    val selectedSizes: Map<String, String> = emptyMap(),
    val ingredients: List<IngredientItemState> = listOf(
        IngredientItemState("1", "Egg", Icons.Default.Egg),
        IngredientItemState("2", "Grass", Icons.Default.Grass),
        IngredientItemState("3", "Salmon", Icons.Default.SetMeal),
        IngredientItemState("4", "Pizza", Icons.Default.LocalPizza),
        IngredientItemState("5", "Bread", Icons.Default.BakeryDining)
    )
)

@HiltViewModel
class EditFoodViewModel @Inject constructor(
    private val repository: RestaurantRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = mutableStateOf(EditFoodState())
    val state: State<EditFoodState> = _state

    init {
        savedStateHandle.get<String>("foodId")?.let { foodId ->
            loadFoodDetails(foodId)
        }
    }

    private fun loadFoodDetails(id: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, foodId = id)
            repository.getFoodById(id)
                .onSuccess { food ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        itemName = food.name,
                        details = food.details,
                        selectedCategory = food.category,
                        imageUrl = food.imageUrl,
                        selectedSizes = mapOf("M" to food.price.toString())
                    )
                }
                .onFailure { error ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = error.message
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
            val defaultPrice = currentState.selectedSizes.values.firstOrNull()?.toDoubleOrNull() ?: 0.0

            val request = FoodRequest(
                name = currentState.itemName,
                price = defaultPrice,
                details = currentState.details,
                category = currentState.selectedCategory,
                imageUrl = currentState.imageUrl
            )

            repository.updateFood(currentState.foodId, request)
                .onSuccess {
                    _state.value = _state.value.copy(isLoading = false, isSuccess = true)
                }
                .onFailure { error ->
                    _state.value = _state.value.copy(isLoading = false, error = error.message)
                }
        }
    }
}