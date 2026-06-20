package com.example.fooddelivery.ui.screens.restaurant

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.fooddelivery.ui.screens.restaurant.component.DFoodBottomBar
import com.example.fooddelivery.ui.screens.restaurant.coupon.RestaurantCouponScreen
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "dashboard"
    val screensWithBottomBar = listOf("dashboard", "menu", "notifications", "profile")

    Scaffold(
        bottomBar = {
            if (currentRoute in screensWithBottomBar) {
                DFoodBottomBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onAddClick = { /* Handle sự kiện bấm nút chính giữa (+) */ }
                )
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "dashboard",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("dashboard") { MockScreen(title = "Dashboard Screen") }
            composable("menu") { MockScreen(title = "Menu Screen") }
            composable("notifications") { MockScreen(title = "Notifications Screen") }
            composable("profile") { MockScreen(title = "Profile Screen") }
            composable("coupon_management") {
                RestaurantCouponScreen(
                    onNavigateBack = { navController.navigateUp() }
                )
            }
        }
    }
}


@Composable
fun MockScreen(title: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        androidx.compose.material3.Text(text = title)
    }
}