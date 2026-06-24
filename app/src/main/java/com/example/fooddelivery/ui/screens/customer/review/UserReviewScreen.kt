package com.example.fooddelivery.ui.screens.customer.review

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fooddelivery.domain.model.UserReview
import com.example.fooddelivery.ui.components.topbar.DFoodTopBar
import com.example.fooddelivery.ui.screens.customer.review.components.UserReviewItem
import com.example.fooddelivery.ui.theme.DFoodTheme
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserReviewScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (String, String, String, String, Int, String) -> Unit,
    viewModel: UserReviewViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val pullToRefreshState = rememberPullToRefreshState()
    val listState = rememberLazyListState()

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collectLatest { effect ->
            when (effect) {
                is UserReviewUiEffect.NavigateToEdit -> {
                    onNavigateToEdit(
                        effect.orderId,
                        effect.restaurantId,
                        effect.restaurantName,
                        effect.restaurantImage,
                        effect.rating,
                        effect.comment
                    )
                }

                is UserReviewUiEffect.ShowToast -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    val shouldLoadNextPage = remember {
        derivedStateOf {
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()
                ?: return@derivedStateOf false
            lastVisibleItem.index >= listState.layoutInfo.totalItemsCount - 2
        }
    }

    LaunchedEffect(shouldLoadNextPage.value) {
        if (shouldLoadNextPage.value && !state.isLoading && !state.endReached) {
            viewModel.onEvent(UserReviewEvent.LoadNextPage)
        }
    }

    UserReviewContent(
        state = state,
        onEvent = viewModel::onEvent,
        pullToRefreshState = pullToRefreshState,
        listState = listState,
        onNavigateBack = onNavigateBack,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserReviewContent(
    state: UserReviewState,
    onEvent: (UserReviewEvent) -> Unit,
    pullToRefreshState: PullToRefreshState,
    listState: LazyListState,
    onNavigateBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            DFoodTopBar(
                title = "My Reviews",
                onBackClick = onNavigateBack
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        PullToRefreshBox(
            state = pullToRefreshState,
            isRefreshing = state.isRefreshing,
            onRefresh = { onEvent(UserReviewEvent.RefreshReviews) },
            modifier = Modifier.padding(innerPadding)
        ) {
            if (state.reviews.isEmpty() && !state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "You haven't written any reviews yet.", 
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(state.reviews, key = { it.id }) { review ->
                        UserReviewItem(
                            review = review,
                            onEditClick = { onEvent(UserReviewEvent.EditReview(review)) },
                            onDeleteClick = { onEvent(UserReviewEvent.DeleteReview(review.id)) }
                        )
                    }

                    if (state.isLoading && !state.isRefreshing) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun UserReviewPreview() {
    DFoodTheme(darkTheme = false) {
        UserReviewContent(
            state = UserReviewState(
                reviews = listOf(
                    UserReview(
                        id = "1",
                        restaurantId = "res1",
                        restaurantName = "Pizza Hut",
                        restaurantImage = "",
                        rating = 5,
                        comment = "Very good!",
                        tags = listOf("Delicious"),
                        createdAt = System.currentTimeMillis(),
                        orderId = "order1"
                    )
                )
            ),
            onEvent = {},
            pullToRefreshState = rememberPullToRefreshState(),
            listState = rememberLazyListState(),
            onNavigateBack = {}
        )
    }
}
