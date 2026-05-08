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
    val conservationId: Int = 0,
    val senderId: Int = 0,
    val content: String = "",
    val createdAt: String = "",
    val who: String = "other"
)
data class ChatState(
    val message: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = false,
    val inputText: String = "",
    val conservationId: Int? = null,
    val error: String? = null
)
sealed interface ChatEvent {
    data class InitChat(val orderId: String) : ChatEvent
    data class OnTextChanged(val text: String) : ChatEvent
    object SendMessage : ChatEvent
    data class NewMessageReceived(val message: ChatMessage) : ChatEvent
}
@HiltViewModel
class ChatViewModel @Inject constructor(
    // private val chatRepository: ChatRepository
) : ViewModel() {
    private var socket: io.socket.client.Socket? = null
    private val _state = MutableStateFlow(ChatState())
    val state: StateFlow<ChatState> = _state.asStateFlow()

    fun onEvent(event: ChatEvent) {
        when (event) {
            is ChatEvent.InitChat -> loadHistoryAndConnectSocket(event.orderId)
            is ChatEvent.OnTextChanged -> _state.update { it.copy(inputText = event.text) }
            is ChatEvent.SendMessage -> sendMessage()
            is ChatEvent.NewMessageReceived -> {
                _state.update { it.copy(message = it.message + event.message) }
            }
        }
    }
    private fun loadHistoryAndConnectSocket(orderid: String) {
        viewModelScope.launch {
            // call api get details and history of order{id}
            setupSocket()
        }
    }
    private fun setupSocket() {
        val options = io.socket.client.IO.Options().apply {
            extraHeaders = mapOf("authorization" to listOf("Bearer YOUR_TOKEN_HERE"))
        }
        socket = io.socket.client.IO.socket("YOUR_BACKEND_URL", options)
        socket?.on(io.socket.client.Socket.EVENT_CONNECT) {
            val jointPayLoad = org.json.JSONObject().apply {
                put("conservationId", _state.value.conservationId)
            }
            socket?.emit("join-room", jointPayLoad)
        }
        socket?.on("text-chat") { args ->
            val data = (args[0] as org.json.JSONObject).getJSONObject("data")
            val newMessage = ChatMessage(
                content = data.getString("content"),
                senderId = data.getInt("senderId"),
                who = "other" // logic check senderId to "me" or "other"
            )
            onEvent(ChatEvent.NewMessageReceived(newMessage))
        }
        socket?.connect()
    }
    private fun sendMessage() {
        val payload = org.json.JSONObject().apply {
            put("conversationId", _state.value.conservationId)
            put("content", _state.value.inputText)
        }
        socket?.emit("text-chat", payload)
        _state.update { it.copy(inputText = "") }
    }

    override fun onCleared() {
        socket?.disconnect() // disconnect with chat to avoid leak memory
        super.onCleared()
    }
}