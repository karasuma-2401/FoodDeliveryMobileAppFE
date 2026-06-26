package com.example.fooddelivery.ui.screens.notification.components

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.example.fooddelivery.domain.model.effectiveActions

@Composable
fun NotificationItem(
    notification: Notification,
    onClick: () -> Unit,
    onActionClick: (String) -> Unit = {},
    isProcessing: Boolean = false,
    modifier: Modifier = Modifier
) {
    val actions = notification.effectiveActions()
    if (actions.isNotEmpty()) {
        NotificationActionCard(
            notification = notification,
            actions = actions,
            onActionClick = onActionClick,
            isProcessing = isProcessing,
            modifier = modifier
        )
    } else {
        NotificationRowItem(
            notification = notification,
            onClick = onClick,
            modifier = modifier
        )
    }
}

@Composable
private fun NotificationRowItem(
    notification: Notification,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (notification.isRead) {
        Color.Transparent
    } else {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
    }

    Row(
        modifier = modifier
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
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
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
                color = if (notification.isRead) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                },
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun NotificationActionCard(
    notification: Notification,
    actions: List<String>,
    onActionClick: (String) -> Unit,
    isProcessing: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val (icon, color) = getNotificationIcon(notification.type)
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(color.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = notification.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = DateUtils.getRelativeTimeSpanString(notification.timestamp).toString(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = notification.message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (isProcessing) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                }
            } else {
                NotificationActionButtons(
                    actions = actions,
                    onActionClick = onActionClick
                )
            }
        }
    }
}

@Composable
private fun NotificationActionButtons(
    actions: List<String>,
    onActionClick: (String) -> Unit
) {
    val positiveActions = actions.filter {
        it.uppercase() in listOf("ACCEPT_ORDER", "CONFIRM_RECEIVED", "APPROVE_VENDOR", "APPROVE")
    }
    val negativeActions = actions.filter {
        it.uppercase() in listOf("REJECT_ORDER", "REJECT_VENDOR", "REJECT")
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        negativeActions.forEach { action ->
            OutlinedButton(
                onClick = { onActionClick(action) },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(actionLabel(action), fontWeight = FontWeight.Bold)
            }
        }
        positiveActions.forEach { action ->
            Button(
                onClick = { onActionClick(action) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(actionLabel(action), fontWeight = FontWeight.Bold)
            }
        }
    }
}

private fun actionLabel(action: String): String {
    return when (action.uppercase()) {
        "ACCEPT_ORDER" -> "Accept"
        "REJECT_ORDER" -> "Reject"
        "CONFIRM_RECEIVED" -> "Confirm Received"
        "APPROVE_VENDOR", "APPROVE" -> "Approve"
        "REJECT_VENDOR", "REJECT" -> "Reject"
        else -> action
    }
}

@Composable
private fun getNotificationIcon(type: NotificationType): Pair<ImageVector, Color> {
    return when (type) {
        NotificationType.ORDER -> Icons.Default.LocalShipping to MaterialTheme.colorScheme.primary
        NotificationType.PROMOTION -> Icons.Default.Percent to MaterialTheme.colorScheme.secondary
        NotificationType.SYSTEM -> Icons.Default.Settings to MaterialTheme.colorScheme.tertiary
        NotificationType.PAYMENT -> Icons.Default.Payments to Color(0xFF4CAF50)
        NotificationType.CHAT -> Icons.Default.Chat to Color(0xFF2196F3)
    }
}
