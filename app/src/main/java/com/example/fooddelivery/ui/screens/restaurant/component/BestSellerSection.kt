package com.example.fooddelivery.ui.screens.restaurant.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.fooddelivery.domain.model.BestSellerItem


@Composable
fun BestSellerSection(
    items: List<BestSellerItem>,
    onSeeAllClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Popular Items This Week",
                    fontWeight = FontWeight.Bold
                )

                TextButton(onClick = onSeeAllClick) {
                    Text("See All")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(items) { item ->
                    BestSellerItemCard(item)
                }
            }
        }
    }
}