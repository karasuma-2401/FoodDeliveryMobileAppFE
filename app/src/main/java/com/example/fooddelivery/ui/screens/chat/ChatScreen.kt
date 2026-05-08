package com.example.fooddelivery.ui.screens.chat

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fooddelivery.ui.components.topbar.DFoodTopBar
import com.example.fooddelivery.ui.screens.chat.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    onNavigateBack: () -> Unit,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            DFoodTopBar(
                title = "Chat with Restaurant",
                onBackClick = onNavigateBack,
                actions = {
                    IconButton(onClick = { /* Help Action */ }) {
                        Icon(
                            imageVector = Icons.Default.HelpOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        },
        bottomBar = {
            ChatBottomSection(
                inputText = state.inputText,
                orderStatus = state.orderStatus,
                deliveryTime = state.estimatedDelivery,
                onTextChange = { viewModel.onEvent(ChatEvent.OnTextChanged(it)) },
                onSend = { viewModel.onEvent(ChatEvent.SendMessage) }
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
                        onReplyClick = { viewModel.onEvent(ChatEvent.SelectSuggestedReply(it)) }
                    )
                }

                items(state.messages.reversed()) { message ->
                    ChatBubble(
                        message = message,
                        restaurantImage = state.restaurantImage
                    )
                }

                item { ChatDateDivider(date = "Today") }
            }
        }
    }
}