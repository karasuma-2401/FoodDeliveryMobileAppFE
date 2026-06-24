package com.example.fooddelivery.ui.screens.customer.search

import android.Manifest
import android.app.Activity
import android.content.IntentSender
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fooddelivery.domain.model.Restaurant
import com.example.fooddelivery.ui.screens.customer.search.components.EmptySearchView
import com.example.fooddelivery.ui.screens.customer.search.components.PopularFoodRow
import com.example.fooddelivery.ui.screens.customer.search.components.RecentKeywordsList
import com.example.fooddelivery.ui.screens.customer.search.components.SearchFilterSection
import com.example.fooddelivery.ui.screens.customer.search.components.SearchInputField
import com.example.fooddelivery.ui.screens.customer.search.components.SearchRestaurantItem
import com.example.fooddelivery.ui.screens.customer.search.components.SearchShimmerLoading
import com.example.fooddelivery.ui.screens.customer.search.components.SectionHeader
import com.example.fooddelivery.ui.screens.customer.home.components.HomeTopBar
import com.example.fooddelivery.ui.theme.DFoodTheme
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*

enum class SearchDisplayState {
    LOADING, SUGGESTIONS, RESULTS, EMPTY
}

@Composable
fun SearchScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToOrders: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToCart: () -> Unit,
    onNavigateToConversations: () -> Unit,
    onNavigateToRestaurant: (Restaurant) -> Unit,
    onNavigateToFoodDetail: (String) -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val gpsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            viewModel.onEvent(SearchEvent.LoadSearchData)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val isGranted = permissions.values.all { it }
        if (isGranted) {
            checkAndRequestGps(context as Activity, gpsLauncher) {
                viewModel.onEvent(SearchEvent.LoadSearchData)
            }
        }
    }
    LaunchedEffect(Unit) {
        val permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        val allGranted = permissions.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }

        if (allGranted) {
            checkAndRequestGps(context as Activity, gpsLauncher) {
                viewModel.onEvent(SearchEvent.LoadSearchData)
            }
        } else {
            permissionLauncher.launch(permissions)
        }
    }

    Scaffold(
        topBar = {
            HomeTopBar(
                selectedLocation = state.selectedLocation,
                availableLocations = state.availableLocations,
                onLocationSelected = { viewModel.onEvent(SearchEvent.LocationSelected(it)) },
                cartItemCount = state.cartItemCount,
                unreadMessageCount = state.unreadMessageCount,
                onCartClick = onNavigateToCart,
                onMessageClick = onNavigateToConversations,
                onBackClick = onNavigateToHome,
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        SearchContent(
            state = state,
            onEvent = viewModel::onEvent,
            onNavigateToRestaurant = onNavigateToRestaurant,
            onNavigateToFoodDetail = onNavigateToFoodDetail,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

private fun checkAndRequestGps(
    activity: Activity,
    gpsLauncher: androidx.activity.result.ActivityResultLauncher<IntentSenderRequest>,
    onAlreadyEnabled: () -> Unit
) {
    val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000).build()
    val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
    val settingsClient = LocationServices.getSettingsClient(activity)
    val task = settingsClient.checkLocationSettings(builder.build())

    task.addOnSuccessListener {
        onAlreadyEnabled()
    }

    task.addOnFailureListener { exception ->
        if (exception is ResolvableApiException) {
            try {
                val intentSenderRequest = IntentSenderRequest.Builder(exception.resolution).build()
                gpsLauncher.launch(intentSenderRequest)
            } catch (sendEx: IntentSender.SendIntentException) {
            }
        }
    }
}

@Composable
fun SearchContent(
    state: SearchState,
    onEvent: (SearchEvent) -> Unit,
    onNavigateToRestaurant: (Restaurant) -> Unit,
    onNavigateToFoodDetail: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberLazyListState()
    val keyboardController = LocalSoftwareKeyboardController.current

    val isScrolling by remember { derivedStateOf { scrollState.isScrollInProgress } }
    LaunchedEffect(isScrolling) {
        if (isScrolling) {
            keyboardController?.hide()
        }
    }

    val displayState = remember(state.isLoading, state.searchQuery, state.suggestedRestaurants, state.popularFood) {
        when {
            state.isLoading -> SearchDisplayState.LOADING
            state.searchQuery.isEmpty() -> SearchDisplayState.SUGGESTIONS
            state.suggestedRestaurants.isEmpty() && state.popularFood.isEmpty() -> SearchDisplayState.EMPTY
            else -> SearchDisplayState.RESULTS
        }
    }

    LazyColumn(
        state = scrollState,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 24.dp)
    ) {
        item {
            SearchInputField(
                query = state.searchQuery,
                onQueryChange = { onEvent(SearchEvent.QueryChanged(it)) },
                onClear = { onEvent(SearchEvent.ClearSearch) },
                onSearch = { onEvent(SearchEvent.PerformSearch) }
            )
        }

        item {
            SearchFilterSection(
                categories = state.categories,
                selectedCategoryId = state.selectedCategoryId,
                selectedSort = state.selectedSort,
                onCategorySelected = { onEvent(SearchEvent.CategorySelected(it)) },
                onSortSelected = { onEvent(SearchEvent.SortSelected(it)) }
            )
        }

        item {
            AnimatedContent(
                targetState = displayState,
                transitionSpec = {
                    fadeIn(animationSpec = tween(400)) togetherWith fadeOut(animationSpec = tween(400))
                },
                label = "search_state_transition"
            ) { target ->
                Column(modifier = Modifier.fillMaxWidth()) {
                    when (target) {
                        SearchDisplayState.LOADING -> {
                            SearchShimmerLoading()
                        }
                        SearchDisplayState.SUGGESTIONS -> {
                            if (state.recentKeyWords.isNotEmpty()) {
                                RecentKeywordsList(
                                    keywords = state.recentKeyWords,
                                    onKeywordClick = { onEvent(SearchEvent.KeywordClicked(it)) },
                                    onDeleteHistoryItem = { onEvent(SearchEvent.DeleteHistoryItem(it)) },
                                    onClearAll = { onEvent(SearchEvent.ClearAllHistory) }
                                )
                            }
                            SectionHeader(
                                title = "Suggested Restaurants",
                                modifier = Modifier.padding(top = 16.dp)
                            )
                            state.suggestedRestaurants.forEach { restaurant ->
                                SearchRestaurantItem(
                                    restaurant = restaurant,
                                    onClick = { onNavigateToRestaurant(restaurant) }
                                )
                            }
                            SectionHeader(
                                title = "Popular Fast Food",
                                modifier = Modifier.padding(top = 24.dp)
                            )
                            PopularFoodRow(
                                popularFood = state.popularFood,
                                onFoodItemClick = onNavigateToFoodDetail
                            )
                        }
                        SearchDisplayState.RESULTS -> {
                            if (state.suggestedRestaurants.isNotEmpty()) {
                                SectionHeader(
                                    "Restaurants Found",
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                                state.suggestedRestaurants.forEach { restaurant ->
                                    SearchRestaurantItem(
                                        restaurant = restaurant,
                                        onClick = { onNavigateToRestaurant(restaurant) }
                                    )
                                }
                            }
                            if (state.popularFood.isNotEmpty()) {
                                SectionHeader(
                                    title = "Dishes Found",
                                    modifier = Modifier.padding(top = 24.dp)
                                )
                                PopularFoodRow(
                                    popularFood = state.popularFood,
                                    onFoodItemClick = onNavigateToFoodDetail
                                )
                            }
                        }
                        SearchDisplayState.EMPTY -> {
                            EmptySearchView(query = state.searchQuery)
                        }
                    }
                }
            }
        }
        item { Spacer(modifier = Modifier.height(40.dp)) }
    }
}
