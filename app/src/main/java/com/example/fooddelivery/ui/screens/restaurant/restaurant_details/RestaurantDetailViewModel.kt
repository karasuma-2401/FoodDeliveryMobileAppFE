package com.example.fooddelivery.ui.screens.restaurant.restaurant_details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.fooddelivery.R
import com.example.fooddelivery.domain.model.CartItem
import com.example.fooddelivery.domain.model.FoodItem
import com.example.fooddelivery.domain.model.Restaurant
import com.example.fooddelivery.data.remote.dto.toDomain
import com.example.fooddelivery.domain.model.Voucher
import com.example.fooddelivery.domain.config.VoucherFeatureFlags
import com.example.fooddelivery.domain.repository.CartRepository
import com.example.fooddelivery.domain.repository.FoodRepository
import com.example.fooddelivery.domain.repository.RestaurantRepository
import com.example.fooddelivery.domain.repository.VoucherRepository
import com.example.fooddelivery.domain.util.RestaurantShareTextBuilder
import com.example.fooddelivery.ui.navigation.RestaurantDetailRoute
import com.example.fooddelivery.ui.screens.food.FoodSizeOption
import com.example.fooddelivery.data.local.datastore.TokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import javax.inject.Inject

data class AddToCartSheetState(
    val foodItem: FoodItem,
    val description: String = "",
    val sizes: List<FoodSizeOption> = emptyList(),
    val selectedSizeId: Int? = null,
    val quantity: Int = 1,
    val isLoadingDetails: Boolean = false
)

data class RestaurantDetailState(
    val restaurant: Restaurant? = null,
    val foodItems: List<FoodItem> = emptyList(),
    val categories: List<String> = emptyList(),
    val selectedCategory: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val foodsLoadError: String? = null,
    val categorizedFoodItem: Map<String, List<FoodItem>> = emptyMap(),
    val vouchers: List<Voucher> = emptyList(),
    val showAddToCartSheet: Boolean = false,
    val addToCartSheet: AddToCartSheetState? = null,
    val isAddingToCart: Boolean = false,
    val restaurantCartItemCount: Int = 0,
    val restaurantCartSubtotal: Double = 0.0,
    val isAdmin: Boolean = false
)

sealed interface RestaurantDetailEvent {
    data class CategorySelected(val category: String) : RestaurantDetailEvent
    data class OpenAddToCartSheet(val foodItem: FoodItem) : RestaurantDetailEvent
    data object DismissAddToCartSheet : RestaurantDetailEvent
    data class SelectSheetSize(val foodSizeId: Int) : RestaurantDetailEvent
    data class UpdateSheetQuantity(val quantity: Int) : RestaurantDetailEvent
    data object ConfirmAddToCart : RestaurantDetailEvent
    data object ToggleFavorite : RestaurantDetailEvent
    data object ShareRestaurant : RestaurantDetailEvent
}

sealed interface RestaurantDetailUiEffect {
    data class ShowSnackBar(val message: String) : RestaurantDetailUiEffect
    data class LaunchShare(val text: String, val subject: String) : RestaurantDetailUiEffect
}

