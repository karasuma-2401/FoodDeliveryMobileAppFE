package com.example.fooddelivery.ui.screens.chat

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fooddelivery.ui.components.topbar.DFoodTopBar
import com.example.fooddelivery.ui.screens.chat.components.ConversationItem
import com.example.fooddelivery.ui.screens.chat.components.ConversationItemSkeleton
import com.example.fooddelivery.ui.screens.chat.components.ConversationSearchField
import com.example.fooddelivery.ui.theme.DFoodTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationScreen(
    onNavigateBack: () -> Unit,
    onNavigateToChat: (String, String, String) -> Unit,
    screenTitle: String = "Messages",
    viewModel: ConversationViewModel = hiltViewModel()
) {
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.onEvent(ConversationEvent.SyncConversations)
    }

    ConversationContent(
        onNavigateBack = onNavigateBack,
        onNavigateToChat = onNavigateToChat,
        screenTitle = screenTitle,
        viewModel = viewModel
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationContent(
    onNavigateBack: () -> Unit,
    onNavigateToChat: (String, String, String) -> Unit,
    screenTitle: String = "Messages",
    viewModel: ConversationViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val pullRefreshState = rememberPullToRefreshState()
    var isPullRefreshing by remember { mutableStateOf(false) }

    LaunchedEffect(state.isLoading) {
        if (!state.isLoading) isPullRefreshing = false
    }

    Scaffold(
        topBar = {
            DFoodTopBar(
                title = screenTitle,
                onBackClick = onNavigateBack
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            ConversationSearchField(
                query = state.searchQuery,
                onQueryChange = { viewModel.onEvent(ConversationEvent.OnSearchQueryChanged(it)) }
            )

            PullToRefreshBox(
                isRefreshing = isPullRefreshing,
                onRefresh = {
                    isPullRefreshing = true
                    viewModel.onEvent(ConversationEvent.SyncConversations)
                },
                state = pullRefreshState,
                modifier = Modifier.fillMaxSize()
            ) {
                if (state.isLoading && state.conversations.isEmpty()) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(8) {
                            ConversationItemSkeleton()
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 24.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(state.conversations, key = { it.id }) { conversation ->
                            ConversationItem(
                                conversation = conversation,
                                onClick = {
                                    viewModel.onEvent(ConversationEvent.MarkAsRead(conversation.id))
                                    onNavigateToChat(conversation.id, conversation.restaurantName, conversation.restaurantImage)
                                }
                            )
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 24.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }
        }
    }
}
