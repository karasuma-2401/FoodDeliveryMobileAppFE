package com.example.fooddelivery.ui.screens.restaurant.component.coupon

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fooddelivery.domain.model.Voucher
import com.example.fooddelivery.domain.model.VoucherType

@Composable
fun CouponItemCard(
    voucher: Voucher,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (voucher.type == VoucherType.FREE_SHIPPING) "🚚" else "🎟️",
                    fontSize = 24.sp
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = voucher.code,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    val badgeBgColor = if (voucher.isApplicable) Color(0xFFD2E4FF) else Color(0xFFFFDADA)
                    val badgeTextColor = if (voucher.isApplicable) Color(0xFF004BA0) else Color(0xFFBA1A1A)
                    val badgeText = if (voucher.isApplicable) "ACTIVE" else "INACTIVE"

                    Surface(
                        color = badgeBgColor,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = badgeText,
                            color = badgeTextColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = voucher.title,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${voucher.description} • Min. Order: $${voucher.minOrderAmount}",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )

                if (!voucher.expiryText.isNullOrEmpty() || !voucher.conditionMessage.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = voucher.conditionMessage ?: "Expires: ${voucher.expiryText}",
                        fontSize = 12.sp,
                        color = if (voucher.isApplicable) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f) else MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Normal
                    )
                }
            }
        }
    }
}