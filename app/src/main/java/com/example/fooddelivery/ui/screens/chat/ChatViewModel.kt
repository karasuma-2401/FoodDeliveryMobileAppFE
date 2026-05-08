package com.example.fooddelivery.ui.screens.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatMessage(
    val id: Int = 0,
    val senderId: Int = 0,
    val content: String = "",
    val createdAt: String = "12:00 PM",
    val who: String = "other" // "me" or "other"
)

data class ChatState(
    val messages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = false,
    val inputText: String = "",
    val conversationId: Int? = null,
    val restaurantName: String = "Rose Garden Restaurant",
    val restaurantImage: String = "",
    val isOnline: Boolean = true,
    val orderStatus: String = "Order Delivering",
    val estimatedDelivery: String = "20 min",
    val error: String? = null
)

sealed interface ChatEvent {
    data class InitChat(val orderId: String) : ChatEvent
    data class OnTextChanged(val text: String) : ChatEvent
    data object SendMessage : ChatEvent
    data class SelectSuggestedReply(val text: String) : ChatEvent
    data class NewMessageReceived(val message: ChatMessage) : ChatEvent
}

@HiltViewModel
class ChatViewModel @Inject constructor() : ViewModel() {
    private var socket: io.socket.client.Socket? = null
    private val _state = MutableStateFlow(ChatState())
    val state: StateFlow<ChatState> = _state.asStateFlow()

    fun onEvent(event: ChatEvent) {
        when (event) {
            is ChatEvent.InitChat -> loadHistoryAndConnectSocket(event.orderId)
            is ChatEvent.OnTextChanged -> _state.update { it.copy(inputText = event.text) }
            is ChatEvent.SendMessage -> sendMessage()
            is ChatEvent.SelectSuggestedReply -> {
                _state.update { it.copy(inputText = event.text) }
                sendMessage()
            }
            is ChatEvent.NewMessageReceived -> {
                _state.update { it.copy(messages = it.messages + event.message) }
            }
        }
    }

    private fun loadHistoryAndConnectSocket(orderId: String) {
        viewModelScope.launch {
            _state.update { it.copy(
                messages = listOf(
                    ChatMessage(content = "Hello! Your order is being prepared.", who = "other", createdAt = "12:05 PM")
                )
            )}
            // setupSocket()
        }
    }

    private fun sendMessage() {
        val text = _state.value.inputText
        if (text.isBlank()) return

        val newMessage = ChatMessage(
            content = text,
            who = "me",
            createdAt = "Just now"
        )
        
        _state.update { it.copy(
            messages = it.messages + newMessage,
            inputText = ""
        )}
        // socket?.emit("text-chat", payload)
    }

    override fun onCleared() {
        socket?.disconnect()
        super.onCleared()
    }
}
