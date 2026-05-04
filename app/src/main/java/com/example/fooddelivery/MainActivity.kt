package com.example.fooddelivery

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
<<<<<<< bugfix-recovered
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
=======
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
>>>>>>> customer
import androidx.navigation.compose.rememberNavController
import com.example.fooddelivery.ui.navigation.RootNavigationGraph
import com.example.fooddelivery.ui.theme.DFoodTheme
import com.example.fooddelivery.ui.util.GlobalSnackbarManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    @Inject
    lateinit var snackbarManager: GlobalSnackbarManager
    
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
<<<<<<< bugfix-recovered
                val snackbarHostState = remember { SnackbarHostState() }
                
                LaunchedEffect(Unit) {
                    snackbarManager.messages.collectLatest { message ->
                        snackbarHostState.showSnackbar(message)
                    }
                }

                val isLoading = mainViewModel.isLoading.value
                if (!isLoading) {
                    val navController = rememberNavController()
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
                    ) { innerPadding ->
                        Box(modifier = Modifier.padding(innerPadding)) {
                            RootNavigationGraph(
                                navController = navController,
                                startDestination = mainViewModel.startDestination.value
                            )
                        }
                    }
=======
                val isLoading by mainViewModel.isLoading.collectAsStateWithLifecycle()
                val startDestination by mainViewModel.startDestination.collectAsStateWithLifecycle()
                if (!isLoading) {
                    val navController = rememberNavController()
                    RootNavigationGraph(
                        navController = navController,
                        startDestination = startDestination
                    )
>>>>>>> customer
                }
            }
        }
    }
}
