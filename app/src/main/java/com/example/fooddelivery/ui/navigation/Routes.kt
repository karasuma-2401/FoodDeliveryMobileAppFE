package com.example.fooddelivery.ui.navigation

import kotlinx.serialization.Serializable

// graph routes
@Serializable object AuthGraph
@Serializable object CustomerGraph
@Serializable object RestaurantGraph
@Serializable object AdminGraph

// auth
@Serializable object OnboardingRoute
@Serializable object LoginRoute
@Serializable object RegisterRoute
@Serializable object RegistrationSuccessRoute
@Serializable object ForgotPasswordRoute
@Serializable
data class VerificationRoute(
    val email: String = "",
    val isFromRegistration: Boolean = false
)
@Serializable
data class ResetPasswordRoute (
    val email: String = "",
    val otp: String = ""
)

@Serializable
data class PolicyRoute(val type: String)

// customer
@Serializable object HomeRoute
@Serializable object SearchRoute
@Serializable object LocationRoute
@Serializable object CartRoute
@Serializable
data class CheckoutRoute(
    val restaurantId: String,
    val restaurantName: String,
    val discount: Double = 0.0
)
@Serializable object AllCategoriesRoute
@Serializable object AllRestaurantsRoute

@Serializable data class FoodDetailRoute(val foodId: String = "")
@Serializable data class CategoryFilterRoute(val categoryId: String = "")
@Serializable data class RestaurantDetailRoute(val restaurantId: String = "")
@Serializable object PaymentSuccessfulRoute
@Serializable object CheckoutSuccessRoute

@Serializable object MyOrdersRoute
@Serializable data class TrackOrderRoute(val orderId: String = "")
@Serializable 
data class RatingReviewRoute(
    val orderId: String, 
    val restaurantName: String = "", 
    val restaurantImage: String = "",
    val initialRating: Int = 0,
    val initialComment: String = ""
)

@Serializable object ProfileRoute
@Serializable object EditProfileRoute
@Serializable object ChangePasswordRoute
@Serializable object ResetEmailRoute
@Serializable object MyAddressRoute
@Serializable data class AddAddressRoute(val addressId: String? = null)
@Serializable object FavouriteRoute
@Serializable object NotificationRoute
@Serializable object PaymentMethodRoute
@Serializable object UserReviewRoute
@Serializable object ConversationRoute
@Serializable data class ChatRoute(
    val conversationId: String,
    val restaurantName: String = "",
    val restaurantImage: String = ""
)

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

// Admin
@Serializable object AdminDashboardRoute
@Serializable object AdminCategoriesRoute
@Serializable object AdminCouponRoute
@Serializable object AdminSettingsRoute
