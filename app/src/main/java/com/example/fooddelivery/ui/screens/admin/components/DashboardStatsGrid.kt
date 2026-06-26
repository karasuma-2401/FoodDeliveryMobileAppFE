package com.example.fooddelivery.ui.screens.admin.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.fooddelivery.ui.screens.admin.dashboard.DashboardStats

@Composable
fun DashboardStatsGrid(
    stats: DashboardStats,
    onUsersClick: () -> Unit,
    onRestaurantsClick: () -> Unit,
    onOrdersClick: () -> Unit,
    onPaymentsClick: () -> Unit,
    onCategoriesClick: () -> Unit,
    onVouchersClick: () -> Unit,
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
                modifier = Modifier.weight(1f),
                onClick = onUsersClick
            )
            DashboardStatCard(
                title = "Restaurants",
                value = stats.restaurants.toString(),
                icon = Icons.Default.Storefront,
                iconContainerColor = Color(0xFFE8F5E9),
                iconColor = Color(0xFF43A047),
                modifier = Modifier.weight(1f),
                onClick = onRestaurantsClick
            )
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            DashboardStatCard(
                title = "Total Orders",
                value = stats.orders.toString(),
                icon = Icons.Default.ShoppingBag,
                iconContainerColor = Color(0xFFFFF3E0),
                iconColor = Color(0xFFFB8C00),
                modifier = Modifier.weight(1f),
                onClick = onOrdersClick
            )
            DashboardStatCard(
                title = "Payments",
                value = stats.payments.toString(),
                icon = Icons.Default.CreditCard,
                iconContainerColor = Color(0xFFEDE7F6),
                iconColor = Color(0xFF5E35B1),
                modifier = Modifier.weight(1f),
                onClick = onPaymentsClick
            )
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            DashboardStatCard(
                title = "Categories",
                value = stats.categories.toString(),
                icon = Icons.Default.Category,
                iconContainerColor = Color(0xFFE0F7FA),
                iconColor = Color(0xFF00ACC1),
                modifier = Modifier.weight(1f),
                onClick = onCategoriesClick
            )
            DashboardStatCard(
                title = "Vouchers Active",
                value = stats.vouchers.toString(),
                icon = Icons.Default.LocalOffer,
                iconContainerColor = Color(0xFFFCE4EC),
                iconColor = Color(0xFFD81B60),
                modifier = Modifier.weight(1f),
                onClick = onVouchersClick
            )
        }
    }
}

@Composable
fun DashboardStatCard(
    title: String,
    value: String,
    icon: ImageVector,
    iconContainerColor: Color,
    iconColor: Color,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) Modifier.clickable { onClick() } else Modifier
            ),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconContainerColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}