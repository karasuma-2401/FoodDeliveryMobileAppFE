package com.example.fooddelivery

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.example.fooddelivery.ui.navigation.RootNavigationGraph
import com.example.fooddelivery.ui.theme.DFoodTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val mainViewModel: MainViewModel by viewModels()

    private val requestNotificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* granted or denied — push registration handled separately */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen();
        super.onCreate(savedInstanceState)

        requestNotificationPermissionIfNeeded()

        splashScreen.setKeepOnScreenCondition {
            mainViewModel.isLoading.value
        }
        enableEdgeToEdge()
        setContent {
            val isDarkMode by mainViewModel.isDarkMode.collectAsStateWithLifecycle()
            
            DFoodTheme(darkTheme = isDarkMode) {
                val isLoading = mainViewModel.isLoading.value
                if (!isLoading) {
                    val navController = rememberNavController()
                    RootNavigationGraph(
                        navController = navController,
                        startDestination = mainViewModel.startDestination.value
                    )
                }
            }
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}