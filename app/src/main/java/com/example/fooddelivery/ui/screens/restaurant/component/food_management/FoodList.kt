package com.example.fooddelivery.ui.screens.restaurant.component.food_management

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.example.fooddelivery.data.remote.dto.FoodResponse
import com.example.fooddelivery.ui.components.card.FoodItemCard

@Composable
fun FoodList(
    items: List<FoodResponse>,
    onEditClick: (FoodResponse) -> Unit = {},
    onDeleteClick: (FoodResponse) -> Unit = {}
) {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        items(items) { foodItem ->
            FoodItemCard(
                item = foodItem,
                onEditClick = { onEditClick(foodItem) },
                onDeleteClick = { onDeleteClick(foodItem) }
            )
        }
    }
}
