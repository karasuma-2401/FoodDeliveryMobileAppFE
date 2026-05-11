package com.example.fooddelivery.ui.screens.home.restaurant_detail.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.DirectionsRun
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fooddelivery.domain.model.Restaurant

@Composable
fun RestaurantHeader(restaurant: Restaurant) {
    Column(modifier = Modifier.fillMaxWidth()) {
        restaurant.imageRes?.let {
            Image(
                painter = painterResource(id = it),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .clip(RoundedCornerShape(32.dp)),
                contentScale = ContentScale.Crop
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = restaurant.name,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp
            )
        )
        Text(
            text = restaurant.description,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            modifier = Modifier.padding(top = 8.dp)
        )
        Row (
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            InfoItem(icon = Icons.Default.Star, text = "${restaurant.rating}", iconColor = Color(0xFFFF9800))
            val deliveryFeeText = if (restaurant.deliveryFee == 0.0) "Free" else "$${restaurant.deliveryFee}"
            InfoItem(icon = Icons.Outlined.DirectionsRun, text = deliveryFeeText, iconColor = MaterialTheme.colorScheme.primary)
            InfoItem(icon = Icons.Default.AccessTime, text = restaurant.deliveryTime, iconColor = MaterialTheme.colorScheme.primary)
        }
    }
}
@Composable fun InfoItem(icon: ImageVector, text: String, iconColor: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = text, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
    }
}