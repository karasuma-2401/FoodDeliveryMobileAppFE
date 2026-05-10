package com.example.fooddelivery.ui.screens.admin
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

data class AdminRestaurantState(
    val restaurants: List<RestaurantItem> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false
)

data class RestaurantItem(
    val id: Int,
    val name: String,
    val phone: String,
    val isApproved: Boolean
)

class AdminRestaurantViewModel : ViewModel() {
    private val _state = mutableStateOf(AdminRestaurantState())
    val state: State<AdminRestaurantState> = _state

    init {
        loadRestaurants()
    }

    private fun loadRestaurants() {
        _state.value = _state.value.copy(
            restaurants = List(10) { index ->
                RestaurantItem(
                    id = index, // Gán id ở đây
                    name = "Restaurant $index",
                    phone = "090123456$index",
                    isApproved = index % 2 == 0
                )
            }
        )
    }

    fun onSearchQueryChange(query: String) {
        _state.value = _state.value.copy(searchQuery = query)
        // Thực hiện lọc danh sách tại đây
    }

    fun deleteRestaurant(name: Int) {
        // Logic xóa
    }
}