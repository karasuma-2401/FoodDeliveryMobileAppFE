package com.example.fooddelivery.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fooddelivery.R
import com.example.fooddelivery.domain.model.Category
import com.example.fooddelivery.domain.model.Restaurant
import com.example.fooddelivery.domain.model.User
import com.example.fooddelivery.domain.repository.CartRepository
import com.example.fooddelivery.domain.usecase.GetUserProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeBanner(
    val id: String,
    val title: String,
    val description: String,
    val imageRes: Int,
    val targetType: BannerTarget,
    val targetId: String,
    val backgroundColor: Long = 0xFFFF8142
)

enum class BannerTarget {
    CATEGORY, RESTAURANT, FOOD
}

data class HomeState(
    val user: User = User(),
    val banners: List<HomeBanner> = emptyList(),
    val categories: List<Category> = emptyList(),
    val restaurants: List<Restaurant> = emptyList(),
    val cartItemCount: Int = 0,
    val selectedLocation: String = "Home",
    val availableLocations: List<String> = listOf("Home", "Work", "Other"),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

sealed interface HomeEvent {
    object LoadHomeData : HomeEvent
    object CartClicked : HomeEvent
    data class LocationSelected(val location: String) : HomeEvent
    data class CategoryClicked(val categoryId: String) : HomeEvent
    data class RestaurantClicked(val restaurantId: String) : HomeEvent
    data class BannerClicked(val banner: HomeBanner) : HomeEvent
    object SeeAllCategoriesClicked : HomeEvent
    object SeeAllRestaurantsClicked : HomeEvent
}

sealed interface HomeUiEffect {
    object NavigateToCart : HomeUiEffect
    object NavigateToAllCategories : HomeUiEffect
    object NavigateToAllRestaurants : HomeUiEffect
    data class NavigateToCategory(val categoryId: String) : HomeUiEffect
    data class NavigateToRestaurant(val restaurantId: String) : HomeUiEffect
    data class NavigateToFoodDetail(val foodId: String) : HomeUiEffect
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val cartRepository: CartRepository
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<HomeUiEffect>()
    val effect: SharedFlow<HomeUiEffect> = _effect.asSharedFlow()

    init {
        loadData()
        observeCart()
    }

    private fun observeCart() {
        viewModelScope.launch {
            cartRepository.cartItems.collectLatest { items ->
                val totalCount = items.sumOf { it.quantity }
                _state.update { it.copy(cartItemCount = totalCount) }
            }
        }
    }

    fun onEvent(event: HomeEvent) {
        viewModelScope.launch {
            when (event) {
                HomeEvent.LoadHomeData -> loadData()
                HomeEvent.CartClicked -> _effect.emit(HomeUiEffect.NavigateToCart)
                is HomeEvent.LocationSelected -> {
                    _state.update { it.copy(selectedLocation = event.location) }
                }
                is HomeEvent.CategoryClicked -> _effect.emit(HomeUiEffect.NavigateToCategory(event.categoryId))
                is HomeEvent.RestaurantClicked -> _effect.emit(HomeUiEffect.NavigateToRestaurant(event.restaurantId))
                is HomeEvent.BannerClicked -> {
                    when (event.banner.targetType) {
                        BannerTarget.CATEGORY -> _effect.emit(HomeUiEffect.NavigateToCategory(event.banner.targetId))
                        BannerTarget.RESTAURANT -> _effect.emit(HomeUiEffect.NavigateToRestaurant(event.banner.targetId))
                        BannerTarget.FOOD -> _effect.emit(HomeUiEffect.NavigateToFoodDetail(event.banner.targetId))
                    }
                }
                HomeEvent.SeeAllCategoriesClicked -> _effect.emit(HomeUiEffect.NavigateToAllCategories)
                HomeEvent.SeeAllRestaurantsClicked -> _effect.emit(HomeUiEffect.NavigateToAllRestaurants)
            }
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            // Fetch user profile
            getUserProfileUseCase().onSuccess { user ->
                _state.update { it.copy(user = user) }
            }.onFailure {
                // Fallback or error handling
            }

            delay(1000)

            val mockBanners = listOf(
                HomeBanner("1", "Flash Sale 50%", "Pizza Hut Special Deal", R.drawable.food_bowl, BannerTarget.RESTAURANT, "3", 0xFFFF8142),
                HomeBanner("2", "Burger Day", "Buy 1 Get 1 Free Today", R.drawable.food_bowl, BannerTarget.CATEGORY, "2", 0xFF4CAF50),
                HomeBanner("3", "Pasta Lovers", "New Italian Dishes in Town", R.drawable.food_bowl, BannerTarget.FOOD, "9", 0xFF2196F3),
                HomeBanner("4", "Drink Free", "Orders above $20 get Free Coke", R.drawable.food_bowl, BannerTarget.CATEGORY, "4", 0xFF9C27B0),
                HomeBanner("5", "Spicy Chicken", "Spicy Restaurant Promo 30%", R.drawable.food_bowl, BannerTarget.RESTAURANT, "1", 0xFFF44336),
                HomeBanner("6", "Healthy Salads", "Fresh & Green Veggie Mix", R.drawable.food_bowl, BannerTarget.CATEGORY, "5", 0xFF009688),
                HomeBanner("7", "Family Deal", "Pizza & Drinks for 4 People", R.drawable.food_bowl, BannerTarget.FOOD, "10", 0xFFFFC107),
                HomeBanner("8", "KFC Special", "Crunchy Fried Chicken Bucket", R.drawable.food_bowl, BannerTarget.RESTAURANT, "2", 0xFF795548),
                HomeBanner("9", "Dessert Night", "20% off on all Sweet Cakes", R.drawable.food_bowl, BannerTarget.FOOD, "11", 0xFFE91E63),
                HomeBanner("10", "Seafood Fest", "New Seafood Menu Available", R.drawable.food_bowl, BannerTarget.RESTAURANT, "1", 0xFF607D8B)
            )

            val mockCategories = listOf(
                Category(id = "1", name = "Pizza", imageRes = R.drawable.food_bowl, startingPrice = 70.0, promoText = "Discount 20%"),
                Category(id = "2", name = "Burger", imageRes = R.drawable.food_bowl, startingPrice = 50.0, promoText = "PROMO"),
                Category(id = "3", name = "Pasta", imageRes = R.drawable.food_bowl, startingPrice = 60.0),
                Category(id = "4", name = "Drink", imageRes = R.drawable.food_bowl, startingPrice = 20.0),
                Category(id = "5", name = "Chicken", imageRes = R.drawable.food_bowl, startingPrice = 45.0)
            )
            
            val mockRestaurants = listOf(
                Restaurant(
                    id = "1",
                    name = "Rose Garden Restaurant",
                    tags = listOf("Burger", "Chicken", "Rice", "Wings"),
                    rating = 4.7f,
                    deliveryFee = 0.0,
                    deliveryTime = "20 min",
                    imageRes = R.drawable.food_bowl,
                    promoTags = listOf("PROMO", "Freeship")
                ),
                Restaurant(
                    id = "2",
                    name = "KFC - Ho Chi Minh",
                    tags = listOf("Fast Food", "Fried Chicken"),
                    rating = 4.5f,
                    deliveryFee = 1.5,
                    deliveryTime = "15 min",
                    imageRes = R.drawable.food_bowl,
                    promoTags = listOf("Giảm 50%")
                ),
                Restaurant(
                    id = "3",
                    name = "Pizza Hut Deli",
                    tags = listOf("Pizza", "Italian", "Pasta"),
                    rating = 4.8f,
                    deliveryFee = 0.0,
                    deliveryTime = "30 min",
                    imageRes = R.drawable.food_bowl
                )
            )

            _state.update { it.copy(
                banners = mockBanners,
                categories = mockCategories,
                restaurants = mockRestaurants,
                isLoading = false
            ) }
        }
    }
}
