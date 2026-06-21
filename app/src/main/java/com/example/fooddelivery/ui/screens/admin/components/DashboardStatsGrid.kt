package com.example.fooddelivery.ui.screens.admin.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.fooddelivery.ui.screens.admin.dashboard.DashboardStats

@Composable
fun DashboardStatsGrid(
    stats: DashboardStats,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            DashboardStatCard(
                title = "Total Users",
                value = stats.users.toString(),
                icon = Icons.Default.People,
                iconContainerColor = Color(0xFFE3F2FD),
                iconColor = Color(0xFF1E88E5),
                modifier = Modifier.weight(1f)
            )
            DashboardStatCard(
                title = "Restaurants",
                value = stats.restaurants.toString(),
                icon = Icons.Default.Storefront,
                iconContainerColor = Color(0xFFE8F5E9),
                iconColor = Color(0xFF43A047),
                modifier = Modifier.weight(1f)
            )
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            DashboardStatCard(
                title = "Total Orders",
                value = stats.orders.toString(),
                icon = Icons.Default.ShoppingBag,
                iconContainerColor = Color(0xFFFFF3E0),
                iconColor = Color(0xFFFB8C00),
                modifier = Modifier.weight(1f)
            )
            DashboardStatCard(
                title = "Payments",
                value = stats.payments.toString(),
                icon = Icons.Default.CreditCard,
                iconContainerColor = Color(0xFFEDE7F6),
                iconColor = Color(0xFF5E35B1),
                modifier = Modifier.weight(1f)
            )
        }

        // Hàng 3: Categories & Vouchers
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            DashboardStatCard(
                title = "Categories",
                value = stats.categories.toString(),
                icon = Icons.Default.Category,
                iconContainerColor = Color(0xFFE0F7FA),
                iconColor = Color(0xFF00ACC1),
                modifier = Modifier.weight(1f)
            )
            DashboardStatCard(
                title = "Vouchers Active",
                value = stats.vouchers.toString(),
                icon = Icons.Default.LocalOffer,
                iconContainerColor = Color(0xFFFCE4EC),
                iconColor = Color(0xFFD81B60),
                modifier = Modifier.weight(1f)
            )
        }
    }
}