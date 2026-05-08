package com.example.fooddelivery.ui.screens.chat

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fooddelivery.ui.components.topbar.DFoodTopBar
import com.example.fooddelivery.ui.screens.chat.components.*
import com.example.fooddelivery.ui.theme.DFoodTheme


@Composable
fun ChatScreen(
    onNavigateBack: () -> Unit,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ChatContent(
        state = state,
        onNavigateBack = onNavigateBack,
        onEvent = viewModel::onEvent
    )
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatContent(
    state: ChatState,
    onNavigateBack: () -> Unit,
    onEvent: (ChatEvent) -> Unit
) {
    Scaffold(
        topBar = {
            DFoodTopBar(
                title = "Chat with Restaurant",
                onBackClick = onNavigateBack,
                actions = {},
                scrollBehavior = null,
            )
        },
        bottomBar = {
            ChatBottomSection(
                inputText = state.inputText,
                orderStatus = state.orderStatus,
                onTextChange = { onEvent(ChatEvent.OnTextChanged(it)) },
                onSend = { onEvent(ChatEvent.SendMessage) }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            ChatHeaderInfo(
                restaurantName = state.restaurantName,
                restaurantImage = state.restaurantImage,
                isOnline = state.isOnline
            )

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                reverseLayout = true
            ) {
                item {
                    SuggestedReplies(
                        onReplyClick = { onEvent(ChatEvent.SelectSuggestedReply(it)) }
                    )
                }

                items(state.messages.reversed()) { message ->
                    ChatBubble(
                        message = message,
                        restaurantImage = state.restaurantImage,
                        restaurantName = state.restaurantName
                    )
                }

                item { ChatDateDivider(date = "Today") }
            }
        }
    }
}
@Preview(showBackground = false, showSystemUi = false)
@Composable
fun ChatScreenPreview() {
    DFoodTheme(darkTheme = false) {
        ChatContent(
            state = ChatState(),
            onNavigateBack = {},
            onEvent = {}
        )
    }
}