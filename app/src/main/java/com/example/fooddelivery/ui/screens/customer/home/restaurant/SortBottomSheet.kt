package com.example.fooddelivery.ui.screens.customer.home.restaurant

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.fooddelivery.R
import com.example.fooddelivery.domain.model.RestaurantMinRatingFilter
import com.example.fooddelivery.domain.model.RestaurantSortOption

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestaurantFilterBottomSheet(
    currentSort: RestaurantSortOption,
    currentMinRating: RestaurantMinRatingFilter,
    hasLocation: Boolean,
    onApply: (RestaurantSortOption, RestaurantMinRatingFilter) -> Unit,
    onDismiss: () -> Unit,
) {
    var pendingSort by remember(currentSort) { mutableStateOf(currentSort) }
    var pendingMinRating by remember(currentMinRating) { mutableStateOf(currentMinRating) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(),
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp, start = 24.dp, end = 24.dp),
        ) {
            Text(
                text = stringResource(R.string.all_restaurants_filter_title),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(vertical = 16.dp),
            )

            Text(
                text = stringResource(R.string.all_restaurants_sort_by),
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            RestaurantSortOption.entries.forEach { option ->
                val enabled = option != RestaurantSortOption.DISTANCE || hasLocation
                FilterOptionRow(
                    title = option.title,
                    selected = pendingSort == option,
                    enabled = enabled,
                    onClick = { if (enabled) pendingSort = option },
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Text(
                text = stringResource(R.string.all_restaurants_min_rating),
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            RestaurantMinRatingFilter.entries.forEach { filter ->
                FilterOptionRow(
                    title = filter.title,
                    selected = pendingMinRating == filter,
                    enabled = true,
                    onClick = { pendingMinRating = filter },
                )
            }

            Button(
                onClick = { onApply(pendingSort, pendingMinRating) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
            ) {
                Text(text = stringResource(R.string.all_restaurants_apply_filters))
            }
        }
    }
}

@Composable
private fun FilterOptionRow(
    title: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = when {
                !enabled -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                selected -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.onSurface
            },
        )
        RadioButton(
            selected = selected,
            onClick = onClick,
            enabled = enabled,
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun RestaurantFilterBottomSheetPreview() {
    RestaurantFilterBottomSheet(
        currentSort = RestaurantSortOption.RATING,
        currentMinRating = RestaurantMinRatingFilter.FOUR,
        hasLocation = true,
        onApply = { _, _ -> },
        onDismiss = {},
    )
}
