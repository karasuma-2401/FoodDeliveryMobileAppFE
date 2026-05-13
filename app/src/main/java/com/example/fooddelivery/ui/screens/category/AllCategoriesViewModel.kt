package com.example.fooddelivery.ui.screens.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fooddelivery.R
import com.example.fooddelivery.domain.model.Category
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AllCategoriesState(
    val categories: List<Category> = emptyList(),
    val isLoading: Boolean = false
)
@HiltViewModel
class AllCategoriesViewModel @Inject constructor() : ViewModel() {
    private val _state = MutableStateFlow(AllCategoriesState())
    val state: StateFlow<AllCategoriesState> = _state.asStateFlow()

    init {
        loadAllCategories()
    }
    private fun loadAllCategories() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            delay(600)
            val mockData = listOf(
                Category(id = "1", name = "Burger", imageRes = R.drawable.food_bowl, startingPrice = 5.0, promoText = "PROMO"),
                Category(id = "2", name = "Pizza", imageRes = R.drawable.food_bowl, startingPrice = 8.0, promoText = "Giảm 10%"),
                Category(id = "3", name = "Drink", imageRes = R.drawable.food_bowl, startingPrice = 2.0),
                Category(id = "4", name = "Sushi", imageRes = R.drawable.food_bowl, startingPrice = 12.0),
                Category(id = "5", name = "Dessert", imageRes = R.drawable.food_bowl, startingPrice = 4.0),
                Category(id = "6", name = "Chicken", imageRes = R.drawable.food_bowl, startingPrice = 6.0),
                Category(id = "7", name = "Healthy", imageRes = R.drawable.food_bowl, startingPrice = 10.0),
                Category(id = "8", name = "Noodles", imageRes = R.drawable.food_bowl, startingPrice = 5.0),
                Category(id = "9", name = "Seafood", imageRes = R.drawable.food_bowl, startingPrice = 15.0)
            )
            _state.update { it.copy(categories = mockData, isLoading = false) }
        }
    }
}