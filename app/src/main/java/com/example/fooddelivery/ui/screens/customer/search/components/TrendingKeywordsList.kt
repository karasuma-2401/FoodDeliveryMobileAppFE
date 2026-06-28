package com.example.fooddelivery.ui.screens.customer.search.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.fooddelivery.R
import com.example.fooddelivery.domain.model.TrendingKeyword
import com.example.fooddelivery.ui.components.bounceClick

@Composable
fun TrendingKeywordsList(
    keywords: List<TrendingKeyword>,
    onKeywordClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (keywords.isEmpty()) return

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.trending_keywords),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
        )
        LazyRow(
            modifier = Modifier.padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(keywords, key = { it.keyword }) { item ->
                Text(
                    text = item.keyword,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f))
                        .bounceClick { onKeywordClick(item.keyword) }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }
        }
    }
}
