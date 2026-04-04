package com.example.fooddelivery.ui.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.toRoute

@Composable
fun RootNavigationGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = AuthGraph
    ) {
        authNavGraph(navController = navController)
        userNavGraph(navController = navController)
        vendorNavGraph(navController = navController)
    }
}

// auth graph
fun NavGraphBuilder.authNavGraph(navController: NavHostController) {
    navigation<AuthGraph>(startDestination = OnboardingRoute) {

        composable<OnboardingRoute> {
            Text("Onboarding Screen")
            //navController.navigate(LoginRoute)
        }

        composable<LoginRoute> {
            // trans to user graph and delete auth graph
            Text("Login Screen")
            /* Button(onClick = {
                navController.navigate(UserGraph) {
                    popUpTo<AuthGraph> { inclusive = true }
                }
            }) { Text("Login") }
            */
        }

        composable<RegisterRoute> { Text("Register screen") }
        composable<ForgotPasswordRoute> { Text("Forgot passowrd") }
        composable<VerificationRoute> { Text("Verification OTP") }
    }
}

// customer graph
fun NavGraphBuilder.userNavGraph(navController: NavHostController) {
    navigation<CustomerGraph>(startDestination = HomeRoute) {

        composable<HomeRoute> {
            Text("customer home")
            // navController.navigate(FoodDetailRoute(foodId = 1))
        }
        composable<FoodDetailRoute> { backStackEntry ->
            val args = backStackEntry.toRoute<FoodDetailRoute>()

            Text("Details food with id: ${args.foodId}")
            //FoodDetailScreen(foodId = args.foodId)
        }

        composable<CartRoute> { Text("Cart") }
        composable<PaymentRoute> { Text("Payment") }
        composable<ProfileRoute> { Text("Customer profile") }
        composable<EditProfileRoute> { Text("Edit profile")}

        composable<TrackOrderRoute> { backStackEntry ->
            val args = backStackEntry.toRoute<TrackOrderRoute>()
            Text("Following order with ID: ${args.orderId}")
        }
        composable<SearchRoute> { Text("Search") }
        composable<LocationRoute> { Text("Location") }
        composable<AddCardRoute> { Text("Add card") }
        composable<CheckoutSuccessRoute> { Text("Checkout success") }
        composable<MyOrdersRoute> { Text("My order") }
        composable<MyAddressRoute> { Text("My address") }
        composable<AddAddressRoute> { Text("Add address") }
        composable<ChatRoute> { backStackEntry ->
            val args = backStackEntry.toRoute<ChatRoute>()

            Text ("Chat with ID: ${args.receiverId}")
            //ChatScreen(receiverId = args.receiverId)
        }
    }
}

// vendor graph
fun NavGraphBuilder.vendorNavGraph(navController: NavHostController) {
    navigation< RestaurantGraph>(startDestination = RestaurantDashboardRoute) {

        composable<RestaurantDashboardRoute> { Text("Dashboard management") }
        composable<RestaurantFoodListRoute> { Text("Food List") }

        composable<RestaurantAddFoodRoute> { backStackEntry ->
            val args = backStackEntry.toRoute<RestaurantAddFoodRoute>()
            if (args.foodId == -1) {
                Text("Add new food")
            } else {
                Text("Edit food with ID: ${args.foodId}")
            }
        }
        composable<RestaurantWalletRoute> { Text("Wallet") }
        composable<RestaurantWithdrawRoute> { Text("Withdraw") }
        composable<RestaurantReviewsRoute> { Text("Reviews") }
        composable<RestaurantNotificationsRoute> { Text("Notifications") }
        composable<RestaurantMessagesRoute> { Text("Messages")  }
        composable<RestaurantProfileRoute> { Text("Profile") }
    }
}