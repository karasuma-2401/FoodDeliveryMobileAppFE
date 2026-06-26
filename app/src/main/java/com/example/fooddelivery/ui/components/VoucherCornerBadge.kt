package com.example.fooddelivery.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private val VoucherBadgeBackground = Color(0xFFFFF27E)
private val VoucherBadgeText = Color(0xFFFF5722)

enum class VoucherBadgeSize {
    Default,
    Compact
}

@Composable
fun VoucherCornerBadge(
    label: String,
    modifier: Modifier = Modifier,
    size: VoucherBadgeSize = VoucherBadgeSize.Default
) {
    val horizontalPadding: Dp
    val verticalPadding: Dp
    val cornerRadius: Dp
    val textStyle = when (size) {
        VoucherBadgeSize.Default -> MaterialTheme.typography.labelMedium
        VoucherBadgeSize.Compact -> MaterialTheme.typography.labelSmall
    }
    when (size) {
        VoucherBadgeSize.Default -> {
            horizontalPadding = 10.dp
            verticalPadding = 5.dp
            cornerRadius = 12.dp
        }
        VoucherBadgeSize.Compact -> {
            horizontalPadding = 6.dp
            verticalPadding = 2.dp
            cornerRadius = 8.dp
        }
    }

    Box(
        modifier = modifier
            .background(
                color = VoucherBadgeBackground,
                shape = RoundedCornerShape(bottomStart = cornerRadius)
            )
            .padding(horizontal = horizontalPadding, vertical = verticalPadding),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = VoucherBadgeText,
            style = textStyle,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
    }
}
