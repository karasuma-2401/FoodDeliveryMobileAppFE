package com.example.fooddelivery.ui.screens.admin.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun RestaurantItemRow(
    name: String,
    phone: String,
    status: String,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    onClick: () -> Unit = {}
) {
    var showMenu by remember { mutableStateOf(false) }
    val isApproved = status.uppercase() == "APPROVED"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(name.take(1), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                Text(text = name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                Text(text = phone, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                val statusText = if (isApproved) "Approved" else status.lowercase().replaceFirstChar { it.uppercase() }
                val statusColor = if (isApproved) Color(0xFF43A047) else Color(0xFFE53935)
                Text(text = statusText, color = statusColor, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            }

            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = null)
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Approve Restaurant", color = Color(0xFF43A047)) },
                        onClick = { showMenu = false; onApprove() },
                        leadingIcon = { Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF43A047)) }
                    )
                    DropdownMenuItem(
                        text = { Text("Reject / Cancel", color = MaterialTheme.colorScheme.error) },
                        onClick = { showMenu = false; onReject() },
                        leadingIcon = { Icon(Icons.Default.Close, contentDescription = null, tint = MaterialTheme.colorScheme.error) }
                    )
                }
            }
        }
    }
}