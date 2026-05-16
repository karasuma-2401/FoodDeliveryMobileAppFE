package com.example.fooddelivery.ui.screens.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fooddelivery.data.local.room.entity.MessageEntity
import com.example.fooddelivery.domain.repository.ChatRepository
import com.example.fooddelivery.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
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
    val orderStatus: String = "Order Delivering",
    val currentPage: Int = 0,
    val error: String? = null
)

sealed interface ChatEvent {
    data class InitChat(val conversationId: String, val restaurantName: String, val restaurantImage: String) : ChatEvent
    data class OnTextChanged(val text: String) : ChatEvent
    data object SendMessage : ChatEvent
    data class SendImage(val imagePath: String) : ChatEvent
    data object LoadMoreHistory : ChatEvent
    data class SelectSuggestedReply(val text: String) : ChatEvent
}

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ChatState())
    val state: StateFlow<ChatState> = _state.asStateFlow()
    private var currentConversationId: String? = null
    init {
        getCurrentUser()
    }

    fun onEvent(event: ChatEvent) {
        when (event) {
            is ChatEvent.InitChat -> {
                currentConversationId = event.conversationId
                _state.update { it.copy(
                    conversationId = event.conversationId,
                    restaurantName = event.restaurantName,
                    restaurantImage = event.restaurantImage
                ) }
                observeMessages(event.conversationId)
                syncInitialMessages(event.conversationId)
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
            userRepository.getUserProfile().onSuccess { user ->
                _state.update { it.copy(currentUserId = user.id) }
            }.onFailure { error ->
                _state.update { it.copy(error = error.message) }
            }
        }
    }

    private fun observeMessages(conversationId: String) {
        viewModelScope.launch {
            chatRepository.getMessages(conversationId).collectLatest { messages ->
                _state.update { it.copy(messages = messages) }
            }
        }
    }

    private fun syncInitialMessages(conversationId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            chatRepository.syncMessages(conversationId, 0)
            _state.update { it.copy(isLoading = false) }
        }
    }

    private fun loadMoreMessages() {
        val conversationId = currentConversationId ?: return
        if (_state.value.isLoadMore) return
        
        viewModelScope.launch {
            _state.update { it.copy(isLoadMore = true) }
            val nextPage = _state.value.currentPage + 1
            chatRepository.syncMessages(conversationId, nextPage).onSuccess {
                _state.update { it.copy(currentPage = nextPage, isLoadMore = false) }
            }.onFailure {
                _state.update { it.copy(isLoadMore = false) }
            }
        }
    }

    private fun sendMessage(content: String, imageUrl: String? = null) {
        val conversationId = currentConversationId ?: return
        if (content.isBlank() && imageUrl == null) return

        viewModelScope.launch {
            val userId = _state.value.currentUserId
            _state.update { it.copy(inputText = "") }
            chatRepository.sendMessage(conversationId, userId, content, imageUrl)
        }
    }

    private fun uploadAndSendImage(path: String) {
        val conversationId = currentConversationId ?: return
        viewModelScope.launch {
            _state.update { it.copy(isUploadingImage = true) }
            chatRepository.uploadImage(path).onSuccess { imageUrl ->
                _state.update { it.copy(isUploadingImage = false) }
                sendMessage("", imageUrl)
            }.onFailure { error ->
                _state.update { it.copy(isUploadingImage = false, error = "Failed to upload image") }
            }
        }
    }
}
