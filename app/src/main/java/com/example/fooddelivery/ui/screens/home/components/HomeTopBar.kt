package com.example.fooddelivery.ui.screens.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Message
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HomeTopBar(
    selectedLocation: String,
    availableLocations: List<String>,
    onLocationSelected: (String) -> Unit,
    cartItemCount: Int,
    unreadMessageCount: Int,
    onCartClick: () -> Unit,
    onMessageClick: () -> Unit,
    modifier: Modifier = Modifier,
    onBackClick: (() -> Unit)? = null
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onBackClick != null) {
            TopBarIconButton(
                onClick = onBackClick,
                icon = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back"
            )
        } else {
            Spacer(modifier = Modifier.size(45.dp))
        }

        Box(contentAlignment = Alignment.Center, modifier = Modifier.weight(1f)) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clickable { expanded = true }
                    .padding(horizontal = 8.dp)
            ) {
                Text(
                    text = "DELIVER TO",
                    style = MaterialTheme.typography.labelLarge.copy(
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = selectedLocation,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                    Icon(
                        Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                availableLocations.forEach { location ->
                    DropdownMenuItem(
                        text = { Text(location) },
                        onClick = {
                            onLocationSelected(location)
                            expanded = false
                        }
                    )
                }
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            TopBarIconButton(
                onClick = onCartClick,
                icon = Icons.Outlined.ShoppingBag,
                contentDescription = "Cart",
                badgeCount = cartItemCount,
                badgeColor = MaterialTheme.colorScheme.primary,
                onBadgeColor = MaterialTheme.colorScheme.onPrimary
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            TopBarIconButton(
                onClick = onMessageClick,
                icon = Icons.AutoMirrored.Outlined.Message,
                contentDescription = "Messages",
                badgeCount = unreadMessageCount,
                badgeColor = MaterialTheme.colorScheme.error,
                onBadgeColor = MaterialTheme.colorScheme.onError
            )
        }
    }
}

@Composable
private fun TopBarIconButton(
    onClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    badgeCount: Int = 0,
    badgeColor: Color = MaterialTheme.colorScheme.primary,
    onBadgeColor: Color = MaterialTheme.colorScheme.onPrimary
) {
    Box(
        modifier = Modifier
            .size(45.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(22.dp)
        )

        if (badgeCount > 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 2.dp, y = (-2).dp)
                    .size(18.dp)
                    .background(badgeColor, CircleShape)
                    .border(2.dp, MaterialTheme.colorScheme.background, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (badgeCount > 99) "99+" else badgeCount.toString(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = onBadgeColor,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}
