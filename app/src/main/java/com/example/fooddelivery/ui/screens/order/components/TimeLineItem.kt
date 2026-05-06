package com.example.fooddelivery.ui.screens.order.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun TimeLineItem(
    title: String,
    subTitle: String,
    icon: ImageVector,
    isCompleted: Boolean,
    isActive: Boolean,
    isLast: Boolean,
    modifier: Modifier = Modifier
) {
    val iconColor = if (isActive || isCompleted) Color.White else Color(0xFFD3D1D8)
    val circleColor = if (isActive || isCompleted) MaterialTheme.colorScheme.primary else Color.Transparent
    val borderColor = if (isActive || isCompleted) MaterialTheme.colorScheme.primary else Color(0xFFE8E7E5)
    val titleColor = if (isActive) MaterialTheme.colorScheme.primary else if (isCompleted) MaterialTheme.colorScheme.onBackground else Color(0xFFD3D1D8)

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(50.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(circleColor, CircleShape)
                    .then(
                        if (circleColor == Color.Transparent)
                            Modifier.background(Color.White, CircleShape)
                        else Modifier
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isCompleted && !isActive) Icons.Default.Check else icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(40.dp)
                        .background(if (isCompleted) MaterialTheme.colorScheme.primary else Color(0xFFE8E7E5))
                )
            }
        }
    }
}