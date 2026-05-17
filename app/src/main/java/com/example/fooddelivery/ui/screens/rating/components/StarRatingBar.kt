package com.example.fooddelivery.ui.screens.rating.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp

@Composable
fun StarRatingBar(
    rating: Int,
    onRRatingChanged: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    // Modern star rating uses secondary color for "active" states
    val starColor = MaterialTheme.colorScheme.secondary
    
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    )  {
        for (i in 1..5) {
            val isChecked = i <= rating
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = "Rate $i star${if (i > 1) "s" else ""}",
                tint = if (isChecked) starColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                modifier = Modifier
                    .size(48.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        role = Role.Button
                    ) {
                        onRRatingChanged(i)
                    }
            )
        }
    }
}
