package com.example.fooddelivery.ui.screens.admin.setting

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fooddelivery.ui.screens.admin.components.AdminHeader
import com.example.fooddelivery.ui.screens.restaurant.component.profile.ProfileMenuGroup
import com.example.fooddelivery.ui.screens.restaurant.component.profile.ProfileMenuItem
import com.example.fooddelivery.ui.theme.DFoodTheme

@Composable
fun AdminSettingScreen(
    onNavigateToResetPassword: () -> Unit,
    onLogoutSuccess: () -> Unit, // Đổi tên callback để rõ ngữ nghĩa
    modifier: Modifier = Modifier,
    viewModel: AdminSettingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isLoggedOutSuccessfully) {
        if (uiState.isLoggedOutSuccessfully) {
            onLogoutSuccess()
            viewModel.clearLogoutFlag()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
        ) {
            // Header lấy dữ liệu động từ ViewModel
            AdminHeader(
                adminEmail = uiState.adminEmail,
                totalEarnings = uiState.totalEarnings
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ProfileMenuGroup {
                    ProfileMenuItem(
                        icon = Icons.Default.Lock,
                        iconTint = MaterialTheme.colorScheme.primary,
                        title = "Reset Password",
                        onClick = onNavigateToResetPassword
                    )
                }

                ProfileMenuGroup {
                    ProfileMenuItem(
                        icon = Icons.Default.ExitToApp,
                        iconTint = MaterialTheme.colorScheme.error,
                        title = "Log Out",
                        onClick = { viewModel.logout() }
                    )
                }
            }
        }

        if (uiState.isLoading) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.Black.copy(alpha = 0.2f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AdminProfilePreview() {
    DFoodTheme {
        AdminSettingScreen(
            onNavigateToResetPassword = {},
            onLogoutSuccess = {}
        )
    }
}