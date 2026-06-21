package com.example.fooddelivery.ui.screens.profile.notification.components

import android.text.format.DateUtils
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fooddelivery.domain.model.Notification
import com.example.fooddelivery.domain.model.NotificationType

@Composable
fun NotificationItem(
    notification: Notification,
    onClick: () -> Unit
) {
    val backgroundColor = if (notification.isRead) Color.Transparent else MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.Top
    ) {
        val (icon, color) = getNotificationIcon(notification.type)

        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = notification.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = if (notification.isRead) FontWeight.Medium else FontWeight.Bold,
                        fontSize = 16.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = DateUtils.getRelativeTimeSpanString(notification.timestamp).toString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = notification.message,
                style = MaterialTheme.typography.bodyMedium,
                color = if (notification.isRead) MaterialTheme.colorScheme.onSurfaceVariant 
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun getNotificationIcon(type: NotificationType): Pair<ImageVector, Color> {
    return when (type) {
        NotificationType.ORDER -> Icons.Default.LocalShipping to MaterialTheme.colorScheme.primary
        NotificationType.PROMOTION -> Icons.Default.Percent to MaterialTheme.colorScheme.secondary
        NotificationType.SYSTEM -> Icons.Default.Settings to MaterialTheme.colorScheme.tertiary
        NotificationType.PAYMENT -> Icons.Default.Payments to Color(0xFF4CAF50) // Green for payment
        NotificationType.CHAT -> Icons.Default.Chat to Color(0xFF2196F3) // Blue for chat
    }
}
