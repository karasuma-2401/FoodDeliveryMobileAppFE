package com.example.fooddelivery.ui.screens.restaurant.profile

data class RestaurantPersonalInfoState(
    val name: String = "",
    val phone: String = "",
    val description: String = "",
    val imageUrl: String? = null,
    val addressId: Int? = null,
    val selectedAddressText: String = "",
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
    val isFromSignUp: Boolean = false
)

sealed interface RestaurantPersonalInfoEvent {
    data class NameChanged(val name: String) : RestaurantPersonalInfoEvent
    data class PhoneChanged(val phone: String) : RestaurantPersonalInfoEvent
    data class DescriptionChanged(val description: String) : RestaurantPersonalInfoEvent
    data class AddressSelected(val id: Int, val detail: String) : RestaurantPersonalInfoEvent
    object Submit : RestaurantPersonalInfoEvent
}