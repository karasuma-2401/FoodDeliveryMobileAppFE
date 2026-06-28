package com.example.fooddelivery.ui.screens.customer.order.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.fooddelivery.ui.screens.customer.order.TrackingStatus
import com.example.fooddelivery.ui.theme.DFoodTheme

@Composable
fun OrderStatusHeroCard(
    trackingStatus: TrackingStatus,
    expectedArrivalDisplay: String?,
    deliveredAtDisplay: String?,
    countdownLabel: String?,
    modifier: Modifier = Modifier,
) {
    val (title, subtitle) = heroCopy(
        trackingStatus = trackingStatus,
        hasEta = !expectedArrivalDisplay.isNullOrBlank(),
        hasDeliveredAt = !deliveredAtDisplay.isNullOrBlank(),
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f),
            )

            if (trackingStatus == TrackingStatus.DELIVERING && !expectedArrivalDisplay.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Estimated arrival",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f),
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = expectedArrivalDisplay,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                )
                AnimatedVisibility(
                    visible = !countdownLabel.isNullOrBlank(),
                    enter = fadeIn() + slideInVertically { it / 2 },
                    exit = fadeOut(),
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = countdownLabel.orEmpty(),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }

            if (trackingStatus == TrackingStatus.DELIVERED && !deliveredAtDisplay.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Arrived at",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f),
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = deliveredAtDisplay,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

private fun heroCopy(
    trackingStatus: TrackingStatus,
    hasEta: Boolean,
    hasDeliveredAt: Boolean,
): Pair<String, String> {
    return when (trackingStatus) {
        TrackingStatus.PENDING -> "Order received" to "Waiting for the restaurant to accept your order"
        TrackingStatus.PREPARING -> "Preparing your food" to "The restaurant is cooking your order"
        TrackingStatus.DELIVERING -> {
            if (hasEta) {
                "On the way" to "Your order is out for delivery"
            } else {
                "On the way" to "Your order is out for delivery"
            }
        }
        TrackingStatus.DELIVERED -> {
            if (hasDeliveredAt) {
                "Delivered" to "Your order has been delivered"
            } else {
                "Delivered" to "Your order has been delivered"
            }
        }
        TrackingStatus.CONFIRMED -> "Order completed" to "Thank you for ordering with us"
        TrackingStatus.CANCELLED -> "Order cancelled" to "This order has been cancelled"
    }
}

@Preview(showBackground = true)
@Composable
private fun OrderStatusHeroCardDeliveringPreview() {
    DFoodTheme(darkTheme = false) {
        OrderStatusHeroCard(
            trackingStatus = TrackingStatus.DELIVERING,
            expectedArrivalDisplay = "28 Jun, 12:45",
            deliveredAtDisplay = null,
            countdownLabel = "~8 min left",
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun OrderStatusHeroCardPreparingPreview() {
    DFoodTheme(darkTheme = false) {
        OrderStatusHeroCard(
            trackingStatus = TrackingStatus.PREPARING,
            expectedArrivalDisplay = null,
            deliveredAtDisplay = null,
            countdownLabel = null,
        )
    }
}
