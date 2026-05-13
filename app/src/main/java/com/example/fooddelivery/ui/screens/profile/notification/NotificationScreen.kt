package com.example.fooddelivery.ui.screens.profile.notification

import android.text.format.DateUtils
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fooddelivery.domain.model.Notification
import com.example.fooddelivery.domain.model.NotificationType
import com.example.fooddelivery.ui.components.topbar.DFoodTopBar
import com.example.fooddelivery.ui.screens.profile.notification.components.EmptyNotificationsView
import com.example.fooddelivery.ui.screens.profile.notification.components.NotificationItem
import com.example.fooddelivery.ui.theme.DFoodTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    onNavigateBack: () -> Unit,
    onNavigateToOrder: (String) -> Unit,
    viewModel: NotificationViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
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
    NotificationContent(
        state = state,
        onEvent = viewModel::onEvent,
        onNavigateBack = onNavigateBack,
        onNavigateToOrder = onNavigateToOrder,
        viewModel = viewModel,
        listState = listState

    )
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationContent(
    state: NotificationState = viewModel.state.collectAsState().value,
    onEvent: (NotificationEvent) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToOrder: (String) -> Unit,
    viewModel: NotificationViewModel = viewModel(),
    listState: LazyListState
) {
    Scaffold(
        topBar = {
            DFoodTopBar(
                title = "Notification",
                onBackClick = onNavigateBack,
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
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
                    items(state.notifications) { notification ->
                        NotificationItem(
                            notification = notification,
                            onClick = {
                                viewModel.onEvent(NotificationEvent.MarkAsRead(notification.id))
                                if (notification.type == NotificationType.ORDER && notification.targetId != null) {
                                    onNavigateToOrder(notification.targetId)
                                }
                            }
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 24.dp),
                            thickness = 0.5.dp,
                            color = MaterialTheme.colorScheme.outlineVariant
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
                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
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
            viewModel = hiltViewModel(),
            listState = rememberLazyListState()
        )
    }
}