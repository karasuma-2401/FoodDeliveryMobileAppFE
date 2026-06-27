package com.example.fooddelivery.ui.screens.food.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fooddelivery.domain.util.DiscountBadgeVisual
import com.example.fooddelivery.ui.components.ShopeeDiscountBadge
import com.example.fooddelivery.ui.components.bounceClick
import com.example.fooddelivery.ui.components.cart.onCenterPositioned
import com.example.fooddelivery.ui.theme.CustomerDimens
import java.util.Locale
import kotlinx.coroutines.delay

@Composable
fun FoodInfoSection(
    name: String,
    description: String,
    unitPrice: Double,
    soldCount: Int,
    originalPrice: Double?,
    discountBadge: DiscountBadgeVisual?,
    showAddSuccessPulse: Boolean,
    isAddingToCart: Boolean,
    onQuickAdd: (Offset) -> Unit,
    onClearAddSuccessPulse: () -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(showAddSuccessPulse) {
        if (showAddSuccessPulse) {
            delay(350)
            onClearAddSuccessPulse()
        }
    }

    val pulseScale by animateFloatAsState(
        targetValue = if (showAddSuccessPulse) 1.15f else 1f,
        label = "add_pulse"
    )
    var addButtonCenter by remember { mutableStateOf<Offset?>(null) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        if (discountBadge != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                ShopeeDiscountBadge(
                    visual = discountBadge,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        } else {
            Text(
                text = name,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                ),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.fillMaxWidth(),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (description.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 22.sp
            )
        }

        if (soldCount > 0) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Sold $soldCount",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = formatUsdPrice(unitPrice),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
                val strikePrice = originalPrice?.takeIf { it > unitPrice }
                if (strikePrice != null) {
                    Text(
                        text = formatUsdPrice(strikePrice),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            textDecoration = TextDecoration.LineThrough
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(CustomerDimens.iconContainerSm)
                    .onCenterPositioned { addButtonCenter = it }
                    .graphicsLayer {
                        scaleX = pulseScale
                        scaleY = pulseScale
                    }
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primary)
                    .bounceClick(
                        enabled = !isAddingToCart,
                        enableHaptic = true,
                        hapticFeedbackType = HapticFeedbackType.LongPress,
                        onClick = {
                            addButtonCenter?.let(onQuickAdd)
                        }
                    )
            ) {
                Icon(
                    imageVector = if (showAddSuccessPulse) Icons.Default.Check else Icons.Default.Add,
                    contentDescription = "Add to cart",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(CustomerDimens.iconMd)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        Spacer(modifier = Modifier.height(16.dp))
    }
}

private fun formatUsdPrice(amount: Double): String {
    return "$${String.format(Locale.US, "%.0f", amount)}"
}
