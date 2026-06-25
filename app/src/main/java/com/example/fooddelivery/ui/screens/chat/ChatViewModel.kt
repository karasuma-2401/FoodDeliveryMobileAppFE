package com.example.fooddelivery.ui.screens.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fooddelivery.data.local.room.entity.MessageEntity
import com.example.fooddelivery.data.local.datastore.TokenManager
import com.example.fooddelivery.domain.repository.ChatRepository
import com.example.fooddelivery.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatState(
    val messages: List<MessageEntity> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadMore: Boolean = false,
    val isUploadingImage: Boolean = false,
    val inputText: String = "",
    val conversationId: String? = null,
    val currentUserId: String = "",
    val restaurantName: String = "Restaurant",
    val restaurantImage: String = "",
    val isOnline: Boolean = true,
    val orderStatus: String = "", // Default empty since API 1.1/1.3 doesn't provide it
    val currentPage: Int = 0,
    val hasMore: Boolean = true,
    val error: String? = null
)

sealed interface ChatEvent {
    data class InitChat(
        val conversationId: String,
        val restaurantName: String? = null,
        val restaurantImage: String? = null
    ) : ChatEvent
    data class InitChatFromOrder(val orderId: Int, val sellerId: Int) : ChatEvent
    data class OnTextChanged(val text: String) : ChatEvent
    data object SendMessage : ChatEvent
    data class SendImage(val imagePath: String) : ChatEvent
    data object LoadMoreHistory : ChatEvent
    data class SelectSuggestedReply(val text: String) : ChatEvent
}

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val userRepository: UserRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _state = MutableStateFlow(ChatState())
    val state: StateFlow<ChatState> = _state.asStateFlow()
    private var currentConversationId: String? = null
    private var messageObserverJob: Job? = null

    init {
        getCurrentUser()
    }

    fun onEvent(event: ChatEvent) {
        when (event) {
            is ChatEvent.InitChat -> {
                initChatById(event.conversationId, event.restaurantName, event.restaurantImage)
            }
            is ChatEvent.InitChatFromOrder -> {
                initChatByOrderId(event.orderId, event.sellerId)
            }
            is ChatEvent.OnTextChanged -> {
                _state.update { it.copy(inputText = event.text) }
            }
            is ChatEvent.SendMessage -> {
                sendMessage(_state.value.inputText)
            }
            is ChatEvent.SendImage -> {
                uploadAndSendImage(event.imagePath)
            }
            is ChatEvent.LoadMoreHistory -> {
                loadMoreMessages()
            }
            is ChatEvent.SelectSuggestedReply -> {
                sendMessage(event.text)
            }
        }
    }

    private fun getCurrentUser() {
        viewModelScope.launch {
            tokenManager.getUserId.first()?.let { id ->
                _state.update { it.copy(currentUserId = id.toString()) }
            }
            userRepository.getUserProfile().onSuccess { user ->
                if (user.id.isNotBlank()) {
                    _state.update { it.copy(currentUserId = user.id) }
                }
            }.onFailure { error ->
                if (_state.value.currentUserId.isBlank()) {
                    _state.update { it.copy(error = error.message) }
                }
            }
        }
    }

    private fun initChatById(
        conversationId: String,
        restaurantName: String? = null,
        restaurantImage: String? = null
    ) {
        currentConversationId = conversationId
        _state.update { it.copy(
            conversationId = conversationId,
            restaurantName = restaurantName ?: it.restaurantName,
            restaurantImage = restaurantImage ?: it.restaurantImage,
            currentPage = 0,
            hasMore = true,
            isLoading = true
        ) }
        
        viewModelScope.launch {
            chatRepository.joinRoom(conversationId)
            chatRepository.markAsRead(conversationId)
            observeMessages(conversationId)
            chatRepository.syncConversationDetail(conversationId.toInt(), 0).onSuccess {
                _state.update { it.copy(isLoading = false) }
            }.onFailure { error ->
                _state.update { it.copy(isLoading = false, error = error.message) }
            }
        }
    }

    private fun initChatByOrderId(orderId: Int, sellerId: Int) {
        _state.update { it.copy(isLoading = true) }
        
        viewModelScope.launch {
            chatRepository.createConversation(orderId, sellerId).onSuccess { entity ->
                currentConversationId = entity.id
                _state.update { it.copy(
                    conversationId = entity.id,
                    restaurantName = entity.restaurantName,
                    restaurantImage = entity.restaurantImage
                ) }
                
                chatRepository.joinRoom(entity.id)
                chatRepository.markAsRead(entity.id)
                observeMessages(entity.id)
                chatRepository.syncConversationDetailByOrder(orderId, 0)
                
            }.onFailure { error ->
                _state.update { it.copy(error = error.message) }
            }
            _state.update { it.copy(isLoading = false) }
        }
    }

    private fun observeMessages(conversationId: String) {
        messageObserverJob?.cancel()
        messageObserverJob = viewModelScope.launch {
            chatRepository.getMessages(conversationId).collectLatest { messages ->
                _state.update { it.copy(messages = messages) }
                // Nếu có tin nhắn mới khi đang ở trong chat, tự động đánh dấu đã đọc
                if (messages.any { !it.isRead && it.senderId != _state.value.currentUserId }) {
                    chatRepository.markAsRead(conversationId)
                }
            }
        }
    }

    private fun loadMoreMessages() {
        val conversationId = currentConversationId ?: return
        if (_state.value.isLoadMore || !_state.value.hasMore) return

        viewModelScope.launch {
            _state.update { it.copy(isLoadMore = true) }
            val nextPage = _state.value.currentPage + 1
            val messagesBefore = _state.value.messages.size
            
            chatRepository.syncConversationDetail(conversationId.toInt(), nextPage).onSuccess {
                val messagesAfter = _state.value.messages.size
                val hasMore = messagesAfter > messagesBefore
                _state.update { it.copy(currentPage = nextPage, isLoadMore = false, hasMore = hasMore) }
            }.onFailure {
                _state.update { it.copy(isLoadMore = false, hasMore = false) }
            }
        }
    }

    private fun sendMessage(content: String, imageUrl: String? = null) {
        val conversationId = currentConversationId ?: return
        if (content.isBlank() && imageUrl == null) return

        viewModelScope.launch {
            val userId = _state.value.currentUserId
            if (userId.isBlank()) {
                _state.update { it.copy(error = "User not authenticated") }
                return@launch
            }
            _state.update { it.copy(inputText = "") }
            chatRepository.sendMessage(conversationId, userId, content, imageUrl)
                .onFailure { error ->
                    _state.update { it.copy(error = error.message ?: "Failed to send message") }
                }
        }
    }

    private fun uploadAndSendImage(path: String) {
        val conversationId = currentConversationId ?: return
        viewModelScope.launch {
            val userId = _state.value.currentUserId
            if (userId.isBlank()) {
                _state.update { it.copy(error = "User not authenticated") }
                return@launch
            }
            _state.update { it.copy(isUploadingImage = true) }
            chatRepository.uploadAndSendImage(conversationId, userId, path)
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isUploadingImage = false,
                            error = error.message ?: "Failed to upload image"
                        )
                    }
                }
                .onSuccess {
                    _state.update { it.copy(isUploadingImage = false) }
                }
        }
    }

    override fun onCleared() {
        currentConversationId?.let { id ->
            viewModelScope.launch {
                chatRepository.leaveRoom(id)
            }
        }
        super.onCleared()
    }
}
