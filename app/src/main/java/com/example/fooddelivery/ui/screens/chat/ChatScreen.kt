package com.example.fooddelivery.ui.screens.chat

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fooddelivery.ui.components.topbar.DFoodTopBar
import com.example.fooddelivery.ui.screens.chat.components.*
import com.example.fooddelivery.ui.theme.DFoodTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

@Composable
fun ChatScreen(
    conversationId: String? = null,
    restaurantName: String? = null,
    restaurantImage: String? = null,
    orderId: Int? = null,
    sellerId: Int? = null,
    onNavigateBack: () -> Unit,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(conversationId, orderId, sellerId) {
        if (conversationId != null) {
            viewModel.onEvent(ChatEvent.InitChat(conversationId, restaurantName, restaurantImage))
        } else if (orderId != null && sellerId != null) {
            viewModel.onEvent(ChatEvent.InitChatFromOrder(orderId, sellerId))
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            scope.launch {
                try {
                    val filePath = withContext(Dispatchers.IO) {
                        val file = File(context.cacheDir, "chat_image.jpg")
                        val inputStream = context.contentResolver.openInputStream(it)
                        if (inputStream != null) {
                            inputStream.use { input ->
                                FileOutputStream(file).use { output ->
                                    input.copyTo(output)
                                }
                            }
                            file.absolutePath
                        } else {
                            null
                        }
                    }
                    filePath?.let { path ->
                        viewModel.onEvent(ChatEvent.SendImage(path))
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    ChatContent(
        state = state,
        onNavigateBack = onNavigateBack,
        onEvent = viewModel::onEvent,
        onAddClick = { imagePickerLauncher.launch("image/*") }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatContent(
    state: ChatState,
    onNavigateBack: () -> Unit,
    onEvent: (ChatEvent) -> Unit,
    onAddClick: () -> Unit
) {
    val listState = rememberLazyListState()
    val shouldLoadMore = remember {
        derivedStateOf {
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()
            lastVisibleItem != null && lastVisibleItem.index >= listState.layoutInfo.totalItemsCount - 5
        }
    }

    LaunchedEffect(shouldLoadMore.value) {
        if (shouldLoadMore.value && state.hasMore && !state.isLoadMore && !state.isLoading) {
            onEvent(ChatEvent.LoadMoreHistory)
        }
    }

    Scaffold(
        topBar = {
            DFoodTopBar(
                title = state.restaurantName,
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
                onSend = { onEvent(ChatEvent.SendMessage) },
                onAddClick = onAddClick,
                onEmojiSelected = { emoji -> 
                    onEvent(ChatEvent.OnTextChanged(state.inputText + emoji))
                },
                isUploading = state.isUploadingImage
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
                state = listState,
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

                items(state.messages, key = { it.id }) { message ->
                    ChatBubble(
                        message = message,
                        currentUserId = state.currentUserId,
                        restaurantImage = state.restaurantImage,
                        restaurantName = state.restaurantName
                    )
                }

                if (state.isLoadMore) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            contentAlignment = androidx.compose.ui.Alignment.Center
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        }
                    }
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
            onEvent = {},
            onAddClick = {}
        )
    }
}
