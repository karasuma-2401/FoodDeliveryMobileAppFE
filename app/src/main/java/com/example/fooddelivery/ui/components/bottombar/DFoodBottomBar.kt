package com.example.fooddelivery.ui.components.bottombar

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fooddelivery.ui.components.bounceClick

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
    onItemClick: (BottomNavItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Search,
        BottomNavItem.Orders,
        BottomNavItem.Profile
    )

    Box(
        modifier = modifier
            .navigationBarsPadding()
            .padding(bottom = 8.dp)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(28.dp),
            shadowElevation = 15.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEach { item ->
                    val isSelected = currentRoute == item.route

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        contentAlignment = Alignment.Center
                    ) {
                        val contentColor by animateColorAsState(
                            targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                alpha = 0.5f
                            ),
                            animationSpec = tween(durationMillis = 250),
                            label = "color"
                        )

                        val backgroundColor by animateColorAsState(
                            targetValue = if (isSelected) MaterialTheme.colorScheme.primary.copy(
                                alpha = 0.12f
                            ) else Color.Transparent,
                            animationSpec = tween(durationMillis = 250),
                            label = "bg"
                        )
                        Box(
                            modifier = Modifier
                                .animateContentSize(animationSpec = spring(stiffness = Spring.StiffnessLow))
                                .clip(CircleShape)
                                .background(backgroundColor)
                                .bounceClick(
                                    scale = 0.95f,
                                    onClick = { if (!isSelected) onItemClick(item) }
                                )
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.title,
                                    modifier = Modifier.size(20.dp),
                                    tint = contentColor
                                )

                                AnimatedVisibility(
                                    visible = isSelected,
                                    enter = fadeIn() + expandHorizontally(expandFrom = Alignment.Start),
                                    exit = fadeOut() + shrinkHorizontally(shrinkTowards = Alignment.Start)
                                ) {
                                    Text(
                                        text = item.title,
                                        modifier = Modifier.padding(start = 6.dp),
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp
                                        ),
                                        color = contentColor,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
