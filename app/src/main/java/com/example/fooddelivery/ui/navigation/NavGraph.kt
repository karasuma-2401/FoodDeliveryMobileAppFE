package com.example.fooddelivery.ui.navigation

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import com.example.fooddelivery.ui.screens.auth.forgot_password.ForgotPasswordScreen
import com.example.fooddelivery.ui.screens.auth.login.LoginScreen
import com.example.fooddelivery.ui.screens.auth.register.RegisterScreen
import com.example.fooddelivery.ui.screens.auth.register.RegistrationSuccessScreen
import com.example.fooddelivery.ui.screens.auth.reset_password.ResetPasswordScreen
import com.example.fooddelivery.ui.screens.auth.verification.VerificationScreen
import com.example.fooddelivery.ui.screens.food.FoodDetailScreen
import com.example.fooddelivery.ui.screens.customer.cart.CartScreen
import com.example.fooddelivery.ui.screens.customer.checkout.CheckoutScreen
import com.example.fooddelivery.ui.screens.customer.checkout.CheckoutSuccessScreen
import com.example.fooddelivery.ui.screens.customer.home.HomeScreen
import com.example.fooddelivery.ui.screens.customer.search.SearchScreen
import com.example.fooddelivery.ui.screens.onboarding.OnboardingScreen
import com.example.fooddelivery.ui.screens.restaurant.coupon.RestaurantCouponScreen
import com.example.fooddelivery.ui.screens.restaurant.dashboard.DashboardScreen
import com.example.fooddelivery.ui.screens.restaurant.food_management.AddFoodScreen
import com.example.fooddelivery.ui.screens.restaurant.food_management.EditFoodScreen
import com.example.fooddelivery.ui.screens.restaurant.food_management.MyFoodListScreen
import com.example.fooddelivery.ui.screens.admin.categories.AdminCategoryScreen
import com.example.fooddelivery.ui.screens.admin.coupons.CreateCouponScreen
import com.example.fooddelivery.ui.screens.admin.coupons.AdminCouponScreen
import com.example.fooddelivery.ui.screens.admin.restaurantmanagement.AdminRestaurantScreen
import com.example.fooddelivery.ui.screens.auth.changePassword.ChangePasswordScreen
import com.example.fooddelivery.ui.screens.customer.profile.EditProfileScreen
import com.example.fooddelivery.ui.screens.customer.profile.ProfileScreen
import com.example.fooddelivery.ui.screens.address.AddAddressScreen
import com.example.fooddelivery.ui.screens.address.CustomerAddressScreen
import com.example.fooddelivery.ui.screens.customer.favourite.FavouriteScreen
import com.example.fooddelivery.ui.screens.customer.review.UserReviewScreen
import com.example.fooddelivery.ui.screens.restaurant.restaurant_details.RestaurantDetailScreen
import com.example.fooddelivery.ui.screens.customer.order.OrdersScreen
import com.example.fooddelivery.ui.screens.customer.order.TrackOrderScreen
import com.example.fooddelivery.ui.screens.chat.ChatScreen
import com.example.fooddelivery.ui.screens.chat.ConversationScreen
import com.example.fooddelivery.ui.screens.chat.UnreadChatViewModel
import com.example.fooddelivery.ui.screens.rating_reviews.RatingReviewScreen
import com.example.fooddelivery.ui.screens.category.CategoryFilterScreen
import com.example.fooddelivery.ui.screens.category.AllCategoriesScreen
import com.example.fooddelivery.ui.screens.customer.home.restaurant.AllRestaurantScreen
import com.example.fooddelivery.ui.screens.auth.resetEmail.ResetEmailScreen
import com.example.fooddelivery.ui.screens.auth.register.PolicyScreen
import com.example.fooddelivery.ui.screens.admin.setting.AdminSettingScreen
import com.example.fooddelivery.ui.screens.rating_reviews.restaurant_reviews.ReviewScreen
import com.example.fooddelivery.ui.screens.restaurant.component.DFoodBottomBar
import com.example.fooddelivery.ui.screens.notification.NotificationScreen
import com.example.fooddelivery.ui.screens.notification.UnreadNotificationViewModel
import com.example.fooddelivery.ui.screens.admin.dashboard.AdminDashboardScreen
import com.example.fooddelivery.ui.screens.restaurant.order.OrderManagementScreen
import com.example.fooddelivery.ui.screens.restaurant.profile.RestaurantPersonalInfoScreen
import com.example.fooddelivery.ui.screens.restaurant.profile.RestaurantProfileScreen
import com.example.fooddelivery.ui.screens.restaurant.revenue.RestaurantRevenueScreen
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.fooddelivery.ui.screens.admin.categories.CreateCategoryScreen
import com.example.fooddelivery.ui.screens.admin.components.AdminBottomBar
import com.example.fooddelivery.ui.screens.admin.order.AdminOrderScreen
import com.example.fooddelivery.ui.screens.admin.revenue.AdminRevenueScreen
import com.example.fooddelivery.ui.screens.admin.user.UserListScreen

