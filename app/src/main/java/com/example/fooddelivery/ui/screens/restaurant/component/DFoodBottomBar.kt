package com.example.fooddelivery.ui.screens.restaurant.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fooddelivery.R
import com.example.fooddelivery.ui.components.bounceClick
import com.example.fooddelivery.ui.components.layout.NavigationBarBottomSpacer
import com.example.fooddelivery.ui.theme.CustomerDimens

private val BottomBarShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)

@Composable
fun DFoodBottomBar(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    onAddClick: () -> Unit,
    unreadNotificationCount: Int = 0
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = BottomBarShape,
        shadowElevation = 8.dp,
    ) {
        Column(Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(CustomerDimens.bottomNavHeight),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    NavigationIcon(
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        iconId = R.drawable.ic_dashboard,
                        isSelected = currentRoute == "dashboard",
                        onClick = { onNavigate("dashboard") }
                    )
                    NavigationIcon(
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        iconId = R.drawable.ic_menu,
                        isSelected = currentRoute == "menu",
                        onClick = { onNavigate("menu") }
                    )
                }

                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(52.dp)
                        .shadow(4.dp, CircleShape)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .clickable { onAddClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Item",
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Row(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    NavigationIcon(
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        iconId = R.drawable.ic_notification,
                        isSelected = currentRoute == "notifications",
                        onClick = { onNavigate("notifications") },
                        badgeCount = unreadNotificationCount
                    )
                    NavigationIcon(
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        iconId = R.drawable.ic_profile,
                        isSelected = currentRoute == "profile",
                        onClick = { onNavigate("profile") }
                    )
                }
            }
            NavigationBarBottomSpacer()
        }
    }
}

@Composable
private fun NavigationIcon(
    modifier: Modifier = Modifier,
    iconId: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    badgeCount: Int = 0
) {
    val iconTint by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
        },
        animationSpec = tween(durationMillis = 200),
        label = "IconTint"
    )

    Column(
        modifier = modifier
            .bounceClick(
                scale = 0.96f,
                onClick = onClick
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                painter = painterResource(id = iconId),
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(CustomerDimens.bottomNavIconSize)
            )

            if (badgeCount > 0) {
                Badge(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 8.dp, y = (-6).dp)
                        .defaultMinSize(minWidth = 16.dp, minHeight = 16.dp),
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                ) {
                    Text(
                        text = if (badgeCount > 99) "99+" else badgeCount.toString(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.padding(horizontal = 2.dp)
                    )
                }
            }
        }
    }
}