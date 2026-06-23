package com.example.fooddelivery.ui.screens.restaurant.food_management

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fooddelivery.data.local.datastore.TokenManager
import com.example.fooddelivery.data.remote.dto.FoodResponse
import com.example.fooddelivery.domain.model.Category
import com.example.fooddelivery.domain.repository.CategoryRepository
import com.example.fooddelivery.domain.repository.RestaurantRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MyFoodListState(
    val categories: List<String> = listOf("All"),
    val selectedCategoryIndex: Int = 0,
    val foodList: List<FoodResponse> = emptyList(),
    val filteredFoodList: List<FoodResponse> = emptyList(),
    val totalItems: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class MyFoodListViewModel @Inject constructor(
    private val repository: RestaurantRepository,
    private val categoryRepository: CategoryRepository,
    private val tokenManager: TokenManager
) : ViewModel() {
    private val _state = mutableStateOf(MyFoodListState())
    val state: State<MyFoodListState> = _state

    private var dynamicCategories: List<Category> = emptyList()

    init {
        loadCategoriesAndFoods()
    }

    fun loadCategoriesAndFoods() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            categoryRepository.getCategories()
                .onSuccess { categories ->
                    dynamicCategories = categories
                    val categoryNames = listOf("All") + categories.map { it.name }
                    _state.value = _state.value.copy(categories = categoryNames)
                    loadFoods()
                }
                .onFailure { error ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to load categories"
                    )
                }
        }
    }

    fun loadFoods() {
        viewModelScope.launch {
            val restaurantId = tokenManager.getRestaurantId.first()
            if (restaurantId == null) {
                repository.getMyRestaurants()
                    .onSuccess { list ->
                        val firstId = list.firstOrNull()?.id
                        if (firstId != null) {
                            tokenManager.saveRestaurantId(firstId)
                            loadFoodsForId(firstId)
                        } else {
                            _state.value = _state.value.copy(isLoading = false, error = "No restaurant found")
                        }
                    }
                    .onFailure { error ->
                        _state.value = _state.value.copy(isLoading = false, error = error.message)
                    }
            } else {
                loadFoodsForId(restaurantId)
            }
        }
    }

    private suspend fun loadFoodsForId(restaurantId: Int) {
        repository.getFoods(restaurantId)
            .onSuccess { foods ->
                _state.value = _state.value.copy(
                    isLoading = false,
                    foodList = foods,
                    filteredFoodList = foods,
                    totalItems = foods.size
                )
                filterByCategory(_state.value.selectedCategoryIndex)
            }
            .onFailure { error ->
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = error.message
                )
            }
    }

    fun onCategorySelected(index: Int) {
        _state.value = _state.value.copy(selectedCategoryIndex = index)
        filterByCategory(index)
    }

    private fun filterByCategory(index: Int) {
        if (_state.value.categories.isEmpty() || index >= _state.value.categories.size) return
        val categoryName = _state.value.categories[index]
        val filtered = if (categoryName == "All") {
            _state.value.foodList
        } else {
            val category = dynamicCategories.find { it.name.equals(categoryName, ignoreCase = true) }
            val catId = category?.id?.toIntOrNull()
            _state.value.foodList.filter { it.categoryId == catId }
        }
        _state.value = _state.value.copy(
            filteredFoodList = filtered,
            totalItems = filtered.size
        )
    }

    fun deleteFood(id: Int) {
        viewModelScope.launch {
            repository.deleteFood(id)
                .onSuccess {
                    loadFoods()
                }
        }
    }
}
