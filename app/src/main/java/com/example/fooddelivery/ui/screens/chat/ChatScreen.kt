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
import com.example.fooddelivery.util.messagesForMessengerDisplay
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
        } else if (orderId != null) {
            viewModel.onEvent(ChatEvent.InitChatFromOrder(orderId, sellerId ?: 0))
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            scope.launch {
                try {
                    val filePath = withContext(Dispatchers.IO) {
                        val mimeType = context.contentResolver.getType(it)
                        val extension = when {
                            mimeType?.contains("png") == true -> "png"
                            mimeType?.contains("webp") == true -> "webp"
                            mimeType?.contains("gif") == true -> "gif"
                            else -> "jpg"
                        }
                        val file = File(context.cacheDir, "chat_${System.currentTimeMillis()}.$extension")
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
    val displayMessages = remember(state.messages) {
        messagesForMessengerDisplay(state.messages)
    }
    val shouldLoadMore = remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            if (totalItems == 0 || state.messages.isEmpty()) return@derivedStateOf false
            val lastVisibleIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisibleIndex >= totalItems - 2 &&
                state.hasMore &&
                !state.isLoadMore &&
                !state.isLoading
        }
    }

    LaunchedEffect(shouldLoadMore.value) {
        if (shouldLoadMore.value) {
            onEvent(ChatEvent.LoadMoreHistory)
        }
    }

    LaunchedEffect(displayMessages.firstOrNull()?.id, state.isLoading) {
        if (state.isLoading || displayMessages.isEmpty()) return@LaunchedEffect
        listState.animateScrollToItem(0)
    }

    Scaffold(
        topBar = {
            DFoodTopBar(
                title = when {
                    state.isBusinessUser && state.restaurantName.isNotBlank() -> state.restaurantName
                    state.isBusinessUser -> "Customer"
                    else -> state.restaurantName
                },
                onBackClick = onNavigateBack,
                actions = {},
                scrollBehavior = null,
            )
        },
        bottomBar = {
            Column {
                SuggestedReplies(
                    isBusinessUser = state.isBusinessUser,
                    onReplyClick = { onEvent(ChatEvent.SelectSuggestedReply(it)) }
                )
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
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(
                state = listState,
                reverseLayout = true,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                items(displayMessages, key = { it.id }) { message ->
                    ChatBubble(
                        message = message,
                        currentUserId = state.currentUserId,
                        restaurantImage = state.restaurantImage,
                        restaurantName = state.restaurantName
                    )
                }

                if (state.isLoadMore) {
                    item(key = "load_more") {
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

                item(key = "date_divider") {
                    ChatDateDivider(date = "Today")
                }
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
