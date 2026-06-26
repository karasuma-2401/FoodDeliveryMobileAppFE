package com.example.fooddelivery.ui.screens.restaurant.restaurant_details.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fooddelivery.domain.model.Voucher
import com.example.fooddelivery.domain.util.toTicketLabels
import com.example.fooddelivery.ui.components.TicketVoucherBackground
import com.example.fooddelivery.ui.components.TicketVoucherTag
import com.example.fooddelivery.ui.components.TicketVoucherTeal

@Composable
fun RestaurantVoucherSection(
    vouchers: List<Voucher>,
    onViewAllClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (vouchers.isEmpty()) return

    Column(modifier = modifier.fillMaxWidth()) {
        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onViewAllClick),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Percent,
                contentDescription = null,
                tint = Color(0xFFEE4D2D),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Vouchers for you",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "See more",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(16.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(end = 16.dp)
        ) {
            items(vouchers, key = { it.id }) { voucher ->
                TicketVoucherTag(voucher = voucher)
            }
        }
    }
}

@Composable
fun RestaurantVoucherSheetCard(
    voucher: Voucher,
    modifier: Modifier = Modifier
) {
    val (discountLine, minOrderLine) = voucher.toTicketLabels()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = TicketVoucherTeal.copy(alpha = 0.35f),
                shape = RoundedCornerShape(12.dp)
            )
            .background(TicketVoucherBackground, RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 12.dp)
        ) {
            Text(
                text = discountLine,
                color = TicketVoucherTeal,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = minOrderLine,
                style = MaterialTheme.typography.bodySmall,
                color = TicketVoucherTeal.copy(alpha = 0.8f),
                modifier = Modifier.padding(top = 4.dp)
            )
            if (voucher.description.isNotBlank()) {
                Text(
                    text = voucher.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
            voucher.expiryText?.let { expiry ->
                Text(
                    text = "Expires: $expiry",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFFEE4D2D),
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }
        Text(
            text = voucher.code,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = TicketVoucherTeal
        )
    }
}
