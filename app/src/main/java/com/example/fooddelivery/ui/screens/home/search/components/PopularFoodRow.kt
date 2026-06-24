package com.example.fooddelivery.ui.screens.home.search.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.fooddelivery.domain.model.FoodItem
import com.example.fooddelivery.ui.components.bounceClick
import java.util.Locale

@Composable
fun PopularFoodRow(
    popularFood: List<FoodItem>,
    onFoodItemClick: (String) -> Unit
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(popularFood.size) { index ->
            val food = popularFood[index]
            Card(
                modifier = Modifier
                    .width(160.dp)
                    .bounceClick { onFoodItemClick(food.id) },
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        AsyncImage(
                            model = food.imageUrl ?: food.imageRes,
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(110.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentScale = ContentScale.Crop
                        )
                        if (!food.promoTag.isNullOrBlank()) {
                            Text(
                                text = food.promoTag,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onError,
                                modifier = Modifier
                                    .padding(8.dp)
                                    .background(
                                        color = MaterialTheme.colorScheme.error,
                                        shape = RoundedCornerShape(6.dp)
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = food.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(top = 12.dp),
                        maxLines = 1,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = food.restaurantName,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1
                    )
                    
                    if (food.soldCount > 0) {
                        Text(
                            text = "Sold ${food.soldCount}",
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    Text(
                        text = "$${String.format(Locale.US,"%.2f", food.price)}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
            }
        }
    }
}