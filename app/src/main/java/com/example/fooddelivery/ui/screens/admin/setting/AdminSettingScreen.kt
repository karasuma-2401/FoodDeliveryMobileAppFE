package com.example.fooddelivery.ui.screens.admin.setting

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.fooddelivery.ui.screens.admin.components.AdminHeader
import com.example.fooddelivery.ui.screens.restaurant.component.profile.ProfileMenuGroup
import com.example.fooddelivery.ui.screens.restaurant.component.profile.ProfileMenuItem
import com.example.fooddelivery.ui.theme.DFoodTheme

@Composable
fun AdminSettingScreen(
    onNavigateToResetPassword: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
    totalEarnings: String = "$124,500.80",
    adminEmail: String = "admin@dfood.com"
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        AdminHeader(
            adminEmail = adminEmail,
            totalEarnings = totalEarnings
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
                    onClick = onLogout
                )
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
            onLogout = {}
        )
    }
}