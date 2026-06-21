package com.example.fooddelivery.ui.screens.admin.components

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fooddelivery.ui.theme.DFoodTheme

@Composable
fun AdminBottomBar(
    currentRoute: String?,
    onTabSelected: (AdminTab) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    NavigationBar(
        modifier = Modifier
            .height(80.dp)
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
        containerColor = colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        adminTabs.forEach { tab ->
            val isSelected = currentRoute == tab.route

            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(tab) },
                alwaysShowLabel = true,
                label = {
                    Text(
                        text = tab.title,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                icon = {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.title
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = colorScheme.primaryContainer,
                    selectedIconColor = colorScheme.primary,
                    unselectedIconColor = colorScheme.onSurfaceVariant,
                    selectedTextColor = colorScheme.primary,
                    unselectedTextColor = colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

sealed class AdminTab(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    data object Dashboard : AdminTab("dashboard", "Dashboard", Icons.Default.Dashboard)
    data object Coupons : AdminTab("coupons", "Coupons", Icons.Default.ConfirmationNumber)
    data object Categories : AdminTab("categories", "Categories", Icons.Default.Category)
    data object Notification : AdminTab("notification", "Notifications", Icons.Default.Notifications)
    data object Settings : AdminTab("settings", "Settings", Icons.Default.Settings)
}

val adminTabs = listOf(
    AdminTab.Dashboard,
    AdminTab.Coupons,
    AdminTab.Categories,
    AdminTab.Notification,
    AdminTab.Settings
)

@Preview
@Composable
fun AdminBottomBarPreview() {
    DFoodTheme {
        AdminBottomBar(
            currentRoute = "dashboard",
            onTabSelected = {}
        )
    }
}