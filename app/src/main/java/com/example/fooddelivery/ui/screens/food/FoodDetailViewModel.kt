package com.example.fooddelivery.ui.screens.food

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.fooddelivery.data.remote.dto.toDomain
import com.example.fooddelivery.domain.config.VoucherFeatureFlags
import com.example.fooddelivery.domain.model.CartItem
import com.example.fooddelivery.domain.model.FoodItem
import com.example.fooddelivery.domain.model.Restaurant
import com.example.fooddelivery.domain.model.Voucher
import com.example.fooddelivery.domain.model.VoucherType
import com.example.fooddelivery.domain.repository.CartRepository
import com.example.fooddelivery.domain.repository.FoodRepository
import com.example.fooddelivery.domain.repository.RestaurantRepository
import com.example.fooddelivery.domain.repository.VoucherRepository
import com.example.fooddelivery.domain.util.FoodShareTextBuilder
import com.example.fooddelivery.domain.util.DiscountBadgeVisual
import com.example.fooddelivery.domain.util.pickBestDiscountBadgeVisual
import com.example.fooddelivery.domain.util.toDiscountBadgeVisual
import com.example.fooddelivery.ui.navigation.FoodDetailRoute
import com.example.fooddelivery.ui.utils.GlobalSnackbarManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
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
    val iconKey: String? = null
)

data class FoodDetailState(
    val food: FoodItem? = null,
    val restaurant: Restaurant? = null,
    val foodDescription: String = "",
    val sizes: List<FoodSizeOption> = emptyList(),
    val selectedFoodSizeId: Int? = null,
    val selectedSize: String = "",
    val ingredients: List<FoodIngredient> = emptyList(),
    val unitPrice: Double = 0.0,
    val vouchers: List<Voucher> = emptyList(),
    val isLoading: Boolean = false,
    val isAddingToCart: Boolean = false,
    val showAddSuccessPulse: Boolean = false,
    val showSizeSheet: Boolean = false,
    val sheetSelectedSizeId: Int? = null,
    val sheetQuantity: Int = 1,
    val restaurantCartItemCount: Int = 0,
    val restaurantCartSubtotal: Double = 0.0,
    val errorMessage: String? = null
) {
    val discountBadge: DiscountBadgeVisual?
        get() = vouchers.pickBestDiscountBadgeVisual()
            ?: food?.promoTag?.toDiscountBadgeVisual()

    val originalPrice: Double?
        get() {
            val percentDiscount = vouchers
                .filter { it.type == VoucherType.PERCENT }
                .maxByOrNull { it.discountAmount }
                ?.discountAmount
                ?: promoTagPercentDiscount()
            if (percentDiscount == null || percentDiscount <= 0.0 || percentDiscount >= 100.0) {
                return null
            }
            return unitPrice / (1.0 - percentDiscount / 100.0)
        }

    private fun promoTagPercentDiscount(): Double? {
        val tag = food?.promoTag ?: return null
        val match = Regex("""(\d+(?:\.\d+)?)\s*%""").find(tag) ?: return null
        return match.groupValues[1].toDoubleOrNull()
    }
}

sealed interface FoodDetailEvent {
    data class SelectSize(val foodSizeId: Int, val sizeName: String, val price: Double) : FoodDetailEvent
    data class SelectSheetSize(val foodSizeId: Int) : FoodDetailEvent
    data class UpdateSheetQuantity(val quantity: Int) : FoodDetailEvent
    data object QuickAdd : FoodDetailEvent
    data object ConfirmAddFromSheet : FoodDetailEvent
    data object DismissSizeSheet : FoodDetailEvent
    data object ShareFood : FoodDetailEvent
    data object ClearAddSuccessPulse : FoodDetailEvent
}

sealed interface FoodDetailUiEffect {
    data class LaunchShare(val text: String, val subject: String) : FoodDetailUiEffect
}

