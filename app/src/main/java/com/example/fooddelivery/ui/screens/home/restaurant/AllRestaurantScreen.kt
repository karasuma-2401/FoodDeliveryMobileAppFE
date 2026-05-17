package com.example.fooddelivery.ui.screens.home.restaurant

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fooddelivery.ui.components.topbar.DFoodTopBar
import com.example.fooddelivery.ui.screens.home.components.RestaurantItem

@Composable
fun AllRestaurantScreen(
    onNavigateBack: () -> Unit,
    onNavigateToRestaurantDetail: (String) -> Unit,
    viewModel: AllRestaurantsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    AllRestaurantsContent(
        state = state,
        onEvent = viewModel::onEvent,
        onNavigateBack = onNavigateBack,
        onNavigateToRestaurantDetail = onNavigateToRestaurantDetail,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllRestaurantsContent(
    state: AllRestaurantsState,
    onEvent: (AllRestaurantsEvent) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToRestaurantDetail: (String) -> Unit,
) {
    Scaffold(
        topBar = {
            DFoodTopBar(
                title = "All Restaurants",
                onBackClick = onNavigateBack,
                scrollBehavior = null
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        if(state.isLoading && state.restaurants.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            Column(modifier = Modifier.padding(innerPadding)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    InputChip(
                        selected = true,
                        onClick = { onEvent.invoke(AllRestaurantsEvent.ToggleSortSheet(true)) },
                        label = { 
                            Text(
                                text = " ${state.currentSortOption.title}",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                            ) 
                        },
                        trailingIcon = { 
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown, 
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            ) 
                        },
                        colors = InputChipDefaults.inputChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            selectedTrailingIconColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        border = InputChipDefaults.inputChipBorder(
                            enabled = true,
                            selected = true,
                            borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        )
                    )
                }
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(
                        count = state.restaurants.size,
                        key = { index -> state.restaurants[index].id }
                    ) { index ->
                        val restaurant = state.restaurants[index]

                        if (index >= state.restaurants.size - 1 && !state.isEndReached && !state.isPaginating) {
                            LaunchedEffect(key1 = Unit) {
                                onEvent.invoke(AllRestaurantsEvent.LoadMore)
                            }
                        }
                        RestaurantItem(
                            restaurant = restaurant,
                            onClick = { onNavigateToRestaurantDetail(restaurant.id) }
                        )
                    }
                    if (state.isPaginating) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(32.dp),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                    if (state.isEndReached && state.restaurants.isNotEmpty()) {
                        item {
                            Text(
                                text = "You've reached the end of the list!",
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
            if (state.showSortSheet) {
                SortBottomSheet(
                    currentOption = state.currentSortOption,
                    onOptionSelected = { onEvent.invoke(AllRestaurantsEvent.SortChanged(it)) },
                    onDismiss = { onEvent.invoke(AllRestaurantsEvent.ToggleSortSheet(false)) }
                )
            }
        }
    }
}