@Composable
fun RootNavigationGraph(
    navController: NavHostController,
    startDestination: Any
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val customerBottomBarRoutes = listOf(
        HomeRoute::class,
        MyOrdersRoute::class,
        NotificationRoute::class,
        ProfileRoute::class,
    )

    val showCustomerBottomBar = customerBottomBarRoutes.any { currentDestination?.hasRoute(it) == true }
    val unreadNotificationViewModel: UnreadNotificationViewModel = hiltViewModel()
    val unreadNotificationCount by unreadNotificationViewModel.unreadCount.collectAsStateWithLifecycle()
    val context = LocalContext.current

    when {
        currentDestination?.hasRoute(HomeRoute::class) == true -> {
            BackHandler {
                (context as? Activity)?.finish()
            }
        }
        currentDestination?.hasRoute(MyOrdersRoute::class) == true ||
            currentDestination?.hasRoute(NotificationRoute::class) == true ||
            currentDestination?.hasRoute(ProfileRoute::class) == true -> {
            BackHandler {
                navController.navigate(HomeRoute) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        }
    }

    val rootStartDestination = when (startDestination) {
        CustomerGraph, HomeRoute -> CustomerGraph
        RestaurantGraph -> RestaurantGraph
        AdminGraph -> AdminGraph
        else -> AuthGraph
    }

    val authStartDestination = when (startDestination) {
        OnboardingRoute -> OnboardingRoute
        else -> LoginRoute
    }

    Scaffold(
        bottomBar = {
            if (showCustomerBottomBar) {
                com.example.fooddelivery.ui.components.bottombar.DFoodBottomBar(
                    currentRoute = when {
                        currentDestination?.hasRoute(HomeRoute::class) == true -> "home"
                        currentDestination?.hasRoute(MyOrdersRoute::class) == true -> "orders"
                        currentDestination?.hasRoute(NotificationRoute::class) == true -> "notifications"
                        currentDestination?.hasRoute(ProfileRoute::class) == true -> "profile"
                        else -> ""
                    },
                    unreadNotificationCount = unreadNotificationCount,
                    onItemClick = { item ->
                        val route = when (item) {
                            BottomNavItem.Home -> HomeRoute
                            BottomNavItem.Orders -> MyOrdersRoute
                            BottomNavItem.Notifications -> NotificationRoute
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
            startDestination = rootStartDestination,
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
                startDestination = authStartDestination
            )
            userNavGraph(navController = navController)
            vendorNavGraph(navController = navController)
            adminNavGraph(navController = navController)
        }
    }
}

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
                onNavigateAfterLogin = { destination ->
                    navController.navigate(destination) {
                        popUpTo<AuthGraph> { inclusive = true }
                    }
                }
            )
        }

        composable<RegisterRoute> {
            RegisterScreen(
                onNavigateBack = { navController.popBackStack()},
                onNavigateToLogin = { navController.popBackStack() },
                onNavigateHome = {
                    navController.navigate(CustomerGraph) {
                        popUpTo<AuthGraph> { inclusive = true }
                    }
                },
                onNavigateToVerification = { email ->
                    navController.navigate(VerificationRoute(email = email, isFromRegistration = true))
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
                onVerificationSuccess = { email, resetToken ->
                    if (route.isFromRegistration) {
                        navController.navigate(RegistrationSuccessRoute) {
                            popUpTo<VerificationRoute> { inclusive = true }
                        }
                    } else {
                        navController.navigate(ResetPasswordRoute(resetToken = resetToken ?: ""))
                    }
                }
            )
        }

        composable<ResetPasswordRoute> { backStackEntry ->
            val args = backStackEntry.toRoute<ResetPasswordRoute>()
            ResetPasswordScreen(
                resetToken = args.resetToken,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToLogin = {
                    navController.navigate(LoginRoute) {
                        popUpTo<AuthGraph> { inclusive = true }
                    }
                }
            )
        }
    }
}

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
                onNavigateToEditProfile = { navController.navigate(EditProfileRoute) },
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
                },
                onNavigateToManageAddress = {
                    navController.navigate(MyAddressRoute)
                },
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
            val args = backStackEntry.toRoute<RestaurantDetailRoute>()
            RestaurantDetailScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToFoodDetail = { foodId ->
                    navController.navigate(FoodDetailRoute(foodId = foodId))
                },
                onNavigateToReviews = { restaurantId ->
                    val resId = restaurantId.toIntOrNull() ?: args.restaurantId.toIntOrNull() ?: return@RestaurantDetailScreen
                    navController.navigate(RestaurantReviewsRoute(restaurantId = resId))
                },
                onNavigateToCart = { navController.navigate(CartRoute) },
                onNavigateToCheckout = { restaurantId, restaurantName ->
                    navController.navigate(
                        CheckoutRoute(
                            restaurantId = restaurantId,
                            restaurantName = restaurantName
                        )
                    )
                }
            )
        }

        composable<FoodDetailRoute> {
            FoodDetailScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToRestaurant = { restaurantId ->
                    navController.navigate(RestaurantDetailRoute(restaurantId))
                },
                onNavigateToCart = { navController.navigate(CartRoute) },
                onNavigateToCheckout = { restaurantId, restaurantName ->
                    navController.navigate(
                        CheckoutRoute(
                            restaurantId = restaurantId,
                            restaurantName = restaurantName
                        )
                    )
                }
            )
        }
        composable<RestaurantReviewsRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<RestaurantReviewsRoute>()
            ReviewScreen(
                restaurantId = route.restaurantId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEdit = { orderId, restaurantId, name, image, rating, comment, reviewId ->
                    navController.navigate(
                        RatingReviewRoute(
                            orderId = orderId?.toString() ?: "",
                            restaurantId = restaurantId.toString(),
                            restaurantName = name,
                            restaurantImage = image,
                            initialRating = rating,
                            initialComment = comment,
                            reviewId = reviewId?.toString()
                        )
                    )
                }
            )
        }

        composable<CartRoute> {
            CartScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToCheckout = { restaurantId, restaurantName, discount, voucherId ->
                    navController.navigate(
                        CheckoutRoute(
                            restaurantId = restaurantId,
                            restaurantName = restaurantName,
                            discount = discount,
                            voucherId = voucherId
                        )
                    )
                }
            )
        }

        composable<CheckoutRoute> {
            CheckoutScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAddAddress = { navController.navigate(AddAddressRoute()) },
                onNavigateToPaymentSuccessful = { orderId ->
                    navController.navigate(CheckoutSuccessRoute(orderId = orderId)) {
                        popUpTo<CheckoutRoute> { inclusive = true }
                    }
                }
            )
        }

        composable<CheckoutSuccessRoute> { backStackEntry ->
            val args = backStackEntry.toRoute<CheckoutSuccessRoute>()
            CheckoutSuccessScreen(
                onTrackOrder = {
                    navController.navigate(TrackOrderRoute(orderId = args.orderId)) {
                        popUpTo<CheckoutSuccessRoute> { inclusive = true }
                    }
                }
            )
        }

        composable<MyOrdersRoute> {
            OrdersScreen(
                showBackButton = false,
                onNavigateBack = {},
                onNavigateToTrackOrder = { orderId -> navController.navigate(TrackOrderRoute(orderId = orderId)) },
                onNavigateToRate = { orderId, restaurantId, restaurantName ->
                    navController.navigate(RatingReviewRoute(orderId = orderId, restaurantId = restaurantId, restaurantName = restaurantName))
                },
                onNavigateToCart = { navController.navigate(CartRoute) }
            )
        }

        composable<RatingReviewRoute> {
            RatingReviewScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<ProfileRoute> {
            ProfileScreen(
                showBackButton = false,
                onNavigateBack = {},
                onEditProfile = { navController.navigate(EditProfileRoute) },
                onManageAddress = { navController.navigate(MyAddressRoute) },
                onNavigateToBusinessRegistration = {
                    navController.navigate(RestaurantPersonalInfoRoute(isFromSignUp = true))
                },
                onNavigateToCart = { navController.navigate(CartRoute) },
                onNavigateToFavourite = { navController.navigate(FavouriteRoute) },
                onNavigateToReview = { navController.navigate(UserReviewRoute) },
                onChangePassword = { navController.navigate(ChangePasswordRoute) },
                onResetEmail = { navController.navigate(ResetEmailRoute) },
                onLogout = {
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

        composable<NotificationRoute> {
            NotificationScreen(
                showBackButton = false,
                onNavigateBack = {},
                onNavigateToOrder = { orderId ->
                    navController.navigate(TrackOrderRoute(orderId = orderId))
                }
            )
        }

        composable<UserReviewRoute> {
            UserReviewScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEdit = { orderId, restaurantId, name, image, rating, comment, reviewId ->
                    navController.navigate(
                        RatingReviewRoute(
                            orderId = orderId,
                            restaurantId = restaurantId,
                            restaurantName = name,
                            restaurantImage = image,
                            initialRating = rating,
                            initialComment = comment,
                            reviewId = reviewId
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
                onChatWithRestaurant = { orderId, sellerId, name, image ->
                    navController.navigate(ChatRoute(
                        orderId = orderId,
                        sellerId = sellerId,
                        restaurantName = name,
                        restaurantImage = image
                    ))
                }
            )
        }

        composable<SearchRoute> {
            SearchScreen(
                onNavigateBack = { navController.popBackStack() },
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
                onNavigateToChat = { id, name, image ->
                    navController.navigate(ChatRoute(conversationId = id, restaurantName = name, restaurantImage = image))
                }
            )
        }
        composable<RestaurantPersonalInfoRoute> {
            RestaurantPersonalInfoScreen(
                navController = navController,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToSelectAddress = {
                    navController.navigate(BusinessAddressRoute(isFromSignUp = true))
                },
                onRegistrationComplete = {
                    navController.navigate(RestaurantGraph) {
                        popUpTo<HomeRoute> { inclusive = true }
                    }
                }
            )
        }

        composable<BusinessAddressRoute> { backStackEntry ->
            val args = backStackEntry.toRoute<BusinessAddressRoute>()
            AddAddressScreen(
                onNavigateBack = { navController.popBackStack() },
                onAddressSaved = {
                    if (args.isFromSignUp) {
                        navController.previousBackStackEntry?.savedStateHandle?.set("address_saved_signal", true)
                    }
                    navController.popBackStack()
                }
            )
        }

        composable<ChatRoute> { backStackEntry ->
            val args = backStackEntry.toRoute<ChatRoute>()
            ChatScreen(
                conversationId = args.conversationId,
                orderId = args.orderId,
                sellerId = args.sellerId,
                restaurantName = args.restaurantName,
                restaurantImage = args.restaurantImage,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

fun NavGraphBuilder.vendorNavGraph(navController: NavHostController) {
    composable<RestaurantGraph> {
        val vendorNavController = androidx.navigation.compose.rememberNavController()
        val navBackStackEntry by vendorNavController.currentBackStackEntryAsState()
        val currentRouteStr = navBackStackEntry?.destination?.route ?: ""

        val isDashboard = currentRouteStr.contains("RestaurantDashboardRoute")
        val isMenu = currentRouteStr.contains("RestaurantFoodListRoute")
        val isNotifications = currentRouteStr.contains("RestaurantNotificationsRoute")
        val isProfile = currentRouteStr.contains("RestaurantProfileRoute")
        val isMessages = currentRouteStr.contains("ConversationRoute") || currentRouteStr.contains("ChatRoute")
        val unreadChatViewModel: UnreadChatViewModel = hiltViewModel()
        val unreadMessageCount by unreadChatViewModel.unreadCount.collectAsStateWithLifecycle()
        val showBottomBar = isDashboard || isMenu || isNotifications || isProfile || isMessages

        val vendorCurrentRoute = when {
            isDashboard -> "dashboard"
            isMenu -> "menu"
            isNotifications -> "notifications"
            isProfile -> "profile"
            isMessages -> "messages"
            else -> "dashboard"
        }

        val onVendorNavigate: (String) -> Unit = { route ->
            when (route) {
                "dashboard" -> vendorNavController.navigate(RestaurantDashboardRoute)
                "menu" -> vendorNavController.navigate(RestaurantFoodListRoute)
                "notifications" -> vendorNavController.navigate(RestaurantNotificationsRoute)
                "profile" -> vendorNavController.navigate(RestaurantProfileRoute)
                "coupons" -> vendorNavController.navigate(RestaurantCouponRoute)
                "messages" -> vendorNavController.navigate(ConversationRoute)
                "order_management" -> vendorNavController.navigate(RestaurantOrderManagementRoute)
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
                                "messages" -> ConversationRoute
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
                        onSeeAllReviewsClick = { resId ->
                            vendorNavController.navigate(
                                RestaurantReviewsRoute(restaurantId = resId)
                            )
                        },
                        onSeeRevenueClick = { resId ->
                            vendorNavController.navigate(
                                RestaurantRevenueRoute(restaurantId = resId)
                            )
                        },
                        onAddFoodClick = onAddFood,
                        onNavigate = onVendorNavigate,
                        unreadMessageCount = unreadMessageCount,
                        onNavigateToMessages = { vendorNavController.navigate(ConversationRoute) }
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

                composable<RestaurantOrderManagementRoute> {
                    OrderManagementScreen(
                        onNavigateBack = { vendorNavController.popBackStack() },
                        onChatWithCustomer = { orderId, conversationId, customerName ->
                            if (conversationId != null) {
                                vendorNavController.navigate(
                                    ChatRoute(
                                        conversationId = conversationId.toString(),
                                        restaurantName = customerName
                                    )
                                )
                            } else {
                                vendorNavController.navigate(
                                    ChatRoute(
                                        orderId = orderId,
                                        restaurantName = customerName
                                    )
                                )
                            }
                        }
                    )
                }

                composable<RestaurantReviewsRoute> { backStackEntry ->
                    val route = backStackEntry.toRoute<RestaurantReviewsRoute>()
                    ReviewScreen(
                        restaurantId = route.restaurantId,
                        onNavigateBack = { vendorNavController.popBackStack() },
                        onNavigateToEdit = { _, _, _, _, _, _, _ ->
                        }
                    )
                }
                composable<RestaurantRevenueRoute> { backStackEntry ->
                    val route = backStackEntry.toRoute<RestaurantRevenueRoute>()
                    RestaurantRevenueScreen(
                        onNavigateBack = { vendorNavController.popBackStack() }
                    )
                }

                composable<RestaurantNotificationsRoute> {
                    NotificationScreen(
                        showBackButton = false,
                        onNavigateBack = {},
                        onNavigateToOrder = {
                            vendorNavController.navigate(RestaurantOrderManagementRoute)
                        },
                        onNavigateToChat = { conversationId ->
                            vendorNavController.navigate(ChatRoute(conversationId = conversationId))
                        }
                    )
                }

                composable<RestaurantPersonalInfoRoute> {
                    RestaurantPersonalInfoScreen(
                        navController = vendorNavController,
                        onNavigateBack = { vendorNavController.popBackStack() },
                        onNavigateToSelectAddress = {
                            vendorNavController.navigate(BusinessAddressRoute(isFromSignUp = false))
                        },
                        onRegistrationComplete = {
                        }
                    )
                }
                composable<BusinessAddressRoute> { backStackEntry ->
                    val args = backStackEntry.toRoute<BusinessAddressRoute>()

                    AddAddressScreen(
                        onNavigateBack = {
                            vendorNavController.popBackStack()
                        },
                        onAddressSaved = {
                            vendorNavController.previousBackStackEntry?.savedStateHandle?.set("address_saved_signal", true)

                            vendorNavController.popBackStack()
                        }
                    )
                }

                composable<RestaurantProfileRoute> {
                    RestaurantProfileScreen(
                        onNavigateToPersonalInfo = {
                            vendorNavController.navigate(RestaurantPersonalInfoRoute())
                        },
                        onNavigateToAddress = {
                            vendorNavController.navigate(AddAddressRoute())
                        },
                        onNavigateToOrders = {
                            vendorNavController.navigate(RestaurantOrderManagementRoute)
                        },
                        onNavigateToReviews = {
                            vendorNavController.navigate(RestaurantReviewsRoute(restaurantId = 0))
                        },
                        onNavigateToConversation = {
                            vendorNavController.navigate(ConversationRoute)
                        },
                        onNavigateToResetPassword = {
                            vendorNavController.navigate(ChangePasswordRoute)
                        },
                        onLogout = {
                            navController.navigate(AuthGraph) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    )
                }
                composable<AddAddressRoute> { backStackEntry ->
                    val args = backStackEntry.toRoute<AddAddressRoute>()

                    AddAddressScreen(
                        onNavigateBack = {
                            vendorNavController.popBackStack()
                        },
                        onAddressSaved = {
                            vendorNavController.popBackStack()
                        }
                    )
                }
                composable<ChangePasswordRoute> {
                    ChangePasswordScreen(
                        onNavigateBack = {
                            vendorNavController.popBackStack()
                        }
                    )
                }
                composable<ConversationRoute> {
                    ConversationScreen(
                        screenTitle = "Customer Messages",
                        onNavigateBack = { vendorNavController.popBackStack() },
                        onNavigateToChat = { id, name, image ->
                            vendorNavController.navigate(ChatRoute(conversationId = id, restaurantName = name, restaurantImage = image))
                        }
                    )
                }

                composable<ChatRoute> { backStackEntry ->
                    val args = backStackEntry.toRoute<ChatRoute>()
                    ChatScreen(
                        conversationId = args.conversationId,
                        orderId = args.orderId,
                        sellerId = args.sellerId,
                        restaurantName = args.restaurantName,
                        restaurantImage = args.restaurantImage,
                        onNavigateBack = { vendorNavController.popBackStack() }
                    )
                }


                composable<RestaurantCouponRoute> {
                    RestaurantCouponScreen(
                        onNavigateBack = { vendorNavController.popBackStack() },
                        onCreateCouponClick = {
                            vendorNavController.navigate(CreateCouponRoute())
                        }
                    )
                }

                composable<CreateCouponRoute> {
                    CreateCouponScreen(
                        onNavigateBack = { vendorNavController.popBackStack() }
                    )
                }
            }
        }
    }
}

// admin graph
fun NavGraphBuilder.adminNavGraph(navController: NavHostController) {
    composable<AdminGraph> {
        val adminNavController = rememberNavController()
        val navBackStackEntry by adminNavController.currentBackStackEntryAsState()
        val currentRouteStr = navBackStackEntry?.destination?.route ?: ""

        val currentTabRoute = when {
            currentRouteStr.contains("AdminDashboardRoute") -> "dashboard"
            currentRouteStr.contains("AdminCouponRoute") -> "coupons"
            currentRouteStr.contains("AdminCategoriesRoute") -> "categories"
            currentRouteStr.contains("AdminNotificationRoute") -> "notification"
            currentRouteStr.contains("AdminSettingsRoute") -> "settings"
            else -> ""
        }

        val showBottomBar = currentTabRoute.isNotEmpty()

        val onAdminNavigate: (String) -> Unit = { route ->
            when (route) {
                "dashboard" -> adminNavController.navigate(AdminDashboardRoute)
                "categories" -> adminNavController.navigate(AdminCategoriesRoute)
                "coupons" -> adminNavController.navigate(AdminCouponRoute)
                "settings" -> adminNavController.navigate(AdminSettingsRoute)
                "notifications" -> adminNavController.navigate(AdminNotificationRoute)
                "users" -> adminNavController.navigate(AdminUserListRoute)
                "restaurants" -> adminNavController.navigate(AdminRestaurantsRoute)
                "orders" -> adminNavController.navigate(AdminOrdersRoute)
                "revenue" -> adminNavController.navigate(AdminRevenueRoute)
            }
        }

        Scaffold(
            bottomBar = {
                if (showBottomBar) {
                    AdminBottomBar(
                        currentRoute = currentTabRoute,
                        onTabSelected = { tab ->
                            val targetRoute = when (tab.route) {
                                "dashboard" -> AdminDashboardRoute
                                "coupons" -> AdminCouponRoute
                                "categories" -> AdminCategoriesRoute
                                "notification" -> AdminNotificationRoute
                                "settings" -> AdminSettingsRoute
                                else -> AdminDashboardRoute
                            }
                            adminNavController.navigate(targetRoute) {
                                popUpTo(adminNavController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        ) { paddingValues ->
            NavHost(
                navController = adminNavController,
                startDestination = AdminDashboardRoute,
                modifier = Modifier.padding(paddingValues)
            ) {
                composable<AdminDashboardRoute> {
                    AdminDashboardScreen(
                        onNavigate = onAdminNavigate
                    )
                }
                composable<AdminOrdersRoute> {
                    AdminOrderScreen(
                        onNavigateBack = { adminNavController.popBackStack() }
                    )
                }
                composable<AdminRestaurantsRoute> {
                    AdminRestaurantScreen(
                        onNavigateBack = { adminNavController.popBackStack() },
                        onNavigate = onAdminNavigate,
                        onNavigateToRestaurantDetail = { id ->
                            navController.navigate(RestaurantDetailRoute(restaurantId = id.toString()))
                        }
                    )
                }
                composable<AdminRevenueRoute> {
                    AdminRevenueScreen(
                        onBackClick = { adminNavController.popBackStack() }
                    )
                }
                composable<AdminUserListRoute> {
                    UserListScreen(
                        onBackClick = { adminNavController.popBackStack() }
                    )
                }
                composable<AdminCategoriesRoute> {
                    AdminCategoryScreen(
                        onNavigateBack = { adminNavController.popBackStack() },
                        onNavigateToAdd = {
                            adminNavController.navigate(CreateCategoryRoute(categoryId = null))
                        },
                        onNavigateToEdit = { categoryId ->
                            adminNavController.navigate(CreateCategoryRoute(categoryId = categoryId.toString()))
                        },
                        onNavigate = onAdminNavigate
                    )
                }

                composable<AdminCouponRoute> {
                    AdminCouponScreen(
                        onNavigateBack = { adminNavController.popBackStack() },
                        onNavigateToEditCoupon = {},
                        onNavigateToCreateCoupon = {
                            adminNavController.navigate(CreateCouponRoute())
                        }
                    )
                }

                composable<CreateCouponRoute> {
                    CreateCouponScreen(
                        onNavigateBack = { adminNavController.popBackStack() }
                    )
                }
                composable<CreateCategoryRoute> {
                    CreateCategoryScreen(
                        onNavigateBack = { adminNavController.popBackStack() }
                    )
                }

                composable<AdminNotificationRoute> {
                    NotificationScreen(
                        showBackButton = false,
                        onNavigateBack = {},
                        onNavigateToOrder = {
                            adminNavController.navigate(AdminOrdersRoute)
                        }
                    )
                }

                composable<AdminSettingsRoute> {
                    AdminSettingScreen(
                        onNavigateToResetPassword = {
                            navController.navigate(ChangePasswordRoute)
                        },
                        onLogoutSuccess = {
                            navController.navigate(AuthGraph) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    )
                }
            }
        }
    }
}
