package com.example.fooddelivery.ui.screens.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Message
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fooddelivery.ui.components.bounceClick

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
            .statusBarsPadding()
            .padding(horizontal = 24.dp, vertical = 0.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier
                    .bounceClick { expanded = true }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "DELIVER TO: ",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    )
                )
                Text(
                    text = selectedLocation,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Icon(
                    Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp).padding(start = 2.dp)
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(MaterialTheme.colorScheme.surface)
            ) {
                availableLocations.forEach { location ->
                    DropdownMenuItem(
                        text = { Text(location) },
                        onClick = { onLocationSelected(location); expanded = false }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(16.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .bounceClick { onMessageClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.AutoMirrored.Outlined.Message, contentDescription = "Messages", tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(24.dp))
                if (unreadMessageCount > 0) {
                    BadgeIcon(count = unreadMessageCount, color = MaterialTheme.colorScheme.error, contentColor = MaterialTheme.colorScheme.onError)
                }
            }
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .bounceClick { onCartClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.ShoppingBag, contentDescription = "Cart", tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(24.dp))
                if (cartItemCount > 0) {
                    BadgeIcon(count = cartItemCount, color = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary)
                }
            }
        }
    }
}

@Composable
private fun BoxScope.BadgeIcon(count: Int, color: androidx.compose.ui.graphics.Color, contentColor: androidx.compose.ui.graphics.Color) {
    Box(
        modifier = Modifier
            .align(Alignment.TopEnd)
            .offset(x = 2.dp, y = (-2).dp)
            .defaultMinSize(minWidth = 20.dp, minHeight = 20.dp)
            .background(color, CircleShape)
            .border(2.dp, MaterialTheme.colorScheme.background, CircleShape)
            .padding(horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (count > 99) "99+" else count.toString(),
            style = MaterialTheme.typography.labelSmall.copy(color = contentColor, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        )
    }
}