@HiltViewModel
class FoodDetailViewModel @Inject constructor(
    private val foodRepository: FoodRepository,
    private val restaurantRepository: RestaurantRepository,
    private val cartRepository: CartRepository,
    private val voucherRepository: VoucherRepository,
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
        observeRestaurantCart()
    }

    fun onEvent(event: FoodDetailEvent) {
        when (event) {
            is FoodDetailEvent.SelectSize -> {
                _state.update {
                    it.copy(
                        selectedFoodSizeId = event.foodSizeId,
                        selectedSize = event.sizeName,
                        unitPrice = event.price
                    )
                }
            }
            is FoodDetailEvent.SelectSheetSize -> {
                _state.update { it.copy(sheetSelectedSizeId = event.foodSizeId) }
            }
            is FoodDetailEvent.UpdateSheetQuantity -> {
                val qty = event.quantity.coerceIn(1, 99)
                _state.update { it.copy(sheetQuantity = qty) }
            }
            FoodDetailEvent.QuickAdd -> handleQuickAdd()
            FoodDetailEvent.ConfirmAddFromSheet -> handleConfirmAddFromSheet()
            FoodDetailEvent.DismissSizeSheet -> {
                _state.update {
                    it.copy(showSizeSheet = false, sheetSelectedSizeId = null, sheetQuantity = 1)
                }
            }
            FoodDetailEvent.ShareFood -> shareFood()
            FoodDetailEvent.ClearAddSuccessPulse -> {
                _state.update { it.copy(showAddSuccessPulse = false) }
            }
        }
    }

    private fun observeRestaurantCart() {
        viewModelScope.launch {
            combine(
                _state.map { it.food?.restaurantId },
                cartRepository.cartItems
            ) { restaurantId, items -> restaurantId to items }
                .collectLatest { (restaurantId, items) ->
                    if (restaurantId == null) return@collectLatest
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

    private fun handleQuickAdd() {
        val current = _state.value
        if (current.food == null || current.isAddingToCart) return

        val defaultSize = current.sizes.firstOrNull { it.isDefault }
            ?: current.sizes.firstOrNull()

        _state.update {
            it.copy(
                showSizeSheet = true,
                sheetSelectedSizeId = defaultSize?.foodSizeId,
                sheetQuantity = 1
            )
        }
    }

    private fun handleConfirmAddFromSheet() {
        val current = _state.value
        val selectedSize = if (current.sizes.isNotEmpty()) {
            val sizeId = current.sheetSelectedSizeId ?: return
            current.sizes.firstOrNull { it.foodSizeId == sizeId } ?: return
        } else {
            null
        }

        if (selectedSize != null) {
            _state.update {
                it.copy(
                    selectedFoodSizeId = selectedSize.foodSizeId,
                    selectedSize = selectedSize.name,
                    unitPrice = selectedSize.price
                )
            }
        }

        performOptimisticAdd(
            foodSizeId = selectedSize?.foodSizeId,
            sizeName = selectedSize?.name.orEmpty(),
            unitPrice = selectedSize?.price ?: current.unitPrice,
            quantity = current.sheetQuantity
        )
    }

    private fun performOptimisticAdd(
        foodSizeId: Int?,
        sizeName: String,
        unitPrice: Double,
        quantity: Int
    ) {
        val current = _state.value
        val food = current.food ?: return
        if (current.isAddingToCart) return

        val foodIdInt = food.id.toIntOrNull() ?: return
        val sizeLabel = sizeName.ifBlank { "default" }

        _state.update {
            it.copy(
                isAddingToCart = true,
                showAddSuccessPulse = true,
                showSizeSheet = false,
                sheetSelectedSizeId = null,
                sheetQuantity = 1
            )
        }
        viewModelScope.launch {
            snackbarManager.showSnackbar("Added ${food.name} ($sizeLabel) to cart")
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
            }.onFailure {
                _state.update { it.copy(isAddingToCart = false, showAddSuccessPulse = false) }
                viewModelScope.launch {
                    snackbarManager.showSnackbar("Couldn't add to cart. Please try again.")
                }
            }
        }
    }

    private fun shareFood() {
        val current = _state.value
        val food = current.food ?: return
        val text = FoodShareTextBuilder.build(
            food = food,
            unitPrice = current.unitPrice,
            vouchers = current.vouchers
        )
        viewModelScope.launch {
            _uiEffect.emit(
                FoodDetailUiEffect.LaunchShare(
                    text = text,
                    subject = food.name
                )
            )
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
                        soldCount = dto.totalQuantity,
                        imageUrl = dto.image,
                        promoTag = dto.label
                    )

                    val ingredients = dto.foodIngredients?.map { ingredient ->
                        FoodIngredient(
                            id = ingredient.id,
                            name = ingredient.name,
                            iconKey = ingredient.icon
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
                            selectedFoodSizeId = null,
                            selectedSize = "",
                            ingredients = ingredients,
                            unitPrice = unitPrice,
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

            if (VoucherFeatureFlags.RESTAURANT_PUBLIC_VOUCHERS_ENABLED) {
                voucherRepository.getCustomerVouchers(restaurantId)
                    .onSuccess { vouchersDto ->
                        _state.update {
                            it.copy(vouchers = vouchersDto.map { dto -> dto.toDomain() })
                        }
                    }
            }
        }
    }

    companion object {
        private const val OPTIMISTIC_CART_ITEM_ID = -1
    }
}
