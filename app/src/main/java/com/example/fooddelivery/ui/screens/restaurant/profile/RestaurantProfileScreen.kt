package com.example.fooddelivery.ui.screens.restaurant.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fooddelivery.ui.screens.restaurant.component.profile.BalanceHeader
import com.example.fooddelivery.ui.screens.restaurant.component.profile.ProfileMenuGroup
import com.example.fooddelivery.ui.screens.restaurant.component.profile.ProfileMenuItem
import com.example.fooddelivery.ui.theme.DFoodTheme

@Composable
fun RestaurantProfileScreen(
    onNavigateToPersonalInfo: () -> Unit,
    onNavigateToOrders: () -> Unit,
    onNavigateToAddress: () -> Unit,
    onNavigateToReviews: (Int) -> Unit,
    onNavigateToConversation: () -> Unit,
    onNavigateToResetPassword: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RestaurantProfileViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        BalanceHeader(
            balance = uiState.balance
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ProfileMenuGroup {
                ProfileMenuItem(
                    icon = Icons.Default.AccountCircle,
                    iconTint = MaterialTheme.colorScheme.primary,
                    title = "Personal Info",
                    onClick = onNavigateToPersonalInfo
                )
            }

            ProfileMenuGroup {
                ProfileMenuItem(
                    icon = Icons.Default.LocationOn,
                    iconTint = MaterialTheme.colorScheme.primary,
                    title = "Business Address",
                    onClick = onNavigateToAddress
                )
            }

            ProfileMenuGroup {
                ProfileMenuItem(
                    icon = Icons.Default.List,
                    iconTint = MaterialTheme.colorScheme.secondary,
                    title = "Number of Orders",
                    onClick = onNavigateToOrders
                )
            }

            ProfileMenuGroup {
                ProfileMenuItem(
                    icon = Icons.Default.Star,
                    iconTint = MaterialTheme.colorScheme.tertiary,
                    title = "User Reviews",
                    onClick = { onNavigateToReviews(uiState.restaurantId) }
                )
            }

            ProfileMenuGroup {
                ProfileMenuItem(
                    icon = Icons.Default.Chat,
                    iconTint = MaterialTheme.colorScheme.secondary,
                    title = "Conversation",
                    onClick = onNavigateToConversation
                )
            }

            ProfileMenuGroup {
                ProfileMenuItem(
                    icon = Icons.Default.Settings,
                    iconTint = MaterialTheme.colorScheme.tertiary,
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
fun RestaurantProfilePreview() {
    DFoodTheme {
        RestaurantProfileScreen(
            onNavigateToPersonalInfo = {},
            onNavigateToOrders = {},
            onNavigateToAddress = {},
            onNavigateToReviews = {},
            onNavigateToConversation = {},
            onLogout = {},
            onNavigateToResetPassword = {}
        )
    }
}