package com.example.fooddelivery.ui.screens.order.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.fooddelivery.domain.model.Order
import com.example.fooddelivery.domain.model.OrderStatus
import com.example.fooddelivery.domain.model.OrderType
import java.util.Locale

@Composable
fun OrderItemCard(
    order: Order,
    onPrimaryAction: () -> Unit,
    onSecondaryAction: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        // Metadata row (Food/Drink + Status)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (order.type == OrderType.FOOD) "Food" else "Drink",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color(0xFF32343E),
                    fontWeight = FontWeight.Medium
                )
            )
            
            if (order.status != OrderStatus.ONGOING) {
                Text(
                    text = order.status.name.lowercase().replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = if (order.status == OrderStatus.COMPLETED) Color(0xFF059C6A) else Color(0xFFE53935),
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = order.restaurantImage,
                contentDescription = order.restaurantName,
                modifier = Modifier
                    .size(70.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF6F6F6)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = order.restaurantName,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF32343E)
                        )
                    )
                    Text(
                        text = "#${order.id}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFFA0A5BA),
                            textDecoration = TextDecoration.Underline
                        )
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "$${String.format(Locale.getDefault(), "%.2f", order.price)}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF32343E)
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "|  ${order.itemCount.toString().padStart(2, '0')} Items",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF646982))
                    )
                    if (order.date != null) {
                        Text(
                            text = "  |  ${order.date}",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF646982))
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (order.status == OrderStatus.ONGOING) {
                Button(
                    onClick = onPrimaryAction,
                    modifier = Modifier.weight(1f).height(45.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF7622))
                ) {
                    Text("Track Order", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                OutlinedButton(
                    onClick = onSecondaryAction,
                    modifier = Modifier.weight(1f).height(45.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF7622))
                ) {
                    Text("Cancel", color = Color(0xFFFF7622), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            } else {
                OutlinedButton(
                    onClick = onSecondaryAction,
                    modifier = Modifier.weight(1f).height(45.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF7622))
                ) {
                    Text("Rate", color = Color(0xFFFF7622), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Button(
                    onClick = onPrimaryAction,
                    modifier = Modifier.weight(1f).height(45.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF7622))
                ) {
                    Text("Re-Order", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = Color(0xFFF0F0F0))
    }
}
