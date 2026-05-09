package com.example.fooddelivery.ui.navigation

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
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
import com.example.fooddelivery.ui.screens.restaurant.coupon.RestaurantCouponScreen
import com.example.fooddelivery.ui.screens.restaurant.dashboard.DashboardScreen
import com.example.fooddelivery.ui.screens.restaurant.food_management.AddFoodScreen
import com.example.fooddelivery.ui.screens.restaurant.food_management.EditFoodScreen
import com.example.fooddelivery.ui.screens.restaurant.food_management.MyFoodListScreen

@Composable
fun RootNavigationGraph(
    navController: NavHostController,
    startDestination: Any
) {
    // Determine the root start destination graph based on the provided start route
    val rootStartDestination = when (startDestination) {
        is HomeRoute, is CustomerGraph -> CustomerGraph
        is RestaurantDashboardRoute, is RestaurantGraph -> RestaurantGraph
        else -> AuthGraph
    }

    NavHost(
        navController = navController,
        startDestination = rootStartDestination
    ) {
        authNavGraph(
            navController = navController,
            startDestination = if (rootStartDestination == AuthGraph) startDestination else OnboardingRoute
        )
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
            BackHandler {
                context?.finish()
            }
            LoginScreen(
                onNavigateBack = {
                    context?.finish()
                },
                onNavigateToSignUp = { navController.navigate(RegisterRoute) },
                onNavigateToForgotPassword = { navController.navigate(ForgotPasswordRoute) },
                onNavigateHome = { isVendor ->
                    // Điều hướng dựa trên role
                    val destination = if (isVendor) RestaurantGraph else CustomerGraph
                    navController.navigate(destination) {
                        popUpTo<AuthGraph> { inclusive = true }
                    }
                }
            )
        }

        composable<RegisterRoute> {
            RegisterScreen(
                onNavigateBack = { navController.popBackStack() },
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
                    navController.navigate(CustomerGraph) {
                        popUpTo<AuthGraph> { inclusive = true }
                    }
                },
                onViewProfile = {
                    navController.navigate(ProfileRoute) {
                        popUpTo<AuthGraph> { inclusive = true }
                    }
                },
                onClose = {
                    navController.navigate(CustomerGraph) {
                        popUpTo<AuthGraph> { inclusive = true }
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
            Column {
                Text("customer home")
                // Temporary button to switch to Vendor Dashboard for testing
                Button(onClick = {
                    navController.navigate(RestaurantGraph) {
                        // Clear customer graph if we are switching roles entirely
                        popUpTo<CustomerGraph> { inclusive = true }
                    }
                }) {
                    Text("Go to Vendor Dashboard")
                }
            }
        }
        composable<FoodDetailRoute> { backStackEntry ->
            val args = backStackEntry.toRoute<FoodDetailRoute>()
            Text("Details food with id: ${args.foodId}")
        }

        composable<CartRoute> { Text("Cart") }
        composable<PaymentRoute> { Text("Payment") }
        composable<ProfileRoute> { Text("Customer profile") }
        composable<EditProfileRoute> { Text("Edit profile") }

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
            Text("Chat with ID: ${args.receiverId}")
        }
    }
}

// vendor graph
fun NavGraphBuilder.vendorNavGraph(navController: NavHostController) {
    val onVendorNavigate: (String) -> Unit = { route ->
        when (route) {
            "dashboard" -> navController.navigate(RestaurantDashboardRoute)
            "menu" -> navController.navigate(RestaurantFoodListRoute)
            "notifications" -> navController.navigate(RestaurantNotificationsRoute)
            "profile" -> navController.navigate(RestaurantProfileRoute)
            "coupons" -> navController.navigate(RestaurantCouponRoute)
        }
    }
    val onAddFood: () -> Unit = {
        navController.navigate(RestaurantAddFoodRoute())
    }

    navigation<RestaurantGraph>(startDestination = RestaurantDashboardRoute) {

        composable<RestaurantDashboardRoute> {
            DashboardScreen(
                onSeeAllClick = { navController.navigate(RestaurantFoodListRoute) },
                onSeeAllReviewsClick = { navController.navigate(RestaurantReviewsRoute) },
                onAddFoodClick = onAddFood,
                onNavigate = onVendorNavigate
            )
        }

        composable<RestaurantFoodListRoute> {
            MyFoodListScreen(
                onNavigateBack = { navController.popBackStack() },
                onEditFood = { foodId ->
                    navController.navigate(RestaurantAddFoodRoute(foodId = foodId))
                },
                onAddFoodClick = onAddFood,
                onNavigate = onVendorNavigate
            )
        }

        composable<RestaurantAddFoodRoute> { backStackEntry ->
            val args = backStackEntry.toRoute<RestaurantAddFoodRoute>()
            if (args.foodId == null) {
                AddFoodScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            } else {
                EditFoodScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }

        composable<RestaurantWalletRoute> { Text("Wallet") }
        composable<RestaurantWithdrawRoute> { Text("Withdraw") }
        composable<RestaurantReviewsRoute> { Text("Reviews") }
        composable<RestaurantNotificationsRoute> { Text("Notifications") }
        composable<RestaurantMessagesRoute> { Text("Messages") }
        composable<RestaurantProfileRoute> { Text("Profile") }
        
        composable<RestaurantCouponRoute> {
            RestaurantCouponScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
