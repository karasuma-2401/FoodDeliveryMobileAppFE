package com.example.fooddelivery.ui.navigation

import androidx.compose.ui.Modifier
import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.toRoute
import com.example.fooddelivery.ui.components.bottombar.BottomNavItem
import com.example.fooddelivery.ui.components.bottombar.DFoodBottomBar
import com.example.fooddelivery.ui.screens.auth.forgot_password.ForgotPasswordScreen
import com.example.fooddelivery.ui.screens.auth.login.LoginScreen
import com.example.fooddelivery.ui.screens.auth.register.RegisterScreen
import com.example.fooddelivery.ui.screens.auth.register.RegistrationSuccessScreen
import com.example.fooddelivery.ui.screens.auth.reset_password.ResetPasswordScreen
import com.example.fooddelivery.ui.screens.auth.verification.VerificationScreen
import com.example.fooddelivery.ui.screens.food.FoodDetailScreen
import com.example.fooddelivery.ui.screens.cart.CartScreen
import com.example.fooddelivery.ui.screens.checkout.CheckoutScreen
import com.example.fooddelivery.ui.screens.checkout.CheckoutSuccessScreen
import com.example.fooddelivery.ui.screens.home.HomeScreen
import com.example.fooddelivery.ui.screens.home.search.SearchScreen
import com.example.fooddelivery.ui.screens.onboarding.OnboardingScreen
import com.example.fooddelivery.ui.screens.restaurant.coupon.RestaurantCouponScreen
import com.example.fooddelivery.ui.screens.restaurant.dashboard.DashboardScreen
import com.example.fooddelivery.ui.screens.restaurant.food_management.AddFoodScreen
import com.example.fooddelivery.ui.screens.restaurant.food_management.EditFoodScreen
import com.example.fooddelivery.ui.screens.restaurant.food_management.MyFoodListScreen
import com.example.fooddelivery.ui.screens.admin.categories.AdminCategoryScreen
import com.example.fooddelivery.ui.screens.admin.dashboard.AdminRestaurantScreen
import com.example.fooddelivery.ui.screens.profile.changePassword.ChangePasswordScreen
import com.example.fooddelivery.ui.screens.profile.EditProfileScreen
import com.example.fooddelivery.ui.screens.profile.ProfileScreen
import com.example.fooddelivery.ui.screens.profile.address.AddAddressScreen
import com.example.fooddelivery.ui.screens.profile.address.CustomerAddressScreen
import com.example.fooddelivery.ui.screens.profile.favourite.FavouriteScreen
import com.example.fooddelivery.ui.screens.payment.PaymentMethodScreen
import com.example.fooddelivery.ui.screens.profile.review.UserReviewScreen
import com.example.fooddelivery.ui.screens.home.restaurant.restaurant_details.RestaurantDetailScreen
import com.example.fooddelivery.ui.screens.order.OrdersScreen
import com.example.fooddelivery.ui.screens.order.TrackOrderScreen
import com.example.fooddelivery.ui.screens.chat.ChatScreen
import com.example.fooddelivery.ui.screens.chat.ConversationScreen
import com.example.fooddelivery.ui.screens.rating.RatingReviewScreen
import com.example.fooddelivery.ui.screens.category.CategoryFilterScreen
import com.example.fooddelivery.ui.screens.category.AllCategoriesScreen
import com.example.fooddelivery.ui.screens.home.restaurant.AllRestaurantScreen
import com.example.fooddelivery.ui.screens.home.location.LocationScreen
import com.example.fooddelivery.ui.screens.profile.resetEmail.ResetEmailScreen
import com.example.fooddelivery.ui.screens.auth.register.PolicyScreen
// Import màn hình ReviewScreen mới
import com.example.fooddelivery.ui.screens.restaurant.reviews.ReviewScreen
import com.example.fooddelivery.ui.screens.restaurant.component.DFoodBottomBar

