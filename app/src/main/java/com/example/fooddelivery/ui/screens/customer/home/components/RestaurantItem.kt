package com.example.fooddelivery.ui.screens.customer.home.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
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
import coil.compose.AsyncImage
import com.example.fooddelivery.R
import com.example.fooddelivery.domain.model.Restaurant
import com.example.fooddelivery.ui.components.VoucherCornerBadge
import com.example.fooddelivery.ui.components.bounceClick

@Composable
fun RestaurantItem(
    restaurant: Restaurant,
    onClick: () -> Unit,
    onFavoriteClick: (() -> Unit)? = null,
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
                        .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(id = R.drawable.food_bowl),
                    error = painterResource(id = R.drawable.food_bowl)
                )

                restaurant.voucherBadgeLabel?.let { label ->
                    VoucherCornerBadge(
                        label = label,
                        modifier = Modifier.align(Alignment.TopEnd)
                    )
                }

                onFavoriteClick?.let { onFavorite ->
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(12.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                    ) {
                        IconButton(
                            onClick = onFavorite,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = if (restaurant.isLiked) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = "Favorite",
                                tint = if (restaurant.isLiked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(20.dp)
                            )
                        }
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
                val ratingText = buildString {
                    append(if (restaurant.rating > 0) String.format("%.1f", restaurant.rating) else "New")
                    if (restaurant.reviewCount > 0) {
                        append(" (${restaurant.reviewCount})")
                    }
                }
                InfoItem(
                    icon = Icons.Default.Star,
                    text = ratingText,
                    color = MaterialTheme.colorScheme.primary
                )

                restaurant.distance?.let {
                    InfoItem(
                        icon = Icons.Default.LocationOn,
                        text = String.format("%.1f km", it),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                restaurant.estimatedDeliveryTime?.let {
                    InfoItem(
                        icon = Icons.Default.AccessTime,
                        text = "$it min",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoItem(icon: ImageVector, text: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
    }
}
