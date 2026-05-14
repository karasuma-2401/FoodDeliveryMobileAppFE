package com.example.fooddelivery.ui.screens.home.restaurant

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.fooddelivery.domain.model.RestaurantSortOption

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SortBottomSheet(
    currentOption: RestaurantSortOption,
    onOptionSelected: (RestaurantSortOption) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp, start = 24.dp, end = 24.dp)
        ) {
            Text(
                text = "Sort By",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(vertical = 16.dp)
            )
            RestaurantSortOption.entries.forEach { option ->
                val isSelected = option == currentOption
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOptionSelected(option)}
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = option.title,
                        style = MaterialTheme.typography.bodyLarge,
                        color = if(isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    )
                    RadioButton(
                        selected = isSelected,
                        onClick = { onOptionSelected(option)}
                    )
                }
            }
        }
    }
}
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SortBottomSheetPreview() {
    SortBottomSheet(
        currentOption = RestaurantSortOption.DELIVERY_FEE,
        onOptionSelected = {},
        onDismiss = {}
    )
}