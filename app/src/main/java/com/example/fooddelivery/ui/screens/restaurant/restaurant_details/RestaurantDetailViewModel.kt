package com.example.fooddelivery.ui.screens.restaurant.restaurant_details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.fooddelivery.R
import com.example.fooddelivery.domain.model.FoodItem
import com.example.fooddelivery.domain.model.Restaurant
import com.example.fooddelivery.data.remote.dto.toDomain
import com.example.fooddelivery.domain.model.Voucher
import com.example.fooddelivery.domain.repository.CartRepository
import com.example.fooddelivery.domain.repository.RestaurantRepository
import com.example.fooddelivery.domain.repository.VoucherRepository
import com.example.fooddelivery.ui.navigation.RestaurantDetailRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RestaurantDetailState(
    val restaurant: Restaurant? = null,
    val foodItems: List<FoodItem> = emptyList(),
    val categories: List<String> = emptyList(),
    val selectedCategory: String = "",
    val isLoading: Boolean = false,
    val categorizedFoodItem: Map<String, List<FoodItem>> = emptyMap(),
    val vouchers: List<Voucher> = emptyList()
)

sealed interface RestaurantDetailEvent {
    data class CategorySelected(val category: String) : RestaurantDetailEvent
    data class AddFoodToCart(val foodItem: FoodItem) : RestaurantDetailEvent
    object ToggleFavorite : RestaurantDetailEvent
}

sealed interface RestaurantDetailUiEffect {
    data class ShowSnackBar(val message: String) : RestaurantDetailUiEffect
}

