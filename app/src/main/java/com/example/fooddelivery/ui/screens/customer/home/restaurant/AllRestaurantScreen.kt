package com.example.fooddelivery.ui.screens.customer.home.restaurant

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fooddelivery.R
import com.example.fooddelivery.domain.model.Category
import com.example.fooddelivery.domain.model.RestaurantMinRatingFilter
import com.example.fooddelivery.domain.model.RestaurantSortOption
import com.example.fooddelivery.ui.components.topbar.DFoodTopBar
import com.example.fooddelivery.ui.screens.customer.home.components.RestaurantItem
import com.example.fooddelivery.ui.screens.customer.home.components.RestaurantItemSkeleton
import com.example.fooddelivery.ui.theme.CustomerDimens

@Composable
fun AllRestaurantScreen(
    onNavigateBack: () -> Unit,
    onNavigateToRestaurantDetail: (String) -> Unit,
    viewModel: AllRestaurantsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.onEvent(AllRestaurantsEvent.ErrorDismissed)
        }
    }

    AllRestaurantsContent(
        state = state,
        onEvent = viewModel::onEvent,
        onNavigateBack = onNavigateBack,
        onNavigateToRestaurantDetail = onNavigateToRestaurantDetail,
        snackbarHostState = snackbarHostState,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllRestaurantsContent(
    state: AllRestaurantsState,
    onEvent: (AllRestaurantsEvent) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToRestaurantDetail: (String) -> Unit,
    snackbarHostState: SnackbarHostState,
) {
    Scaffold(
        topBar = {
            DFoodTopBar(
                title = stringResource(R.string.all_restaurants_title),
                onBackClick = onNavigateBack,
                scrollBehavior = null,
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            OutlinedTextField(
                value = state.keywordInput,
                onValueChange = { onEvent(AllRestaurantsEvent.KeywordChanged(it)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = CustomerDimens.screenHorizontalPadding, vertical = 8.dp),
                placeholder = { Text(stringResource(R.string.all_restaurants_search_hint)) },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null)
                },
                singleLine = true,
                shape = MaterialTheme.shapes.medium,
            )

            RestaurantQuickFilters(
                currentSort = state.currentSortOption,
                minRatingFilter = state.minRatingFilter,
                hasLocation = state.hasLocation,
                activeFilterCount = state.activeFilterCount,
                onSortSelected = { onEvent(AllRestaurantsEvent.SortChanged(it)) },
                onMinRatingSelected = { onEvent(AllRestaurantsEvent.MinRatingChanged(it)) },
                onOpenFilterSheet = { onEvent(AllRestaurantsEvent.ToggleFilterSheet(true)) },
            )

            if (state.categories.isNotEmpty()) {
                RestaurantCategoryFilters(
                    categories = state.categories,
                    selectedCategoryId = state.selectedCategoryId,
                    onCategorySelected = { onEvent(AllRestaurantsEvent.CategorySelected(it)) },
                )
            }

            if (state.locationLabel.isNotEmpty()) {
                Text(
                    text = stringResource(R.string.all_restaurants_near, state.locationLabel),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(
                        horizontal = CustomerDimens.screenHorizontalPadding,
                        vertical = 4.dp,
                    ),
                )
            }

            PullToRefreshBox(
                isRefreshing = state.isRefreshing,
                onRefresh = { onEvent(AllRestaurantsEvent.Refresh) },
                modifier = Modifier.weight(1f),
            ) {
                when {
                    state.isLoading && state.restaurants.isEmpty() -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 24.dp),
                            userScrollEnabled = false,
                        ) {
                            items(4) { RestaurantItemSkeleton() }
                        }
                    }

                    state.restaurants.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = stringResource(R.string.all_restaurants_empty),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(32.dp),
                            )
                        }
                    }

                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 24.dp),
                        ) {
                            items(
                                count = state.restaurants.size,
                                key = { index -> state.restaurants[index].id },
                            ) { index ->
                                val restaurant = state.restaurants[index]

                                if (index >= state.restaurants.size - 1 &&
                                    !state.isEndReached &&
                                    !state.isPaginating
                                ) {
                                    LaunchedEffect(state.restaurants.size, state.currentSortOption) {
                                        onEvent(AllRestaurantsEvent.LoadMore)
                                    }
                                }

                                RestaurantItem(
                                    restaurant = restaurant,
                                    onClick = { onNavigateToRestaurantDetail(restaurant.id) },
                                    onFavoriteClick = {
                                        onEvent(AllRestaurantsEvent.ToggleFavorite(restaurant.id))
                                    },
                                )
                            }

                            if (state.isPaginating) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(32.dp),
                                            color = MaterialTheme.colorScheme.primary,
                                        )
                                    }
                                }
                            }

                            if (state.isEndReached) {
                                item {
                                    Text(
                                        text = stringResource(R.string.all_restaurants_end_of_list),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(32.dp),
                                        textAlign = TextAlign.Center,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (state.showFilterSheet) {
            RestaurantFilterBottomSheet(
                currentSort = state.currentSortOption,
                currentMinRating = state.minRatingFilter,
                hasLocation = state.hasLocation,
                onApply = { sort, minRating ->
                    onEvent(AllRestaurantsEvent.ApplyFilters(sort, minRating))
                },
                onDismiss = { onEvent(AllRestaurantsEvent.ToggleFilterSheet(false)) },
            )
        }
    }
}

@Composable
private fun RestaurantQuickFilters(
    currentSort: RestaurantSortOption,
    minRatingFilter: RestaurantMinRatingFilter,
    hasLocation: Boolean,
    activeFilterCount: Int,
    onSortSelected: (RestaurantSortOption) -> Unit,
    onMinRatingSelected: (RestaurantMinRatingFilter) -> Unit,
    onOpenFilterSheet: () -> Unit,
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = CustomerDimens.screenHorizontalPadding),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            BadgedBox(
                badge = {
                    if (activeFilterCount > 0) {
                        Badge { Text(activeFilterCount.toString()) }
                    }
                },
            ) {
                FilterChip(
                    selected = false,
                    onClick = onOpenFilterSheet,
                    label = { Text(stringResource(R.string.all_restaurants_filters)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                    },
                )
            }
        }

        item {
            SortFilterChip(
                label = RestaurantSortOption.NEWEST.title,
                selected = currentSort == RestaurantSortOption.NEWEST,
                onClick = { onSortSelected(RestaurantSortOption.NEWEST) },
            )
        }

        item {
            SortFilterChip(
                label = RestaurantSortOption.DISTANCE.title,
                selected = currentSort == RestaurantSortOption.DISTANCE,
                enabled = hasLocation,
                onClick = { onSortSelected(RestaurantSortOption.DISTANCE) },
            )
        }

        item {
            SortFilterChip(
                label = RestaurantSortOption.RATING.title,
                selected = currentSort == RestaurantSortOption.RATING,
                onClick = { onSortSelected(RestaurantSortOption.RATING) },
            )
        }

        item {
            SortFilterChip(
                label = RestaurantMinRatingFilter.FOUR.title,
                selected = minRatingFilter == RestaurantMinRatingFilter.FOUR,
                onClick = {
                    val next = if (minRatingFilter == RestaurantMinRatingFilter.FOUR) {
                        RestaurantMinRatingFilter.ANY
                    } else {
                        RestaurantMinRatingFilter.FOUR
                    }
                    onMinRatingSelected(next)
                },
            )
        }
    }
}

@Composable
private fun RestaurantCategoryFilters(
    categories: List<Category>,
    selectedCategoryId: String?,
    onCategorySelected: (String?) -> Unit,
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 4.dp),
        contentPadding = PaddingValues(horizontal = CustomerDimens.screenHorizontalPadding),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(categories, key = { it.id }) { category ->
            val isSelected = selectedCategoryId == category.id
            FilterChip(
                selected = isSelected,
                onClick = { onCategorySelected(category.id) },
                label = { Text(category.name) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.secondary,
                    selectedLabelColor = MaterialTheme.colorScheme.onSecondary,
                ),
            )
        }
    }
}

@Composable
private fun SortFilterChip(
    label: String,
    selected: Boolean,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        enabled = enabled,
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                ),
            )
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
    )
}
