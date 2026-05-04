package com.example.fooddelivery

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.example.fooddelivery.ui.navigation.RootNavigationGraph
import com.example.fooddelivery.ui.screens.cart.CartScreen
import com.example.fooddelivery.ui.screens.home.HomeScreen
import com.example.fooddelivery.ui.screens.home.restaurant_detail.RestaurantDetailScreen
import com.example.fooddelivery.ui.theme.DFoodTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val mainViewModel: MainViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen();
        super.onCreate(savedInstanceState)

        splashScreen.setKeepOnScreenCondition {
            mainViewModel.isLoading.value
        }
        enableEdgeToEdge()
        setContent {
            DFoodTheme (darkTheme = false) {
                val isLoading = mainViewModel.isLoading.value
                if (!isLoading) {
                    val navController = rememberNavController();
                    RootNavigationGraph(
                        navController = navController,
                        startDestination = mainViewModel.startDestination.value
                    )
                }
            }
        }
    }
}