@HiltViewModel
class RestaurantDetailViewModel @Inject constructor(
    private val restaurantRepository: RestaurantRepository,
    private val voucherRepository: VoucherRepository,
    private val cartRepository: CartRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val restaurantId: String = savedStateHandle.toRoute<RestaurantDetailRoute>().restaurantId
    private val _state = MutableStateFlow(RestaurantDetailState())
    val state: StateFlow<RestaurantDetailState> = _state.asStateFlow()

    private val _uiEffect = MutableSharedFlow<RestaurantDetailUiEffect>()
    val uiEffect = _uiEffect.asSharedFlow()

    init {
        loadRestaurantDetails()
    }

    fun onEvent(event: RestaurantDetailEvent) {
        when(event) {
            is RestaurantDetailEvent.CategorySelected -> {
                _state.update { it.copy(selectedCategory = event.category) }
            }
            is RestaurantDetailEvent.AddFoodToCart -> {
                addToCart(event.foodItem)
            }
            RestaurantDetailEvent.ToggleFavorite -> {
                toggleFavorite()
            }
        }
    }

    private fun toggleFavorite() {
        val currentRestaurant = _state.value.restaurant ?: return
        val id = currentRestaurant.id.toIntOrNull() ?: return

        val previousState = currentRestaurant.isLiked
        val newFavoriteStatus = !previousState
        
        _state.update { 
            it.copy(restaurant = currentRestaurant.copy(isLiked = newFavoriteStatus))
        }
        
        viewModelScope.launch {
            restaurantRepository.toggleFavorite(id).onSuccess { result ->
                _state.update { 
                    it.copy(restaurant = it.restaurant?.copy(isLiked = result.isLiked))
                }
                val message = if (result.isLiked) "Added to favorites" else "Removed from favorites"
                _uiEffect.emit(RestaurantDetailUiEffect.ShowSnackBar(message))
            }.onFailure { error ->
                _state.update { 
                    it.copy(restaurant = it.restaurant?.copy(isLiked = previousState))
                }
                _uiEffect.emit(RestaurantDetailUiEffect.ShowSnackBar(error.message ?: "Failed to update favorite"))
            }
        }
    }

    private fun addToCart(foodItem: FoodItem) {
        val foodId = foodItem.id.toIntOrNull() ?: return
        viewModelScope.launch {
            cartRepository.addToCart(foodId = foodId, quantity = 1, size = null, note = null)
                .onSuccess {
                    _uiEffect.emit(RestaurantDetailUiEffect.ShowSnackBar("${foodItem.name} added to cart"))
                }
                .onFailure { error ->
                    _uiEffect.emit(
                        RestaurantDetailUiEffect.ShowSnackBar(error.message ?: "Failed to add to cart")
                    )
                }
        }
    }

    private fun loadRestaurantDetails() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            
            val idInt = restaurantId.toIntOrNull() ?: 1
            
            // Parallel loading
            launch {
                restaurantRepository.getFoods(idInt).onSuccess { foodResponses ->
                    val apiFoodItems = foodResponses.map { dto ->
                        FoodItem(
                            id = dto.id.toString(),
                            name = dto.name,
                            restaurantId = restaurantId,
                            restaurantName = _state.value.restaurant?.name ?: "",
                            categoryId = dto.categoryId.toString(),
                            price = dto.price,
                            imageRes = R.drawable.food_bowl,
                            imageUrl = dto.image
                        )
                    }
                    
                    val foodItems = if (apiFoodItems.isEmpty()) getSeedFoodItems() else apiFoodItems
                    val categories = foodItems.map { it.categoryId }.distinct()
                    
                    _state.update {
                        it.copy(
                            foodItems = foodItems,
                            categories = categories,
                            selectedCategory = categories.firstOrNull() ?: "",
                            categorizedFoodItem = foodItems.groupBy { item -> item.categoryId }
                        )
                    }
                }.onFailure {
                    val foodItems = getSeedFoodItems()
                    val categories = foodItems.map { it.categoryId }.distinct()
                    _state.update {
                        it.copy(
                            foodItems = foodItems,
                            categories = categories,
                            selectedCategory = categories.firstOrNull() ?: "",
                            categorizedFoodItem = foodItems.groupBy { item -> item.categoryId }
                        )
                    }
                }
            }

            launch {
                voucherRepository.getVouchers(restaurantId = idInt)
                    .onSuccess { vouchersDto ->
                        _state.update { it.copy(vouchers = vouchersDto.map { dto -> dto.toDomain() }) }
                    }
            }

            launch {
                restaurantRepository.getRestaurantById(idInt).onSuccess { dto ->
                    val restaurant = Restaurant(
                        id = dto.id.toString(),
                        name = dto.name,
                        description = dto.description ?: "",
                        tags = dto.categories?.map { it.name } ?: emptyList(),
                        rating = dto.averageRating?.toFloat() ?: 0f,
                        deliveryFee = dto.deliveryFee ?: 0.0,
                        imageUrl = dto.image,
                        isLiked = dto.isLiked ?: false
                    )
                    _state.update { it.copy(restaurant = restaurant) }
                    
                    // After getting restaurant, check its specific like status
                    checkLikeStatus(idInt)
                }.onFailure {
                    if (_state.value.restaurant == null) {
                        _state.update { it.copy(restaurant = getMockRestaurant()) }
                        checkLikeStatus(idInt)
                    }
                }
            }

            _state.update { it.copy(isLoading = false) }
        }
    }

    private fun checkLikeStatus(restaurantId: Int) {
        viewModelScope.launch {
            restaurantRepository.getLikeStatus(restaurantId).onSuccess { result ->
                _state.update { 
                    it.copy(restaurant = it.restaurant?.copy(isLiked = result.isLiked))
                }
            }
        }
    }

    private fun getMockRestaurant() = Restaurant(
        id = restaurantId,
        name = "Heo Con - Cơm Gà Sốt, Da Gà & Hamburger - Đình Phong Phú",
        description = "Famous for its crispy chicken and unique sauces.",
        tags = listOf("Chicken", "Burger", "Asian"),
        rating = 4.6f,
        reviewCount = 999,
        deliveryFee = 2.0,
        isLiked = false,
        imageRes = R.drawable.food_bowl
    )

    private fun getSeedFoodItems(): List<FoodItem> {
        return listOf(
            FoodItem(
                id = "f1",
                name = "Crispy Chicken with Sauce",
                restaurantId = restaurantId,
                restaurantName = "Heo Con",
                categoryId = "Popular",
                price = 39000.0,
                soldCount = 1000,
                imageRes = R.drawable.food_bowl,
                promoTag = "1K+ Sold"
            ),
            FoodItem(
                id = "f2",
                name = "Classic Beef Burger",
                restaurantId = restaurantId,
                restaurantName = "Heo Con",
                categoryId = "Popular",
                price = 45000.0,
                soldCount = 82,
                imageRes = R.drawable.food_bowl,
                promoTag = "82 Sold"
            ),
            FoodItem(
                id = "f3",
                name = "Fried Rice with Egg",
                restaurantId = restaurantId,
                restaurantName = "Heo Con",
                categoryId = "Main Dishes",
                price = 35000.0,
                soldCount = 500,
                imageRes = R.drawable.food_bowl
            ),
            FoodItem(
                id = "f4",
                name = "Spicy Chicken Wings",
                restaurantId = restaurantId,
                restaurantName = "Heo Con",
                categoryId = "Main Dishes",
                price = 55000.0,
                soldCount = 200,
                imageRes = R.drawable.food_bowl
            ),
            FoodItem(
                id = "f5",
                name = "Coca Cola",
                restaurantId = restaurantId,
                restaurantName = "Heo Con",
                categoryId = "Drinks",
                price = 15000.0,
                soldCount = 2000,
                imageRes = R.drawable.food_bowl
            ),
            FoodItem(
                id = "f6",
                name = "Iced Milk Coffee",
                restaurantId = restaurantId,
                restaurantName = "Heo Con",
                categoryId = "Drinks",
                price = 25000.0,
                soldCount = 300,
                imageRes = R.drawable.food_bowl
            )
        )
    }
}
