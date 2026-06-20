package com.example.fooddelivery.ui.screens.home.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.DirectionsRun
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.fooddelivery.R
import com.example.fooddelivery.domain.model.Restaurant
import com.example.fooddelivery.ui.components.bounceClick

@Composable
fun RestaurantItem(
    restaurant: Restaurant,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp)
            .bounceClick { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp, pressedElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(bottom = 12.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                AsyncImage(
                    model = restaurant.imageUrl,
                    contentDescription = restaurant.name,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(24.dp)),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(id = R.drawable.food_bowl),
                    error = painterResource(id = R.drawable.food_bowl)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    restaurant.promoTags.firstOrNull { !it.contains("Freeship", ignoreCase = true) }?.let { promo ->
                        val isPromo = promo.contains("PROMO", ignoreCase = true)
                        BadgeContainer(
                            text = promo,
                            containerColor = if (isPromo) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                            contentColor = if (isPromo) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onError
                        )
                    }

                    if (restaurant.deliveryFee == 0.0) {
                        BadgeContainer(
                            text = "Free Delivery",
                            containerColor = MaterialTheme.colorScheme.tertiary,
                            contentColor = MaterialTheme.colorScheme.onTertiary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = restaurant.name,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            if (restaurant.tags.isNotEmpty()) {
                Text(
                    text = restaurant.tags.joinToString(" • "),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                InfoItem(
                    icon = Icons.Default.Star, 
                    text = if (restaurant.rating > 0) String.format("%.1f", restaurant.rating) else "New", 
                    color = MaterialTheme.colorScheme.primary
                )
                val deliveryFeeText = if (restaurant.deliveryFee == 0.0) "Free" else "$${String.format("%.2f", restaurant.deliveryFee)}"
                InfoItem(
                    icon = Icons.Outlined.DirectionsRun, 
                    text = deliveryFeeText, 
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun BadgeContainer(text: String, containerColor: Color, contentColor: Color) {
    Surface(
        color = containerColor,
        contentColor = contentColor,
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 2.dp
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
        )
    }
}

@Composable
private fun InfoItem(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
    }
}