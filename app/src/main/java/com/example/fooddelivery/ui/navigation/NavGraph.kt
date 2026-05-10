package com.example.fooddelivery.ui.navigation

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.fooddelivery.ui.screens.checkout.CheckoutSuccessScreen
import com.example.fooddelivery.ui.screens.home.HomeScreen
import com.example.fooddelivery.ui.screens.home.search.SearchScreen
import com.example.fooddelivery.ui.screens.onboarding.OnboardingScreen
import com.example.fooddelivery.ui.screens.restaurant.coupon.RestaurantCouponScreen
import com.example.fooddelivery.ui.screens.restaurant.dashboard.DashboardScreen
import com.example.fooddelivery.ui.screens.restaurant.food_management.AddFoodScreen
import com.example.fooddelivery.ui.screens.restaurant.food_management.EditFoodScreen
import com.example.fooddelivery.ui.screens.restaurant.food_management.MyFoodListScreen
import com.example.fooddelivery.ui.screens.admin.AdminCategoryScreen
import com.example.fooddelivery.ui.screens.admin.AdminRestaurantScreen
import com.example.fooddelivery.ui.screens.profile.EditProfileScreen
import com.example.fooddelivery.ui.screens.profile.ProfileScreen
import com.example.fooddelivery.ui.screens.profile.address.AddAddressScreen
import com.example.fooddelivery.ui.screens.profile.address.CustomerAddressScreen
import com.example.fooddelivery.ui.screens.home.restaurant_detail.RestaurantDetailScreen
import com.example.fooddelivery.ui.screens.order.OrdersScreen
import com.example.fooddelivery.ui.screens.order.TrackOrderScreen
import com.example.fooddelivery.ui.screens.chat.ChatScreen
import com.example.fooddelivery.ui.screens.chat.ConversationScreen
import com.example.fooddelivery.ui.screens.rating.RatingReviewScreen

@Composable
fun RootNavigationGraph(
    navController: NavHostController,
    startDestination: Any
) {
    val initialGraph = if (startDestination is HomeRoute) CustomerGraph else AuthGraph

    NavHost(
        navController = navController,
        startDestination = initialGraph
    ) {
        authNavGraph(
            navController = navController,
            startDestination = if (startDestination is HomeRoute) LoginRoute else startDestination
        )
        userNavGraph(navController = navController)
        vendorNavGraph(navController = navController)
        adminNavGraph(navController = navController)
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
                onNavigateHome = {
                    navController.navigate(CustomerGraph) {
                        popUpTo<AuthGraph> { inclusive = true }
                    }
                }
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
            val args = backStackEntry.toRoute<ResetPasswordRoute>()
            ResetPasswordScreen(
                email = args.email,
                resetCode = args.resetCode,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToLogin = {
                    navController.navigate(LoginRoute) {
                        popUpTo<LoginRoute> { inclusive = true }
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
                onNavigateToAllRestaurants = {  },
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
                onNavigateToAddAddress = { navController.navigate(AddAddressRoute) },
                onNavigateToPaymentSuccessful = { navController.navigate(CheckoutSuccessRoute) }
            )
        }


        composable<CheckoutSuccessRoute> {
            CheckoutSuccessScreen(
                onTrackOrder = {
                    navController.navigate(TrackOrderRoute(orderId = "162432")) {
                        popUpTo<CheckoutRoute> { inclusive = true }
                    }
                }
            )
        }
        composable<MyOrdersRoute> {
            OrdersScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToTrackOrder = { orderId -> navController.navigate(TrackOrderRoute(orderId = orderId)) },
                onNavigateToRate = { orderId, restaurantName ->
                    navController.navigate(RatingReviewRoute(orderId = orderId, restaurantName = restaurantName))
                }
            )
        }
        composable<RatingReviewRoute> {
            RatingReviewScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<ProfileRoute> {
            ProfileScreen(
                onNavigateBack = { navController.popBackStack() },
                onEditProfile = { navController.navigate(EditProfileRoute) },
                onLogout = {
                    navController.navigate(AuthGraph) {
                        popUpTo<CustomerGraph> { inclusive = true }
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
            TrackOrderScreen(
                orderId = args.orderId,
                onNavigateBack = { navController.popBackStack() },
                onChatWithRestaurant = { receiverId ->
                    navController.navigate(ChatRoute(conversationId = receiverId))
                }
            )
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

        composable<ConversationRoute> {
            ConversationScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToChat = { id, name ->
                    navController.navigate(ChatRoute(conversationId = id, restaurantName = name))
                }
            )
        }

        composable<ChatRoute> {
            ChatScreen(
                onNavigateBack = { navController.popBackStack() }
            )
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
        composable<RestaurantMessagesRoute> { Text("Messages")  }
        composable<RestaurantProfileRoute> { Text("Profile") }

        composable<RestaurantCouponRoute> {
            RestaurantCouponScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

// admin graph
fun NavGraphBuilder.adminNavGraph(navController: NavHostController) {
    val onAdminNavigate: (String) -> Unit = { route ->
        when (route) {
            "dashboard" -> navController.navigate(AdminDashboardRoute) {
                launchSingleTop = true
                popUpTo<AdminDashboardRoute> { inclusive = false }
            }
            "categories" -> navController.navigate(AdminCategoriesRoute) {
                launchSingleTop = true
            }
            "coupons" -> navController.navigate(AdminCouponRoute) {
                launchSingleTop = true
            }
            "settings" -> navController.navigate(AdminSettingsRoute) {
                launchSingleTop = true
            }
        }
    }

    navigation<AdminGraph>(startDestination = AdminDashboardRoute) {
        composable<AdminDashboardRoute> {
            AdminRestaurantScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAdd = { /* TODO */ },
                onNavigateToEdit = { /* TODO */ },
                onNavigate = onAdminNavigate
            )
        }

        composable<AdminCategoriesRoute> {
            AdminCategoryScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAdd = { /* TODO: navController.navigate(CreateCategoryRoute) */ },
                onNavigateToEdit = { /* TODO */ },
                onNavigate = onAdminNavigate
            )
        }

        composable<AdminCouponRoute> {
            RestaurantCouponScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<AdminSettingsRoute> {
            Column {
                Text("Admin Settings")
                Button(onClick = { navController.popBackStack() }) {
                    Text("Back")
                }
            }
        }
    }
}