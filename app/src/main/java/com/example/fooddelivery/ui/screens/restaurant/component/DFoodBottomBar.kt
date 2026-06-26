package com.example.fooddelivery.ui.screens.restaurant.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fooddelivery.R

@Composable
fun DFoodBottomBar(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    onAddClick: () -> Unit,
    unreadMessageCount: Int = 0
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp,
        shadowElevation = 16.dp
    ) {
        Row(
            modifier = Modifier
                .navigationBarsPadding()
                .height(80.dp)
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            NavigationIcon(
                iconId = R.drawable.ic_dashboard,
                isSelected = currentRoute == "dashboard",
                onClick = { onNavigate("dashboard") }
            )
            NavigationIcon(
                iconId = R.drawable.ic_menu,
                isSelected = currentRoute == "menu",
                onClick = { onNavigate("menu") }
            )

            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .clickable { onAddClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Item",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            NavigationIcon(
                iconId = R.drawable.ic_chat,
                isSelected = currentRoute == "messages",
                onClick = { onNavigate("messages") },
                badgeCount = unreadMessageCount
            )
            NavigationIcon(
                iconId = R.drawable.ic_notification,
                isSelected = currentRoute == "notifications",
                onClick = { onNavigate("notifications") }
            )
            NavigationIcon(
                iconId = R.drawable.ic_profile,
                isSelected = currentRoute == "profile",
                onClick = { onNavigate("profile") }
            )
        }
    }
}

@Composable
private fun NavigationIcon(
    iconId: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    badgeCount: Int = 0
) {
    Box {
        IconButton(onClick = onClick) {
            Icon(
                painter = painterResource(id = iconId),
                contentDescription = null,
                tint = if (isSelected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
        if (badgeCount > 0) {
            Badge(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 6.dp, end = 4.dp),
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onError
            ) {
                Text(
                    text = if (badgeCount > 99) "99+" else badgeCount.toString(),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 2.dp)
                )
            }
        }
    }
}