@HiltViewModel
class RestaurantDetailViewModel @Inject constructor(
    private val restaurantRepository: RestaurantRepository,
    private val voucherRepository: VoucherRepository,
    private val cartRepository: CartRepository,
    private val foodRepository: FoodRepository,
    private val tokenManager: TokenManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val restaurantId: String = savedStateHandle.toRoute<RestaurantDetailRoute>().restaurantId
    private val _state = MutableStateFlow(RestaurantDetailState())
    val state: StateFlow<RestaurantDetailState> = _state.asStateFlow()

    private val _uiEffect = MutableSharedFlow<RestaurantDetailUiEffect>()
    val uiEffect = _uiEffect.asSharedFlow()

    init {
        loadRestaurantDetails()
        observeRestaurantCart()
        checkUserRole()
    }

    private fun checkUserRole() {
        viewModelScope.launch {
            tokenManager.getUserRoles.collectLatest { roles ->
                val isAdmin = roles.any { it.equals("ADMIN", ignoreCase = true) }
                _state.update { it.copy(isAdmin = isAdmin) }
            }
        }
    }

    fun onEvent(event: RestaurantDetailEvent) {
        when (event) {
            is RestaurantDetailEvent.CategorySelected -> {
                _state.update { it.copy(selectedCategory = event.category) }
            }
            is RestaurantDetailEvent.OpenAddToCartSheet -> openAddToCartSheet(event.foodItem)
            RestaurantDetailEvent.DismissAddToCartSheet -> {
                _state.update {
                    it.copy(showAddToCartSheet = false, addToCartSheet = null)
                }
            }
            is RestaurantDetailEvent.SelectSheetSize -> {
                _state.update { current ->
                    val sheet = current.addToCartSheet ?: return@update current
                    current.copy(addToCartSheet = sheet.copy(selectedSizeId = event.foodSizeId))
                }
            }
            is RestaurantDetailEvent.UpdateSheetQuantity -> {
                val qty = event.quantity.coerceIn(1, 99)
                _state.update { current ->
                    val sheet = current.addToCartSheet ?: return@update current
                    current.copy(addToCartSheet = sheet.copy(quantity = qty))
                }
            }
            RestaurantDetailEvent.ConfirmAddToCart -> confirmAddToCart()
            RestaurantDetailEvent.ToggleFavorite -> toggleFavorite()
            RestaurantDetailEvent.ShareRestaurant -> shareRestaurant()
        }
    }

    private fun observeRestaurantCart() {
        viewModelScope.launch {
            cartRepository.cartItems.collectLatest { items ->
                val restaurantItems = items.filter { it.restaurantId == restaurantId }
                _state.update {
                    it.copy(
                        restaurantCartItemCount = restaurantItems.sumOf { item -> item.quantity },
                        restaurantCartSubtotal = restaurantItems.sumOf { item -> item.lineTotal }
                    )
                }
            }
        }
    }

    private fun openAddToCartSheet(foodItem: FoodItem) {
        val foodId = foodItem.id.toIntOrNull() ?: return
        _state.update {
            it.copy(
                showAddToCartSheet = true,
                addToCartSheet = AddToCartSheetState(
                    foodItem = foodItem,
                    isLoadingDetails = true,
                    quantity = 1
                )
            )
        }

        viewModelScope.launch {
            foodRepository.getFoodById(foodId)
                .onSuccess { dto ->
                    val sizes = dto.sizes?.map { size ->
                        FoodSizeOption(
                            foodSizeId = size.foodSizeId,
                            name = size.name,
                            price = size.price,
                            isDefault = size.isDefault
                        )
                    } ?: emptyList()
                    val defaultSize = sizes.firstOrNull { it.isDefault } ?: sizes.firstOrNull()

                    _state.update { current ->
                        val sheet = current.addToCartSheet ?: return@update current
                        current.copy(
                            addToCartSheet = sheet.copy(
                                description = dto.description,
                                sizes = sizes,
                                selectedSizeId = defaultSize?.foodSizeId,
                                foodItem = foodItem.copy(
                                    price = defaultSize?.price ?: dto.price,
                                    imageUrl = dto.image ?: foodItem.imageUrl
                                ),
                                isLoadingDetails = false
                            )
                        )
                    }
                }
                .onFailure {
                    _state.update { current ->
                        val sheet = current.addToCartSheet ?: return@update current
                        current.copy(
                            addToCartSheet = sheet.copy(isLoadingDetails = false),
                            showAddToCartSheet = false
                        )
                    }
                    _uiEffect.emit(
                        RestaurantDetailUiEffect.ShowSnackBar("Couldn't load item details")
                    )
                }
        }
    }

    private fun confirmAddToCart() {
        val current = _state.value
        val sheet = current.addToCartSheet ?: return
        if (current.isAddingToCart) return

        val food = sheet.foodItem
        val foodIdInt = food.id.toIntOrNull() ?: return
        val selectedSize = sheet.sizes.firstOrNull { it.foodSizeId == sheet.selectedSizeId }
        if (sheet.sizes.isNotEmpty() && selectedSize == null) return

        val unitPrice = selectedSize?.price ?: food.price
        val sizeName = selectedSize?.name.orEmpty()
        val foodSizeId = selectedSize?.foodSizeId
        val quantity = sheet.quantity

        _state.update {
            it.copy(
                isAddingToCart = true,
                showAddToCartSheet = false,
                addToCartSheet = null,
            )
        }

        val optimisticItem = CartItem(
            food = food.copy(price = unitPrice, size = sizeName.takeIf { it.isNotBlank() }),
            quantity = quantity,
            lineTotal = unitPrice * quantity,
            restaurantId = food.restaurantId,
            restaurantName = food.restaurantName,
            cartItemId = OPTIMISTIC_CART_ITEM_ID,
            foodSizeId = foodSizeId?.toString()
        )

        viewModelScope.launch {
            cartRepository.addToCartWithOptimisticLocal(
                foodId = foodIdInt,
                quantity = quantity,
                foodSizeId = foodSizeId,
                note = null,
                optimisticItem = optimisticItem
            ).onSuccess {
                _state.update { it.copy(isAddingToCart = false) }
                _uiEffect.emit(
                    RestaurantDetailUiEffect.ShowSnackBar("${food.name} added to cart")
                )
            }.onFailure { error ->
                _state.update { it.copy(isAddingToCart = false) }
                _uiEffect.emit(
                    RestaurantDetailUiEffect.ShowSnackBar(
                        error.message ?: "Failed to add to cart"
                    )
                )
            }
        }
    }

    private fun shareRestaurant() {
        val restaurant = _state.value.restaurant ?: return
        val text = RestaurantShareTextBuilder.build(
            restaurant = restaurant,
            vouchers = _state.value.vouchers
        )
        viewModelScope.launch {
            _uiEffect.emit(
                RestaurantDetailUiEffect.LaunchShare(
                    text = text,
                    subject = restaurant.name
                )
            )
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

    private fun loadRestaurantDetails() {
        viewModelScope.launch {
            _state.update {
                it.copy(isLoading = true, errorMessage = null, foodsLoadError = null)
            }

            val idInt = restaurantId.toIntOrNull()
            if (idInt == null) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Invalid restaurant"
                    )
                }
                return@launch
            }

            try {
                supervisorScope {
                    launch {
                        restaurantRepository.getFoods(idInt).onSuccess { foodResponses ->
                            val foodItems = foodResponses.map { dto ->
                                FoodItem(
                                    id = dto.id.toString(),
                                    name = dto.name,
                                    restaurantId = restaurantId,
                                    restaurantName = _state.value.restaurant?.name ?: "",
                                    categoryId = dto.category?.name ?: "Others",
                                    price = dto.price,
                                    imageRes = R.drawable.food_bowl,
                                    imageUrl = dto.image
                                )
                            }
                            applyFoodItems(foodItems)
                        }.onFailure { error ->
                            clearFoodItems(
                                foodsLoadError = error.message ?: "Failed to load menu"
                            )
                        }
                    }

                    if (VoucherFeatureFlags.RESTAURANT_PUBLIC_VOUCHERS_ENABLED) {
                        launch {
                            voucherRepository.getCustomerVouchers(idInt)
                                .onSuccess { vouchersDto ->
                                    _state.update {
                                        it.copy(vouchers = vouchersDto.map { dto -> dto.toDomain() })
                                    }
                                }
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
                                reviewCount = dto.ratingCount ?: 0,
                                deliveryFee = dto.deliveryFee ?: 0.0,
                                imageUrl = dto.image,
                                isLiked = dto.isLiked ?: false
                            )
                            _state.update { current ->
                                val updatedFoodItems = current.foodItems.map {
                                    it.copy(restaurantName = restaurant.name)
                                }
                                current.copy(
                                    restaurant = restaurant,
                                    foodItems = updatedFoodItems,
                                    categorizedFoodItem = updatedFoodItems.groupBy { item -> item.categoryId }
                                )
                            }
                            checkLikeStatus(idInt)
                        }.onFailure { error ->
                            _state.update {
                                it.copy(
                                    errorMessage = error.message ?: "Failed to load restaurant"
                                )
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                try {
                    _uiEffect.emit(
                        RestaurantDetailUiEffect.ShowSnackBar(
                            e.localizedMessage ?: "Failed to load restaurant details"
                        )
                    )
                } catch (_: Exception) {
                }
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun applyFoodItems(foodItems: List<FoodItem>) {
        val categories = foodItems.map { it.categoryId }.distinct()
        _state.update {
            it.copy(
                foodItems = foodItems,
                categories = categories,
                selectedCategory = categories.firstOrNull() ?: "",
                categorizedFoodItem = foodItems.groupBy { item -> item.categoryId },
                foodsLoadError = null
            )
        }
    }

    private fun clearFoodItems(foodsLoadError: String? = null) {
        _state.update {
            it.copy(
                foodItems = emptyList(),
                categories = emptyList(),
                selectedCategory = "",
                categorizedFoodItem = emptyMap(),
                foodsLoadError = foodsLoadError
            )
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

    companion object {
        private const val OPTIMISTIC_CART_ITEM_ID = -1
    }
}
