package com.example.fooddelivery.ui.screens.chat.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SuggestedReplies(
    onReplyClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val suggestions = listOf("Thank you!", "How long will it take?", "Okay, I'm waiting")

    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                start = 48.dp,
                top = 12.dp,
                bottom = 12.dp
            ),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(end = 16.dp)
    ) {
        items(suggestions) { reply ->
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = MaterialTheme.shapes.medium,
                border = BorderStroke(
                    width = 1.dp, 
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                ),
                onClick = { onReplyClick(reply) }
            ) {
                Text(
                    text = reply,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelLarge.copy(fontSize = 13.sp)
                )
            }
        }
    }
}

@Composable
fun ChatDateDivider(
    date: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(1.dp)
                .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
        )
        
        Surface(
            color = MaterialTheme.colorScheme.background,
            modifier = Modifier.padding(horizontal = 12.dp)
        ) {
            Text(
                text = date,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
}
