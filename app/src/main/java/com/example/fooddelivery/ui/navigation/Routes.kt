package com.example.fooddelivery.ui.navigation

import kotlinx.serialization.Serializable

// graph routes
@Serializable object AuthGraph
@Serializable object CustomerGraph
@Serializable object RestaurantGraph

// auth
@Serializable object OnboardingRoute
@Serializable object LoginRoute
@Serializable object RegisterRoute
@Serializable object RegistrationSuccessRoute
@Serializable object ForgotPasswordRoute
@Serializable
data class VerificationRoute(
    val email: String
)
@Serializable
data class ResetPasswordRoute (
    val email: String
)
// customer
@Serializable object HomeRoute
@Serializable object SearchRoute
@Serializable object LocationRoute
@Serializable object CartRoute

@Serializable data class FoodDetailRoute(val foodId: Int)

@Serializable object PaymentRoute
@Serializable object AddCardRoute
@Serializable object CheckoutSuccessRoute

@Serializable object MyOrdersRoute
@Serializable data class TrackOrderRoute(val orderId: String)

@Serializable object ProfileRoute
@Serializable object EditProfileRoute
@Serializable object MyAddressRoute
@Serializable object AddAddressRoute
@Serializable data class ChatRoute(val receiverId: String)

// Restaurant
@Serializable object RestaurantDashboardRoute
@Serializable object RestaurantFoodListRoute
@Serializable data class RestaurantAddFoodRoute(val foodId: String? = null)
@Serializable object RestaurantWalletRoute
@Serializable object RestaurantWithdrawRoute
@Serializable object RestaurantReviewsRoute
@Serializable object RestaurantNotificationsRoute
@Serializable object RestaurantMessagesRoute
@Serializable object RestaurantProfileRoute
@Serializable object RestaurantCouponRoute
