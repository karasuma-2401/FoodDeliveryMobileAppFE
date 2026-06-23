package com.example.fooddelivery.ui.screens.rating_reviews.components

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun StarRatingBar(
    rating: Int,
    onRRatingChanged: (Int) -> Unit,
    modifier: Modifier = Modifier,
    starSize: Dp = 48.dp,
    starSpacing: Dp = 8.dp,
    activeColor: Color = MaterialTheme.colorScheme.primary
) {
    Row(
        modifier = modifier
            .pointerInput(starSize, starSpacing) {
                val starWidthPx = starSize.toPx()
                val spacingPx = starSpacing.toPx()
                val totalStarWidth = starWidthPx + spacingPx
                
                awaitEachGesture {
                    val down = awaitFirstDown()
                    val initialRating = (down.position.x / totalStarWidth).toInt() + 1
                    onRRatingChanged(initialRating.coerceIn(1, 5))
                    
                    while (true) {
                        val event = awaitPointerEvent()
                        val anyPressed = event.changes.any { it.pressed }
                        if (!anyPressed) break
                        
                        event.changes.forEach { change ->
                            val currentRating = (change.position.x / totalStarWidth).toInt() + 1
                            onRRatingChanged(currentRating.coerceIn(1, 5))
                            change.consume()
                        }
                    }
                }
            },
        horizontalArrangement = Arrangement.spacedBy(starSpacing),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 1..5) {
            val isChecked = i <= rating
            Icon(
                imageVector = if (isChecked) Icons.Filled.Star else Icons.Outlined.Star,
                contentDescription = "Rate $i star${if (i > 1) "s" else ""}",
                tint = if (isChecked) activeColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(starSize)
            )
        }
    }
}