@Composable
fun RootNavigationGraph(
    navController: NavHostController,
    startDestination: Any
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val customerBottomBarRoutes = listOf(
        HomeRoute::class,
        SearchRoute::class,
        MyOrdersRoute::class,
        ProfileRoute::class
    )

    val showCustomerBottomBar = customerBottomBarRoutes.any { currentDestination?.hasRoute(it) == true }

    Scaffold(
        bottomBar = {
            if (showCustomerBottomBar) {
                DFoodBottomBar(
                    currentRoute = when {
                        currentDestination?.hasRoute(HomeRoute::class) == true -> "home"
                        currentDestination?.hasRoute(SearchRoute::class) == true -> "search"
                        currentDestination?.hasRoute(MyOrdersRoute::class) == true -> "orders"
                        currentDestination?.hasRoute(ProfileRoute::class) == true -> "profile"
                        else -> ""
                    },
                    onItemClick = { item ->
                        val route = when (item) {
                            BottomNavItem.Home -> HomeRoute
                            BottomNavItem.Search -> SearchRoute
                            BottomNavItem.Orders -> MyOrdersRoute
                            BottomNavItem.Profile -> ProfileRoute
                        }
                        navController.navigate(route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = CustomerGraph,
            modifier = Modifier.padding(innerPadding),
            enterTransition = {
                fadeIn(animationSpec = tween(300)) +
                        scaleIn(initialScale = 0.98f, animationSpec = tween(300))
            },
            exitTransition = {
                fadeOut(animationSpec = tween(250))
            },
            popEnterTransition = {
                fadeIn(animationSpec = tween(300))
            },
            popExitTransition = {
                fadeOut(animationSpec = tween(250))
            }
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
                onNavigateToRegistrationSuccess = { email ->
                    navController.navigate(VerificationRoute(email = email, isFromRegistration = true)) {
                        popUpTo<RegisterRoute> { inclusive = true }
                    }
                },
                onNavigateToPolicy = { type ->
                    navController.navigate(PolicyRoute(type = type))
                }
            )
        }

        composable<PolicyRoute> { backStackEntry ->
            val args = backStackEntry.toRoute<PolicyRoute>()
            PolicyScreen(
                type = args.type,
                onNavigateBack = { navController.popBackStack() }
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
                    navController.navigate(VerificationRoute(email = emailInput, isFromRegistration = false))
                }
            )
        }

        composable<VerificationRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<VerificationRoute>()
            VerificationScreen(
                email = route.email,
                isFromRegistration = route.isFromRegistration,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onVerificationSuccess = { email, otp ->
                    if (route.isFromRegistration) {
                        navController.navigate(RegistrationSuccessRoute) {
                            popUpTo<VerificationRoute> { inclusive = true }
                        }
                    } else {
                        navController.navigate(ResetPasswordRoute(email = email, resetCode = otp))
                    }
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
                onNavigateToAllRestaurants = {
                    navController.navigate(AllRestaurantsRoute)
                },
                onNavigateToAllCategories = {
                    navController.navigate(AllCategoriesRoute)
                },
                onNavigateToFoodDetail = { foodId ->
                    navController.navigate(FoodDetailRoute(foodId = foodId))
                },
                onNavigateToConversations = {
                    navController.navigate(ConversationRoute) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable<LocationRoute> {
            LocationScreen(
                onPermissionGranted = {
                    navController.navigate(HomeRoute) {
                        popUpTo<LocationRoute> { inclusive = true }
                    }
                }
            )
        }

        composable<AllCategoriesRoute> {
            AllCategoriesScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToCategory = { id ->
                    navController.navigate(CategoryFilterRoute(categoryId = id))
                }
            )
        }

        composable<AllRestaurantsRoute> {
            AllRestaurantScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToRestaurantDetail = { id ->
                    navController.navigate(RestaurantDetailRoute(restaurantId = id))
                }
            )
        }

        composable<CategoryFilterRoute> {
            CategoryFilterScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToFoodDetail = { foodId ->
                    navController.navigate(FoodDetailRoute(foodId = foodId))
                }
            )
        }

        composable<RestaurantDetailRoute> { backStackEntry ->
            RestaurantDetailScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToFoodDetail = { foodId ->
                    navController.navigate(FoodDetailRoute(foodId = foodId))
                }
            )
        }

        composable<FoodDetailRoute> {
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
                onNavigateToCheckout = { restaurantId, restaurantName, discount ->
                    navController.navigate(CheckoutRoute(restaurantId = restaurantId, restaurantName = restaurantName, discount = discount))
                }
            )
        }

        composable<CheckoutRoute> {
            CheckoutScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAddAddress = { navController.navigate(AddAddressRoute()) },
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
                onManageAddress = { navController.navigate(MyAddressRoute) },
                onNavigateToCart = { navController.navigate(CartRoute) },
                onNavigateToFavourite = { navController.navigate(FavouriteRoute) },
                onNavigateToNotification = { navController.navigate(NotificationRoute) },
                onNavigateToPaymentMethod = { navController.navigate(PaymentMethodRoute) },
                onNavigateToReview = { navController.navigate(UserReviewRoute) },
                onChangePassword = { navController.navigate(ChangePasswordRoute) },
                onResetEmail = { navController.navigate(ResetEmailRoute) },
                onLogout = {
                    // delete all backstack
                    navController.navigate(AuthGraph) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        
        composable<EditProfileRoute> {
            EditProfileScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<ChangePasswordRoute> {
            ChangePasswordScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<ResetEmailRoute> {
            ResetEmailScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<FavouriteRoute> {
            FavouriteScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToRestaurant = { id ->
                    navController.navigate(RestaurantDetailRoute(restaurantId = id))
                }
            )
        }

        composable<PaymentMethodRoute> {
            PaymentMethodScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<UserReviewRoute> {
            UserReviewScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEdit = { orderId, name, image, rating, comment ->
                    navController.navigate(
                        RatingReviewRoute(
                            orderId = orderId,
                            restaurantName = name,
                            restaurantImage = image,
                            initialRating = rating,
                            initialComment = comment
                        )
                    )
                }
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
                onNavigateToConversations = { navController.navigate(ConversationRoute) },
                onNavigateToRestaurant = { restaurant ->
                    navController.navigate(RestaurantDetailRoute(restaurantId = restaurant.id))
                },
                onNavigateToFoodDetail = { foodId ->
                    navController.navigate(FoodDetailRoute(foodId = foodId))
                }
            )
        }

        composable<MyAddressRoute> {
            CustomerAddressScreen(
                onNavigateBack = { navController.popBackStack() },
                onAddNewAddress = { navController.navigate(AddAddressRoute()) },
                onEditAddress = { addressId ->
                    navController.navigate(AddAddressRoute(addressId = addressId))
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

        composable<ChatRoute> { backStackEntry ->
            val args = backStackEntry.toRoute<ChatRoute>()
            ChatScreen(
                conversationId = args.conversationId,
                restaurantName = args.restaurantName,
                restaurantImage = args.restaurantImage,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

// vendor graph
fun NavGraphBuilder.vendorNavGraph(navController: NavHostController) {

    composable<RestaurantGraph> {
        val vendorNavController = androidx.navigation.compose.rememberNavController()

        val navBackStackEntry by vendorNavController.currentBackStackEntryAsState()
        val currentRouteStr = navBackStackEntry?.destination?.route ?: ""

        val isDashboard = currentRouteStr.contains("RestaurantDashboardRoute")
        val isMenu = currentRouteStr.contains("RestaurantFoodListRoute")
        val isNotifications = currentRouteStr.contains("RestaurantNotificationsRoute")
        val isProfile = currentRouteStr.contains("RestaurantProfileRoute")

        val showBottomBar = isDashboard || isMenu || isNotifications || isProfile

        val vendorCurrentRoute = when {
            isDashboard -> "dashboard"
            isMenu -> "menu"
            isNotifications -> "notifications"
            isProfile -> "profile"
            else -> "dashboard"
        }

        val onVendorNavigate: (String) -> Unit = { route ->
            when (route) {
                "dashboard" -> vendorNavController.navigate(RestaurantDashboardRoute)
                "menu" -> vendorNavController.navigate(RestaurantFoodListRoute)
                "notifications" -> vendorNavController.navigate(RestaurantNotificationsRoute)
                "profile" -> vendorNavController.navigate(RestaurantProfileRoute)
                "coupons" -> vendorNavController.navigate(RestaurantCouponRoute)
            }
        }

        val onAddFood: () -> Unit = {
            vendorNavController.navigate(RestaurantAddFoodRoute())
        }

        androidx.compose.material3.Scaffold(
            bottomBar = {
                if (showBottomBar) {
                    DFoodBottomBar(
                        currentRoute = vendorCurrentRoute,
                        onNavigate = { tab ->
                            val targetRoute = when (tab) {
                                "dashboard" -> RestaurantDashboardRoute
                                "menu" -> RestaurantFoodListRoute
                                "notifications" -> RestaurantNotificationsRoute
                                "profile" -> RestaurantProfileRoute
                                else -> RestaurantDashboardRoute
                            }
                            vendorNavController.navigate(targetRoute) {
                                popUpTo(vendorNavController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        onAddClick = onAddFood
                    )
                }
            }
        ) { paddingValues ->

            NavHost(
                navController = vendorNavController,
                startDestination = RestaurantDashboardRoute,
                modifier = Modifier.padding(paddingValues)
            ) {
                composable<RestaurantDashboardRoute> {
                    DashboardScreen(
                        onSeeAllClick = { vendorNavController.navigate(RestaurantFoodListRoute) },
                        onSeeAllReviewsClick = { vendorNavController.navigate(RestaurantReviewsRoute) },
                        onAddFoodClick = onAddFood,
                        onNavigate = onVendorNavigate
                    )
                }

                composable<RestaurantFoodListRoute> {
                    MyFoodListScreen(
                        onNavigateBack = { vendorNavController.popBackStack() },
                        onEditFood = { foodId ->
                            vendorNavController.navigate(RestaurantAddFoodRoute(foodId = foodId))
                        },
                        onAddFoodClick = onAddFood,
                        onNavigate = onVendorNavigate
                    )
                }

                composable<RestaurantAddFoodRoute> { backStackEntry ->
                    val args = backStackEntry.toRoute<RestaurantAddFoodRoute>()
                    if (args.foodId == null) {
                        AddFoodScreen(onNavigateBack = { vendorNavController.popBackStack() })
                    } else {
                        EditFoodScreen(onNavigateBack = { vendorNavController.popBackStack() })
                    }
                }

                composable<RestaurantWalletRoute> { Text("Wallet") }
                composable<RestaurantWithdrawRoute> { Text("Withdraw") }

                composable<RestaurantReviewsRoute> {
                    ReviewScreen(onNavigateBack = { vendorNavController.popBackStack() })
                }

                composable<RestaurantNotificationsRoute> { Text("Notifications") }
                composable<RestaurantMessagesRoute> { Text("Messages")  }
                composable<RestaurantProfileRoute> { Text("Profile") }

                composable<RestaurantCouponRoute> {
                    RestaurantCouponScreen(onNavigateBack = { vendorNavController.popBackStack() })
                }
            }
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
