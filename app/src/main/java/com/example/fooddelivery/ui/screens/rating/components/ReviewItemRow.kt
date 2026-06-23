package com.example.fooddelivery.ui.screens.rating.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.fooddelivery.domain.model.ReviewItem

@Composable
fun ReviewItemRow(
    review: ReviewItem,
    onMoreClick: () -> Unit // Tiếp tục nhận callback tại đây
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        // User Avatar Placholder
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
        ) {
        }

        Spacer(modifier = Modifier.width(12.dp))

        ReviewContentCard(
            review = review,
            onMoreClick = onMoreClick,
            modifier = Modifier.weight(1f)
        )
    }
}