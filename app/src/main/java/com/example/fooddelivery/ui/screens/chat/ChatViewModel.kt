package com.example.fooddelivery.ui.screens.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.socket.client.Ack
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.delay
import java.util.UUID

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val senderId: Int = 0,
    val content: String = "",
    val createdAt: String = "12:00 PM",
    val who: String = "other",
    val isSending: Boolean = false,
    val isError: Boolean = false
)

data class ChatState(
    val messages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = false,
    val inputText: String = "",
    val conversationId: String? = null,
    val restaurantName: String = "Rose Garden Restaurant",
    val restaurantImage: String = "https://example.com/logo.jpg",
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
    private var socket: Socket? = null
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
                viewModelScope.launch {
                    _state.update { it.copy(messages = it.messages + event.message) }
                }
            }
        }
    }

    private fun loadHistoryAndConnectSocket(orderId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            delay(1000)
            
            _state.update { it.copy(
                isLoading = false,
                conversationId = "conv_123",
                messages = listOf(
                    ChatMessage(content = "Hello! Your orders are preparing.", who = "other", createdAt = "12:05 PM")
                )
            )}
            
            setupSocket()
        }
    }

    private fun setupSocket() {
        try {
            val options = IO.Options().apply {
                forceNew = true
                reconnection = true
            }
            socket = IO.socket("http://10.0.2.2:4000", options) 

            socket?.on(Socket.EVENT_CONNECT) {
                val joinData = JSONObject().apply {
                    put("conversationId", _state.value.conversationId)
                }
                socket?.emit("join-room", joinData)
            }

            socket?.on("text-chat") { args ->
                val data = args[0] as JSONObject
                val newMessage = ChatMessage(
                    content = data.optString("content"),
                    senderId = data.optInt("senderId"),
                    who = "other",
                    createdAt = "Just now"
                )
                onEvent(ChatEvent.NewMessageReceived(newMessage))
            }

            socket?.connect()
        } catch (e: Exception) {
            _state.update { it.copy(error = "Cannot connect with chat server") }
        }
    }

    private fun sendMessage() {
        val text = _state.value.inputText
        if (text.isBlank()) return

        val tempMessage = ChatMessage(
            content = text,
            who = "me",
            createdAt = "Just now",
            isSending = true
        )
        _state.update { it.copy(
            messages = it.messages + tempMessage,
            inputText = ""
        )}
        val payload = JSONObject().apply {
            put("conversationId", _state.value.conversationId)
            put("content", text)
        }
        socket?.emit(
            "text-chat",
            payload,
            Ack {
                viewModelScope.launch {
                    updateMessageStatus(tempMessage.id)
                }
            }
        )
    }

    private fun updateMessageStatus(tempId: String) {
        _state.update { currentState ->
            val updatedMessages = currentState.messages.map {
                if (it.id == tempId) it.copy(isSending = false) else it
            }
            currentState.copy(messages = updatedMessages)
        }
    }

    override fun onCleared() {
        socket?.off()
        socket?.disconnect()
        super.onCleared()
    }
}
