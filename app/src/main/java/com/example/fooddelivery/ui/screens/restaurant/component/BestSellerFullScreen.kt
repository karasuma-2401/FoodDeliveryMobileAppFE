package com.example.fooddelivery.ui.screens.restaurant.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

import com.example.fooddelivery.domain.model.BestSellerItem
import androidx.compose.foundation.lazy.items

@Composable
fun BestSellerFullScreen(items: List<BestSellerItem>) {

    val sortedItems = items.sortedByDescending { it.soldCount }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text(
            "All Popular Items",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(sortedItems) { item ->
                BestSellerItemCard(item)
            }
        }
    }
}