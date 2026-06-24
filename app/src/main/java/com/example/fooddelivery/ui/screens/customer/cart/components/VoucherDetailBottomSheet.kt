package com.example.fooddelivery.ui.screens.customer.cart.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fooddelivery.domain.model.Voucher
import com.example.fooddelivery.domain.model.VoucherType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoucherDetailBottomSheet(
    voucher: Voucher,
    onDismissRequest: () -> Unit,
    onApplyVoucher: (Voucher) -> Unit,
    showApplyButton: Boolean = true
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        dragHandle = null,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Voucher Details",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                IconButton(
                    onClick = onDismissRequest,
                    modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                ) {
                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(20.dp))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(
                            if (voucher.type == VoucherType.PERCENT) MaterialTheme.colorScheme.primary 
                            else MaterialTheme.colorScheme.secondary,
                            RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (voucher.type == VoucherType.PERCENT) Icons.Default.Percent else Icons.Default.LocalShipping,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = voucher.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Code: ${voucher.code}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Divider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(24.dp))

            DetailSection(
                icon = Icons.Default.Info,
                title = "Description",
                content = voucher.description
            )

            if (voucher.restaurantName != null) {
                Spacer(modifier = Modifier.height(16.dp))
                DetailSection(
                    icon = Icons.Default.Store,
                    title = "Applied for",
                    content = voucher.restaurantName
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            DetailSection(
                icon = Icons.Default.Percent,
                title = "Condition",
                content = buildString {
                    append("Min. Order: $${voucher.minOrderAmount}")
                    if (voucher.maxDiscountAmount != null) {
                        append("\nMax. Discount: $${voucher.maxDiscountAmount}")
                    }
                }
            )

            if (voucher.expiryText != null) {
                Spacer(modifier = Modifier.height(16.dp))
                DetailSection(
                    icon = Icons.Default.Close,
                    title = "Expiry",
                    content = "Ends on: ${voucher.expiryText}"
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            if (showApplyButton) {
                Button(
                    onClick = {
                        onApplyVoucher(voucher)
                        onDismissRequest()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    enabled = voucher.isApplicable
                ) {
                    Text("Apply this Voucher", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun DetailSection(icon: ImageVector, title: String, content: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
