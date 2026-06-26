package com.example.fooddelivery.ui.components.cart

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fooddelivery.ui.components.bounceClick
import com.example.fooddelivery.ui.theme.CustomerDimens

@Composable
fun CartIconWithBadge(
    itemCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    badgeColor: Color = MaterialTheme.colorScheme.primary,
    badgeContentColor: Color = MaterialTheme.colorScheme.onPrimary,
    badgeBorderColor: Color = MaterialTheme.colorScheme.surface
) {
    Box(modifier = modifier.size(CustomerDimens.cartBarIconSize)) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .bounceClick(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.ShoppingCart,
                contentDescription = "View cart",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(CustomerDimens.iconSm)
            )
        }
        if (itemCount > 0) {
            CartCountBadge(
                count = itemCount,
                color = badgeColor,
                contentColor = badgeContentColor,
                borderColor = badgeBorderColor
            )
        }
    }
}

@Composable
private fun BoxScope.CartCountBadge(
    count: Int,
    color: Color,
    contentColor: Color,
    borderColor: Color
) {
    Box(
        modifier = Modifier
            .align(Alignment.TopEnd)
            .offset(x = 4.dp, y = (-4).dp)
            .defaultMinSize(minWidth = 20.dp, minHeight = 20.dp)
            .background(color, CircleShape)
            .border(2.dp, borderColor, CircleShape)
            .padding(horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (count > 99) "99+" else count.toString(),
            style = MaterialTheme.typography.labelSmall.copy(
                color = contentColor,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
            )
        )
    }
}
