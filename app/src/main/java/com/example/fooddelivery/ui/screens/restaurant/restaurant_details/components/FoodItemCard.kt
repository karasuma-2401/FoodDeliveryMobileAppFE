package com.example.fooddelivery.ui.screens.restaurant.restaurant_details.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.fooddelivery.domain.model.FoodItem
import com.example.fooddelivery.ui.components.cart.onCenterPositioned
import com.example.fooddelivery.ui.theme.CustomerDimens

@Composable
fun FoodItemCard(
    foodItem: FoodItem,
    onAddClick: (imageCenter: Offset) -> Unit,
    onItemClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var imageCenter by remember { mutableStateOf<Offset?>(null) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(CustomerDimens.cardCornerRadius))
            .background(MaterialTheme.colorScheme.surface)
            .clickable { onItemClick() }
            .padding(CustomerDimens.cardPadding)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(CustomerDimens.foodGridImageHeight)
            ) {
                AsyncImage(
                    model = foodItem.imageUrl ?: foodItem.imageRes,
                    contentDescription = foodItem.name,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .onCenterPositioned { imageCenter = it },
                    contentScale = ContentScale.Crop
                )
                foodItem.promoTag?.let { tag ->
                    val badgeColor = when {
                        tag.contains("HOT", true) -> MaterialTheme.colorScheme.error
                        tag.contains("FREESHIP", true) -> MaterialTheme.colorScheme.tertiary
                        else -> MaterialTheme.colorScheme.secondary
                    }
                    val onBadgeColor = when {
                        tag.contains("HOT", true) -> MaterialTheme.colorScheme.onError
                        tag.contains("FREESHIP", true) -> MaterialTheme.colorScheme.onTertiary
                        else -> MaterialTheme.colorScheme.onSecondary
                    }
                    Box(
                        modifier = Modifier
                            .padding(8.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(badgeColor)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = tag,
                            color = onBadgeColor,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = foodItem.name,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
            Text(
                text = foodItem.restaurantName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "$${foodItem.price}",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                color = MaterialTheme.colorScheme.primary
            )
        }
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
        ) {
            IconButton(
                onClick = {
                    imageCenter?.let(onAddClick)
                },
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add ${foodItem.name} to cart",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(CustomerDimens.iconMd)
                )
            }
        }
    }
}
