package com.example.fooddelivery.ui.screens.customer.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fooddelivery.R
import com.example.fooddelivery.domain.model.Category
import com.example.fooddelivery.domain.model.Restaurant
import com.example.fooddelivery.domain.model.User
import com.example.fooddelivery.domain.location.LocationTracker
import com.example.fooddelivery.domain.repository.CartRepository
import com.example.fooddelivery.domain.repository.ChatRepository
import com.example.fooddelivery.domain.repository.DeliveryLocationRepository
import com.example.fooddelivery.domain.usecase.EnrichRestaurantsWithVoucherBadgesUseCase
import com.example.fooddelivery.domain.usecase.GetHomeDashboardUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.map
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
    val unreadMessageCount: Int = 0,
    val selectedLocation: String = "Home",
    val availableLocations: List<String> = listOf("Home", "Work", "Other"),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isPhoneMissing: Boolean = false,
    val errorMessage: String? = null
)

sealed interface HomeEvent {
    object LoadHomeData : HomeEvent
    object Refresh : HomeEvent
    object CartClicked : HomeEvent
    object MessageClicked : HomeEvent
    data class LocationSelected(val location: String) : HomeEvent
    data class CategoryClicked(val categoryId: String) : HomeEvent
    data class RestaurantClicked(val restaurantId: String) : HomeEvent
    data class BannerClicked(val banner: HomeBanner) : HomeEvent
    object SeeAllCategoriesClicked : HomeEvent
    object SeeAllRestaurantsClicked : HomeEvent
    object PhoneUpdateDismissed : HomeEvent
    object ErrorDismissed : HomeEvent
}

sealed interface HomeUiEffect {
    object NavigateToCart : HomeUiEffect
    object NavigateToConversations : HomeUiEffect
    object NavigateToAllCategories : HomeUiEffect
    object NavigateToAllRestaurants : HomeUiEffect
    data class NavigateToCategory(val categoryId: String) : HomeUiEffect
    data class NavigateToRestaurant(val restaurantId: String) : HomeUiEffect
    data class NavigateToFoodDetail(val foodId: String) : HomeUiEffect
    object NavigateToEditProfile : HomeUiEffect
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getHomeDashboardUseCase: GetHomeDashboardUseCase,
    private val enrichRestaurantsWithVoucherBadgesUseCase: EnrichRestaurantsWithVoucherBadgesUseCase,
    private val cartRepository: CartRepository,
    private val chatRepository: ChatRepository,
    private val deliveryLocationRepository: DeliveryLocationRepository,
    private val locationTracker: LocationTracker,
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<HomeUiEffect>()
    val effect: SharedFlow<HomeUiEffect> = _effect.asSharedFlow()

    init {
        observeCart()
        observeUnreadMessages()
        observeDeliveryLocation()
    }

    private fun observeDeliveryLocation() {
        viewModelScope.launch {
            deliveryLocationRepository.deliveryLocation.collectLatest { location ->
                _state.update {
                    it.copy(
                        selectedLocation = location.selectedLabel,
                        availableLocations = location.availableLabels,
                    )
                }
            }
        }
        viewModelScope.launch {
            deliveryLocationRepository.refreshAddresses()
            loadData()
        }
        viewModelScope.launch {
            deliveryLocationRepository.deliveryLocation
                .map { it.selectedAddressId }
                .distinctUntilChanged()
                .drop(1)
                .collectLatest { loadData() }
        }
    }

    private fun observeCart() {
        viewModelScope.launch {
            cartRepository.cartItems.collectLatest { items ->
                val totalCount = items.sumOf { it.quantity }
                _state.update { it.copy(cartItemCount = totalCount) }
            }
        }
    }

    private fun observeUnreadMessages() {
        viewModelScope.launch {
            chatRepository.getConversations().collectLatest { conversations ->
                val totalUnread = conversations.sumOf { it.unreadCount }
                _state.update { it.copy(unreadMessageCount = totalUnread) }
            }
        }
    }

    fun onEvent(event: HomeEvent) {
        viewModelScope.launch {
            when (event) {
                HomeEvent.LoadHomeData -> loadData()
                HomeEvent.Refresh -> loadData(isRefresh = true)
                HomeEvent.CartClicked -> _effect.emit(HomeUiEffect.NavigateToCart)
                HomeEvent.MessageClicked -> _effect.emit(HomeUiEffect.NavigateToConversations)
                is HomeEvent.LocationSelected -> {
                    deliveryLocationRepository.selectByLabel(event.location)
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
                HomeEvent.PhoneUpdateDismissed -> _state.update { it.copy(isPhoneMissing = false) }
                HomeEvent.ErrorDismissed -> _state.update { it.copy(errorMessage = null) }
            }
        }
    }

    private fun loadData(isRefresh: Boolean = false) {
        viewModelScope.launch {
            if (isRefresh) {
                _state.update { it.copy(isRefreshing = true, errorMessage = null) }
            } else {
                _state.update { it.copy(isLoading = true, errorMessage = null) }
            }

            val location = deliveryLocationRepository.deliveryLocation.value
            val (lat, lng) = resolveCoordinates(location.lat, location.lng)
            getHomeDashboardUseCase(lat = lat, lng = lng).onSuccess { data ->
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

                _state.update {
                    it.copy(
                        user = data.user ?: User(),
                        categories = data.categories,
                        restaurants = data.restaurants,
                        banners = mockBanners,
                        cartItemCount = data.cartItemCount,
                        unreadMessageCount = data.unreadMessageCount,
                        isPhoneMissing = data.user?.phone?.isBlank() ?: false,
                        isLoading = false,
                        isRefreshing = false
                    )
                }
                enrichVoucherBadges(data.restaurants)
            }.onFailure { e ->
                _state.update { it.copy(isLoading = false, isRefreshing = false, errorMessage = e.message) }
            }
        }
    }

    private suspend fun resolveCoordinates(
        addressLat: Double?,
        addressLng: Double?,
    ): Pair<Double?, Double?> {
        if (addressLat != null && addressLng != null) return addressLat to addressLng
        val gps = locationTracker.getCurrentLocation()
        return (addressLat ?: gps?.latitude) to (addressLng ?: gps?.longitude)
    }

    private fun enrichVoucherBadges(restaurants: List<Restaurant>) {
        if (restaurants.isEmpty()) return
        viewModelScope.launch {
            val enriched = enrichRestaurantsWithVoucherBadgesUseCase(restaurants)
            _state.update { current ->
                if (current.restaurants.map { it.id } != restaurants.map { it.id }) {
                    current
                } else {
                    current.copy(restaurants = enriched)
                }
            }
        }
    }
}
