package com.example.fooddelivery.ui.screens.restaurant.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fooddelivery.ui.screens.restaurant.component.BalanceHeader
import com.example.fooddelivery.ui.screens.restaurant.component.ProfileMenuGroup
import com.example.fooddelivery.ui.screens.restaurant.component.ProfileMenuItem
import com.example.fooddelivery.ui.screens.restaurant.reviews.ReviewScreen
import com.example.fooddelivery.ui.theme.DFoodTheme

@Composable
fun RestaurantProfileScreen(
    onNavigateToPersonalInfo: () -> Unit,
    onNavigateToWithdrawalHistory: () -> Unit,
    onNavigateToReviews: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        BalanceHeader(
            balance = "$500.00",
            onWithdrawClick = { }
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
                    icon = Icons.Default.DateRange,
                    iconTint = MaterialTheme.colorScheme.primary,
                    title = "Withdrawal History",
                    onClick = onNavigateToWithdrawalHistory
                )
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant,
                    thickness = 1.dp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                ProfileMenuItem(
                    icon = Icons.Default.List,
                    iconTint = MaterialTheme.colorScheme.secondary,
                    title = "Number of Orders",
                    trailingContent = {
                        Text(
                            text = "29K",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                )
            }

            ProfileMenuGroup {
                ProfileMenuItem(
                    icon = Icons.Default.Star,
                    iconTint = MaterialTheme.colorScheme.tertiary,
                    title = "User Reviews",
                    onClick = onNavigateToReviews
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
            onNavigateToWithdrawalHistory = {},
            onNavigateToReviews = {},
            onLogout = {}
        )
    }
}