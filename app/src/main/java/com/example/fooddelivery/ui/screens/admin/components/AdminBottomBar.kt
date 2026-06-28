package com.example.fooddelivery.ui.screens.admin.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fooddelivery.ui.components.layout.NavigationBarBottomSpacer
import com.example.fooddelivery.ui.theme.DFoodTheme

@Composable
fun AdminBottomBar(
    currentRoute: String?,
    onTabSelected: (AdminTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme

    // 🌟 Dùng Surface để tách biệt lớp nền hoàn hảo
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 20.dp, // Đẩy shadow cao lên tạo chiều sâu, tách biệt hẳn với màn hình bên dưới
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                clip = false
            ),
        color = Color.White,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(84.dp)
                    .padding(top = 10.dp, bottom = 6.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
            adminTabs.forEach { tab ->
                val isSelected = currentRoute == tab.route
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable(
                            onClick = { onTabSelected(tab) },
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple()
                        )
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (isSelected) colorScheme.primaryContainer.copy(alpha = 0.5f)
                                else Color.Transparent
                            )
                            .padding(horizontal = 18.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = tab.title,
                            tint = if (isSelected) colorScheme.primary else colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    if (isSelected) {
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = tab.title,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
            }
            NavigationBarBottomSpacer()
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
    data object Notification : AdminTab("notification", "Alerts", Icons.Default.Notifications)
    data object Settings : AdminTab("settings", "Settings", Icons.Default.Settings)
}

val adminTabs = listOf(
    AdminTab.Dashboard,
    AdminTab.Coupons,
    AdminTab.Categories,
    AdminTab.Notification,
    AdminTab.Settings
)

@Preview(showBackground = true)
@Composable
fun AdminBottomBarPreview() {
    DFoodTheme {
        Box(modifier = Modifier.background(Color(0xFFFFF5F0)).padding(top = 40.dp)) {
            AdminBottomBar(
                currentRoute = "dashboard",
                onTabSelected = {}
            )
        }
    }
}