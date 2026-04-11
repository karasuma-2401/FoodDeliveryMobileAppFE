package com.example.fooddelivery.ui.navigation

import android.app.Activity
import android.app.Application
import androidx.activity.compose.BackHandler
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.toRoute
import com.example.fooddelivery.ui.screens.auth.forgot_password.ForgotPasswordScreen
import com.example.fooddelivery.ui.screens.auth.login.LoginScreen
import com.example.fooddelivery.ui.screens.auth.register.RegisterScreen
import com.example.fooddelivery.ui.screens.auth.register.RegistrationSuccessScreen
import com.example.fooddelivery.ui.screens.auth.reset_password.ResetPasswordScreen
import com.example.fooddelivery.ui.screens.auth.verification.VerificationScreen
import com.example.fooddelivery.ui.screens.onboarding.OnboardingScreen

@Composable
fun RootNavigationGraph(
    navController: NavHostController,
    startDestination: Any
) {
    NavHost(
        navController = navController,
        startDestination = AuthGraph
    ) {
        authNavGraph(navController = navController, startDestination = startDestination)
        userNavGraph(navController = navController)
        vendorNavGraph(navController = navController)
    }
}

// auth graph
fun NavGraphBuilder.authNavGraph(
    navController: NavHostController,
    startDestination: Any
) {
    navigation<AuthGraph>(startDestination = startDestination) {

        composable<OnboardingRoute> {
            OnboardingScreen(onFinishOnboarding = {
                navController.navigate(LoginRoute) {
                    popUpTo<OnboardingRoute> { inclusive = true }
                }
            })
        }

        composable<LoginRoute> {
            val context = LocalContext.current as? Activity
            BackHandler{
                context?.finish()
            }
            LoginScreen(
                onNavigateBack = {
                    context?.finish()
                },
                onNavigateToSignUp = { navController.navigate(RegisterRoute) },
                onNavigateToForgotPassword = { navController.navigate(ForgotPasswordRoute) },
                onNavigateHome = { navController.navigate(HomeRoute) {
                    popUpTo<LoginRoute> { inclusive = true}
                } }
            )
        }

        composable<RegisterRoute> {
            RegisterScreen(
                onNavigateBack = { navController.popBackStack()},
                onNavigateToLogin = { navController.popBackStack() },
                onNavigateToRegistrationSuccess = {
                    navController.navigate(RegistrationSuccessRoute) {
                        popUpTo<RegisterRoute> { inclusive = true }
                    }
                }
            )
        }
        composable<RegistrationSuccessRoute> {
            RegistrationSuccessScreen(
                onStartOrdering = {
                    navController.navigate(HomeRoute) {
                        popUpTo<RegistrationSuccessRoute> { inclusive = true }
                    }
                },
                onViewProfile = {
                    navController.navigate(ProfileRoute) {
                        popUpTo<RegistrationSuccessRoute> { inclusive = true }
                    }
                },
                onClose = {
                    navController.navigate(HomeRoute) {
                        popUpTo<RegistrationSuccessRoute> { inclusive = true }
                    }
                }
            )
        }

        composable<ForgotPasswordRoute> {
            ForgotPasswordScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToVerify = { emailInput ->
                    navController.navigate(VerificationRoute(email = emailInput))
                }
            )
        }

        composable<VerificationRoute> { backStackEntry ->
            val userEmail = backStackEntry.toRoute<VerificationRoute>().email
            VerificationScreen(
                email = userEmail,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToResetPassword = {
                    navController.navigate(ResetPasswordRoute(email = userEmail))
                }
            )
        }

        composable<ResetPasswordRoute> { backStackEntry ->
            val userEmail = backStackEntry.toRoute<ResetPasswordRoute>().email

            ResetPasswordScreen(
                email = userEmail,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToLogin = {
                    navController.navigate(LoginRoute) {
                        popUpTo<LoginRoute> { inclusive = false }
                    }
                }
            )
        }
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