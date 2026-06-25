package com.example.fooddelivery.ui.screens.food

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.fooddelivery.domain.model.FoodItem
import com.example.fooddelivery.domain.model.Restaurant
import com.example.fooddelivery.domain.repository.CartRepository
import com.example.fooddelivery.domain.repository.FoodRepository
import com.example.fooddelivery.domain.repository.RestaurantRepository
import com.example.fooddelivery.ui.navigation.FoodDetailRoute
import com.example.fooddelivery.ui.utils.GlobalSnackbarManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FoodSizeOption(
    val foodSizeId: Int,
    val name: String,
    val price: Double,
    val isDefault: Boolean = false
)

data class FoodIngredient(
    val id: Int,
    val name: String,
    val iconUrl: String? = null
)

data class FoodDetailState(
    val food: FoodItem? = null,
    val restaurant: Restaurant? = null,
    val foodDescription: String = "",
    val quantity: Int = 1,
    val sizes: List<FoodSizeOption> = emptyList(),
    val selectedFoodSizeId: Int? = null,
    val selectedSize: String = "",
    val ingredients: List<FoodIngredient> = emptyList(),
    val totalPrice: Double = 0.0,
    val isFavorite: Boolean = false,
    val isLoading: Boolean = false,
    val isAddingToCart: Boolean = false,
    val errorMessage: String? = null
)

sealed interface FoodDetailEvent {
    data class UpdateQuantity(val delta: Int) : FoodDetailEvent
    data class SelectSize(val foodSizeId: Int, val sizeName: String, val price: Double) : FoodDetailEvent
    data object ToggleFavorite : FoodDetailEvent
    data object AddToCart : FoodDetailEvent
}

sealed interface FoodDetailUiEffect {
    data object NavigateBack : FoodDetailUiEffect
}

