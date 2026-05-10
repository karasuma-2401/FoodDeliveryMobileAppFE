package com.example.fooddelivery.ui.components.bottombar

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

sealed class BottomNavItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val route: String
) {
    object Home : BottomNavItem("Home", Icons.Default.Home, Icons.Outlined.Home, "home")
    object Search : BottomNavItem("Search", Icons.Default.Search, Icons.Outlined.Search, "search")
    object Orders : BottomNavItem("Orders", Icons.Default.Assignment, Icons.Outlined.Assignment, "orders")
    object Profile : BottomNavItem("Profile", Icons.Default.Person, Icons.Outlined.Person, "profile")
}

@Composable
fun DFoodBottomBar(
    currentRoute: String,
    onItemClick: (BottomNavItem) -> Unit
) {
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Search,
        BottomNavItem.Orders,
        BottomNavItem.Profile
    )

    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 8.dp,
    ) {
        items.forEach { item ->
            val isSelected = currentRoute == item.route
            
            NavigationBarItem(
                selected = isSelected,
                onClick = { onItemClick(item) },
                alwaysShowLabel = true,
                label = {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontSize = 12.sp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray
                        )
                    )
                },
                icon = {
                    Icon(
                        imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.title,
                        modifier = Modifier.size(24.dp),
                        tint = if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                )
            )
        }
    }
}
