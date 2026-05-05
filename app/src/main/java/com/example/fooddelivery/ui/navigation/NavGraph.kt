package com.example.fooddelivery.ui.navigation

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.toRoute
import androidx.compose.runtime.remember
import com.example.fooddelivery.ui.screens.auth.forgot_password.ForgotPasswordScreen
import com.example.fooddelivery.ui.screens.auth.login.LoginScreen
import com.example.fooddelivery.ui.screens.auth.register.RegisterScreen
import com.example.fooddelivery.ui.screens.auth.register.RegistrationSuccessScreen
import com.example.fooddelivery.ui.screens.auth.reset_password.ResetPasswordScreen
import com.example.fooddelivery.ui.screens.auth.verification.VerificationScreen
import com.example.fooddelivery.ui.screens.food.FoodDetailScreen
import com.example.fooddelivery.ui.screens.cart.CartScreen
import com.example.fooddelivery.ui.screens.checkout.CheckoutScreen
import com.example.fooddelivery.ui.screens.checkout.CheckoutViewModel
import com.example.fooddelivery.ui.screens.checkout.CheckoutEvent
import com.example.fooddelivery.ui.screens.checkout.PaymentScreen
import com.example.fooddelivery.ui.screens.home.HomeScreen
import com.example.fooddelivery.ui.screens.home.search.SearchScreen
import com.example.fooddelivery.ui.screens.onboarding.OnboardingScreen
import com.example.fooddelivery.ui.screens.profile.EditProfileScreen
import com.example.fooddelivery.ui.screens.profile.ProfileScreen
import com.example.fooddelivery.ui.screens.profile.address.AddAddressScreen
import com.example.fooddelivery.ui.screens.profile.address.CustomerAddressScreen
import com.example.fooddelivery.ui.screens.home.restaurant_detail.RestaurantDetailScreen

@Composable
fun RootNavigationGraph(
    navController: NavHostController,
    startDestination: Any
) {
    NavHost(
        navController = navController,
        startDestination = CustomerGraph
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
                onNavigateToResetPassword = { email, resetCode ->
                    navController.navigate(ResetPasswordRoute(email = email, resetCode = resetCode))
                }
            )
        }
        composable<ResetPasswordRoute> { backStackEntry ->
            val userEmail = backStackEntry.toRoute<ResetPasswordRoute>().email
            val resetCode = backStackEntry.toRoute<ResetPasswordRoute>().resetCode
            ResetPasswordScreen(
                email = userEmail,
                resetCode = resetCode,
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
            HomeScreen(
                onNavigateToCategory = { id ->
                    navController.navigate(CategoryFilterRoute(categoryId = id))
                },
                onNavigateToCart = { navController.navigate(CartRoute) },
                onNavigateToSearch = { navController.navigate(SearchRoute) },
                onNavigateToProfile = { navController.navigate(ProfileRoute) },
                onNavigateToOrders = { navController.navigate(MyOrdersRoute) },
                onNavigateToRestaurant = { id ->
                    navController.navigate(RestaurantDetailRoute(restaurantId = id))
                },
                onNavigateToAllRestaurants = { /* later */ },
                onNavigateToAllCategories = { },
                onOpenMenu = {},
                onOpenLocationPicker = {},
            )
        }
        composable<CategoryFilterRoute> { backStackEntry ->
            val args = backStackEntry.toRoute<CategoryFilterRoute>()
            Text("Category with id: ${args.categoryId}")
        }
        composable<RestaurantDetailRoute> { backStackEntry ->
            val args = backStackEntry.toRoute<RestaurantDetailRoute>()
            RestaurantDetailScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToFoodDetail = { foodId ->
                    navController.navigate(FoodDetailRoute(foodId = foodId))
                }
            )
        }
        composable<FoodDetailRoute> { backStackEntry ->
            FoodDetailScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToRestaurant = { restaurantId ->
                    navController.navigate(RestaurantDetailRoute(restaurantId))
                },
                onShowSnackbar = { message ->
                    println(message)
                }
            )
        }

        composable<CartRoute> {
            CartScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToCheckout = { navController.navigate(CheckoutRoute) }
            )
        }

        composable<CheckoutRoute> { backStackEntry ->
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(CustomerGraph)
            }
            val viewModel: CheckoutViewModel = hiltViewModel(parentEntry)
            CheckoutScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToTrackOrder = { orderId ->
                    navController.navigate(TrackOrderRoute(orderId = orderId)) {
                        popUpTo<CartRoute> { inclusive = true }
                    }
                },
                onNavigateToAddAddress = { navController.navigate(AddAddressRoute) },
                onNavigateToPaymentMethod = { navController.navigate(PaymentRoute) }
            )
        }

        composable<PaymentRoute> { backStackEntry ->
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(CustomerGraph)
            }
            val viewModel: CheckoutViewModel = hiltViewModel(parentEntry)
            val state by viewModel.state.collectAsState()
            
            PaymentScreen(
                onNavigateBack = { navController.popBackStack() },
                onConfirmPayment = { method ->
                    viewModel.onEvent(CheckoutEvent.PaymentMethodSelected(method))
                    navController.popBackStack()
                },
                currentMethod = state.paymentMethod,
                totalAmount = state.total
            )
        }

        composable<ProfileRoute> {
            ProfileScreen(
                onNavigateBack = { navController.popBackStack() },
                onEditProfile = { navController.navigate(EditProfileRoute) },
                onLogout = {
                    navController.navigate(LoginRoute) {
                        popUpTo<ProfileRoute> { inclusive = true }
                    }
                }
            )
        }
        composable<EditProfileRoute> {
            EditProfileScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<TrackOrderRoute> { backStackEntry ->
            val args = backStackEntry.toRoute<TrackOrderRoute>()
            Text("Following order with ID: ${args.orderId}")
        }
        composable<SearchRoute> {
            SearchScreen(
                onNavigateToHome = { navController.popBackStack() },
                onNavigateToOrders = { navController.navigate(MyOrdersRoute) },
                onNavigateToProfile = { navController.navigate(ProfileRoute) },
                onNavigateToCart = { navController.navigate(CartRoute) },
                onNavigateToRestaurant = { restaurant ->
                    navController.navigate(RestaurantDetailRoute(restaurantId = restaurant.id))
                }
            )
        }
        composable<LocationRoute> { Text("Location") }
        composable<AddCardRoute> { Text("Add card") }
        composable<CheckoutSuccessRoute> { Text("Checkout success") }
        composable<MyOrdersRoute> { Text("My order") }
        
        composable<MyAddressRoute> {
            CustomerAddressScreen(
                onNavigateBack = { navController.popBackStack() },
                onAddNewAddress = { navController.navigate(AddAddressRoute) },
                onEditAddress = { addressId ->
                    // Bạn có thể thêm route EditAddressRoute sau
                }
            )
        }
        
        composable<AddAddressRoute> {
            AddAddressScreen(
                onNavigateBack = { navController.popBackStack() },
                onAddressSaved = {
                    navController.popBackStack()
                }
            )
        }
        
        composable<ChatRoute> { backStackEntry ->
            val args = backStackEntry.toRoute<ChatRoute>()
            Text ("Chat with ID: ${args.receiverId}")
        }
    }
}

// vendor graph
fun NavGraphBuilder.vendorNavGraph(navController: NavHostController) {
    navigation<RestaurantGraph>(startDestination = RestaurantDashboardRoute) {
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
