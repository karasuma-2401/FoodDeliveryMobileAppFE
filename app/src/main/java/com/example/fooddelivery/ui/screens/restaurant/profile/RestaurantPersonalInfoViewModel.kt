package com.example.fooddelivery.ui.screens.restaurant.profile

data class RestaurantPersonalInfoState(
    val name: String = "",
    val phone: String = "",
    val street: String = "",
    val district: String = "",
    val city: String = "",
    val imageUrl: String? = null,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)

sealed interface RestaurantPersonalInfoEvent {
    data class NameChanged(val name: String) : RestaurantPersonalInfoEvent
    data class PhoneChanged(val phone: String) : RestaurantPersonalInfoEvent
    data class StreetChanged(val street: String) : RestaurantPersonalInfoEvent
    data class DistrictChanged(val district: String) : RestaurantPersonalInfoEvent
    data class CityChanged(val city: String) : RestaurantPersonalInfoEvent
    object Submit : RestaurantPersonalInfoEvent
}