@HiltViewModel
class FoodDetailViewModel @Inject constructor(
    private val foodRepository: FoodRepository,
    private val restaurantRepository: RestaurantRepository,
    private val cartRepository: CartRepository,
    private val snackbarManager: GlobalSnackbarManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val foodId: String = savedStateHandle.toRoute<FoodDetailRoute>().foodId
    private val _state = MutableStateFlow(FoodDetailState())
    val state: StateFlow<FoodDetailState> = _state.asStateFlow()

    private val _uiEffect = MutableSharedFlow<FoodDetailUiEffect>()
    val uiEffect = _uiEffect.asSharedFlow()

    init {
        loadFoodDetail()
    }

    fun onEvent(event: FoodDetailEvent) {
        when (event) {
            is FoodDetailEvent.UpdateQuantity -> {
                _state.update {
                    val newQuantity = (it.quantity + event.delta).coerceAtLeast(1)
                    val unitPrice = selectedUnitPrice(it)
                    it.copy(
                        quantity = newQuantity,
                        totalPrice = unitPrice * newQuantity
                    )
                }
            }
            is FoodDetailEvent.SelectSize -> {
                _state.update {
                    it.copy(
                        selectedFoodSizeId = event.foodSizeId,
                        selectedSize = event.sizeName,
                        totalPrice = event.price * it.quantity
                    )
                }
            }
            is FoodDetailEvent.ToggleFavorite -> {
                _state.update { it.copy(isFavorite = !it.isFavorite) }
            }
            is FoodDetailEvent.AddToCart -> {
                val currentState = _state.value
                val food = currentState.food ?: return

                _state.update { it.copy(isAddingToCart = true) }
                viewModelScope.launch {
                    val result = cartRepository.addToCart(
                        foodId = food.id.toIntOrNull() ?: 0,
                        quantity = currentState.quantity,
                        size = currentState.selectedFoodSizeId?.toString(),
                        note = null
                    )

                    result.onSuccess {
                        _state.update { it.copy(isAddingToCart = false) }
                        val sizeLabel = currentState.selectedSize.ifBlank { "default" }
                        snackbarManager.showSnackbar("Added ${food.name} ($sizeLabel) to cart")
                        _uiEffect.emit(FoodDetailUiEffect.NavigateBack)
                    }.onFailure { error ->
                        _state.update { it.copy(isAddingToCart = false) }
                        snackbarManager.showSnackbar(error.message ?: "Failed to add to cart")
                    }
                }
            }
        }
    }

    private fun loadFoodDetail() {
        val idInt = foodId.toIntOrNull()
        if (idInt == null) {
            _state.update { it.copy(errorMessage = "Invalid food id") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }

            foodRepository.getFoodById(idInt)
                .onSuccess { dto ->
                    val defaultSize = dto.sizes?.firstOrNull { it.isDefault }
                        ?: dto.sizes?.firstOrNull()
                    val unitPrice = defaultSize?.price ?: dto.price
                    val sizes = dto.sizes?.map { size ->
                        FoodSizeOption(
                            foodSizeId = size.foodSizeId,
                            name = size.name,
                            price = size.price,
                            isDefault = size.isDefault
                        )
                    } ?: emptyList()

                    val foodItem = FoodItem(
                        id = dto.id.toString(),
                        name = dto.name,
                        restaurantId = dto.restaurantId.toString(),
                        restaurantName = dto.restaurant?.name ?: "",
                        categoryId = dto.categoryId.toString(),
                        price = unitPrice,
                        rating = dto.rating ?: 0f,
                        reviewCount = dto.reviewCount ?: 0,
                        imageUrl = dto.image,
                        promoTag = dto.label
                    )

                    val ingredients = dto.foodIngredients?.map { ingredient ->
                        FoodIngredient(
                            id = ingredient.id,
                            name = ingredient.name,
                            iconUrl = ingredient.icon
                        )
                    } ?: emptyList()

                    val restaurant = Restaurant(
                        id = dto.restaurantId.toString(),
                        name = dto.restaurant?.name ?: "",
                        description = dto.description,
                        tags = dto.category?.let { listOf(it.name) } ?: emptyList(),
                        rating = dto.rating ?: 0f,
                        deliveryFee = 0.0,
                        imageUrl = dto.restaurant?.image
                    )

                    _state.update {
                        it.copy(
                            food = foodItem,
                            restaurant = restaurant,
                            foodDescription = dto.description,
                            sizes = sizes,
                            selectedFoodSizeId = defaultSize?.foodSizeId,
                            selectedSize = defaultSize?.name ?: "",
                            ingredients = ingredients,
                            quantity = 1,
                            totalPrice = unitPrice,
                            isLoading = false
                        )
                    }

                    loadRestaurantDetails(dto.restaurantId)
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Failed to load food details"
                        )
                    }
                    snackbarManager.showSnackbar(error.message ?: "Failed to load food details")
                }
        }
    }

    private fun loadRestaurantDetails(restaurantId: Int) {
        viewModelScope.launch {
            restaurantRepository.getRestaurantById(restaurantId).onSuccess { dto ->
                _state.update { current ->
                    current.copy(
                        restaurant = current.restaurant?.copy(
                            name = dto.name.ifBlank { current.restaurant.name },
                            description = dto.description ?: current.foodDescription,
                            tags = dto.categories?.map { it.name } ?: current.restaurant.tags,
                            rating = dto.averageRating?.toFloat() ?: current.restaurant.rating,
                            deliveryFee = dto.deliveryFee ?: current.restaurant.deliveryFee,
                            imageUrl = dto.image?.ifBlank { current.restaurant.imageUrl },
                            reviewCount = dto.ratingCount ?: current.restaurant.reviewCount
                        ),
                        food = current.food?.copy(
                            restaurantName = dto.name.ifBlank { current.food.restaurantName }
                        )
                    )
                }
            }
        }
    }

    private fun selectedUnitPrice(state: FoodDetailState): Double {
        val selectedSize = state.sizes.firstOrNull { it.foodSizeId == state.selectedFoodSizeId }
        return selectedSize?.price ?: state.food?.price ?: 0.0
    }
}
