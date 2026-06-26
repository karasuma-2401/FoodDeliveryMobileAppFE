package com.example.fooddelivery.ui.screens.customer.cart.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.fooddelivery.R
import com.example.fooddelivery.domain.model.CartItem
import com.example.fooddelivery.ui.components.cart.CompactQuantityStepper
import com.example.fooddelivery.ui.theme.CustomerDimens
import java.util.Locale

@Composable
fun CartItemCard(
    item: CartItem,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(CustomerDimens.cardCornerRadius),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .padding(CustomerDimens.cardPadding)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = item.food.imageUrl,
                contentDescription = item.food.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(CustomerDimens.listThumbnailMd)
                    .clip(RoundedCornerShape(CustomerDimens.cardCornerRadiusSm)),
                placeholder = painterResource(id = R.drawable.food_bowl),
                error = painterResource(id = R.drawable.food_bowl)
            )
            Spacer(modifier = Modifier.width(CustomerDimens.itemSpacing))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.food.name,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        item.food.size?.let { size ->
                            Text(
                                text = "Size: $size",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Text(
                        text = String.format(Locale.US, "$%.2f", item.totalPrice),
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                CompactQuantityStepper(
                    quantity = item.quantity,
                    onDecrease = onDecrease,
                    onIncrease = onIncrease
                )
            }
        }
    }
}
