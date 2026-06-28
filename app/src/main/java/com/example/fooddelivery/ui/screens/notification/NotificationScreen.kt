package com.example.fooddelivery.ui.screens.notification

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fooddelivery.domain.model.NotificationType
import com.example.fooddelivery.domain.model.effectiveActions
import com.example.fooddelivery.ui.components.topbar.DFoodTopBar
import com.example.fooddelivery.ui.screens.notification.components.EmptyNotificationsView
import com.example.fooddelivery.ui.screens.notification.components.NotificationItem
import com.example.fooddelivery.ui.theme.DFoodTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    onNavigateBack: () -> Unit,
    onNavigateToOrder: (String) -> Unit,
    onNavigateToChat: ((String) -> Unit)? = null,
    showBackButton: Boolean = true,
    viewModel: NotificationViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }

    val shouldLoadMore = remember {
        derivedStateOf {
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()
                ?: return@derivedStateOf false
            lastVisibleItem.index >= listState.layoutInfo.totalItemsCount - 2
        }
    }

    LaunchedEffect(shouldLoadMore.value) {
        if (shouldLoadMore.value && !state.isPaginating && !state.isEndReached) {
            viewModel.onEvent(NotificationEvent.LoadMore)
        }
    }

    LaunchedEffect(state.successMessage) {
        state.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onEvent(NotificationEvent.SuccessDismissed)
        }
    }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onEvent(NotificationEvent.ErrorDismissed)
        }
    }

    NotificationContent(
        state = state,
        onEvent = viewModel::onEvent,
        onNavigateBack = onNavigateBack,
        onNavigateToOrder = onNavigateToOrder,
        onNavigateToChat = onNavigateToChat,
        listState = listState,
        snackbarHostState = snackbarHostState,
        showBackButton = showBackButton
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationContent(
    state: NotificationState,
    onEvent: (NotificationEvent) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToOrder: (String) -> Unit,
    onNavigateToChat: ((String) -> Unit)? = null,
    listState: LazyListState,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    showBackButton: Boolean = true
) {
    val pullRefreshState = rememberPullToRefreshState()

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            DFoodTopBar(
                title = "Notifications",
                onBackClick = if (showBackButton) onNavigateBack else null,
                actions = {
                    if (state.notifications.any { !it.isRead }) {
                        IconButton(onClick = { onEvent(NotificationEvent.MarkAllRead) }) {
                            Icon(
                                imageVector = Icons.Default.DoneAll,
                                contentDescription = "Mark all as read",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = state.isLoading && state.notifications.isNotEmpty(),
            onRefresh = { onEvent(NotificationEvent.LoadNotifications) },
            state = pullRefreshState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                if (state.isLoading && state.notifications.isEmpty()) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else if (state.notifications.isEmpty()) {
                    EmptyNotificationsView()
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(
                            items = state.notifications,
                            key = { it.id }
                        ) { notification ->
                            val hasActions = notification.effectiveActions().isNotEmpty()
                            NotificationItem(
                                notification = notification,
                                onClick = {
                                    if (!notification.isRead) {
                                        onEvent(NotificationEvent.MarkAsRead(notification.id))
                                    }
                                    if (notification.type == NotificationType.CHAT && notification.targetId != null) {
                                        onNavigateToChat?.invoke(notification.targetId)
                                    } else if (notification.type == NotificationType.ORDER && notification.targetId != null) {
                                        onNavigateToOrder(notification.targetId)
                                    }
                                },
                                onActionClick = { action ->
                                    onEvent(NotificationEvent.ExecuteAction(notification.id, action))
                                },
                                isProcessing = state.processingNotificationId == notification.id
                            )
                            if (!hasActions) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 24.dp),
                                    thickness = 0.5.dp,
                                    color = MaterialTheme.colorScheme.outlineVariant
                                )
                            }
                        }
                        if (state.isPaginating) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun NotificationScreenPreview() {
    DFoodTheme(darkTheme = false) {
        NotificationContent(
            state = NotificationState(),
            onEvent = {},
            onNavigateBack = {},
            onNavigateToOrder = {},
            listState = rememberLazyListState()
        )
    }
}
