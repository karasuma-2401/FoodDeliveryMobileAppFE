package com.example.fooddelivery.ui.screens.restaurant.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fooddelivery.ui.screens.restaurant.order.OrderModel
import com.example.fooddelivery.ui.screens.restaurant.order.OrderStatus

@Composable
fun OrderCard(
    order: OrderModel,
    onAccept: () -> Unit,
    onDeny: () -> Unit,
    onDone: () -> Unit,
    onDelivered: () -> Unit,
    onCancel: () -> Unit,
    isUpdating: Boolean = false,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    val arrowRotationDegree by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        label = "ArrowRotationAnimation"
    )

    fun formatVND(amount: Double) = String.format("%,d", amount.toInt()).replace(',', '.') + "đ"

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Order #${order.id}",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = order.orderTime,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = order.customerName,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Phone: ${order.customerPhone}",
                    modifier = Modifier.padding(top = 2.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Total Price: ",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatVND(order.totalPrice),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Toggle Details",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.rotate(arrowRotationDegree)
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    Text(
                        text = "LIST OF FOODS",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    order.items.forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${item.name} x${item.quantity}",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = formatVND(item.lineTotal),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            when (order.status) {
                // Bước 1: Khách vừa đặt — nhà hàng có thể Accept hoặc Deny
                OrderStatus.PENDING -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDeny,
                            enabled = !isUpdating,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Deny", fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = onAccept,
                            enabled = !isUpdating,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Accept", fontWeight = FontWeight.Bold)
                        }
                    }
                }
                // Bước 2: Nhà hàng đang làm — có thể Done (→DELIVERING) hoặc Cancel
                OrderStatus.PREPARING -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onCancel,
                            enabled = !isUpdating,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Cancel", fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = onDone,
                            enabled = !isUpdating,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Text("Done", fontWeight = FontWeight.Bold)
                        }
                    }
                }
                // Bước 3: Đang giao — chỉ có thể xác nhận đã giao (BE không cho cancel)
                OrderStatus.DELIVERING -> {
                    Button(
                        onClick = onDelivered,
                        enabled = !isUpdating,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                    ) {
                        Text("Delivered", fontWeight = FontWeight.Bold)
                    }
                }
                // Bước 4: Đã giao, chờ khách xác nhận
                OrderStatus.DELIVERED -> BadgeStatus(text = "DELIVERED — Awaiting confirmation", color = Color(0xFF4CAF50))
                // Bước 5: Khách/hệ thống đã xác nhận
                OrderStatus.CONFIRMED -> BadgeStatus(text = "CONFIRMED", color = Color(0xFF2196F3))
                // Đã hủy
                OrderStatus.CANCELLED -> BadgeStatus(text = "CANCELLED", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun BadgeStatus(text: String, color: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(color.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, color = color, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
    }
}
