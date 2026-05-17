package com.example.fooddelivery.ui.screens.food.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun BottomCartBar(
    price: String,
    quantity: Int,
    onUpdateQuantity: (Int) -> Unit,
    onAddToCart: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        shadowElevation = 16.dp,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "$$price",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
                Text(
                    text = "TOTAL PRICE",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.sp
                    )
                )
            }
            
            // Quantity Selector
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(32.dp))
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .padding(horizontal = 4.dp, vertical = 4.dp)
            ) {
                IconButton(
                    onClick = { onUpdateQuantity(-1) }, 
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Default.Remove, 
                        contentDescription = null, 
                        tint = MaterialTheme.colorScheme.onSecondaryContainer, 
                        modifier = Modifier.size(16.dp)
                    )
                }
                Text(
                    text = "$quantity",
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(horizontal = 8.dp),
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                )
                IconButton(
                    onClick = { onUpdateQuantity(1) }, 
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Default.Add, 
                        contentDescription = null, 
                        tint = MaterialTheme.colorScheme.onSecondaryContainer, 
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Button(
                onClick = onAddToCart,
                modifier = Modifier.height(56.dp).width(140.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(16.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Text(
                    text = "Add to Cart",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}
