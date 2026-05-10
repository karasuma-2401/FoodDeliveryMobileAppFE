package com.example.fooddelivery.ui.screens.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class Conversation(
    val id: String = "",
    val restaurantName: String = "",
    val restaurantImage: String = "",
    val lastMessage: String = "",
    val lastMessageTime: String = "",
    val unreadCount: Int = 0
)

data class ConversationListState(
    val conversations: List<Conversation> = emptyList(),
    val isLoading: Boolean = false,
    val searchQuery: String = ""
)

sealed interface ConversationEvent {
    object LoadConversations : ConversationEvent
    data class OnSearchQueryChanged(val query: String) : ConversationEvent
}

@HiltViewModel
class ConversationViewModel @Inject constructor() : ViewModel() {
    private val _state = MutableStateFlow(ConversationListState())
    val state = _state.asStateFlow()

    init {
        onEvent(ConversationEvent.LoadConversations)
    }

    fun onEvent(event: ConversationEvent) {
        when (event) {
            is ConversationEvent.LoadConversations -> fetchConversations()
            is ConversationEvent.OnSearchQueryChanged -> {
                _state.update { it.copy(searchQuery = event.query) }
            }
        }
    }

    private fun fetchConversations() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
                // call api here
            _state.update { it.copy(
                conversations = listOf(
                    Conversation("1", "Rose Garden Restaurant", "", "De quán đã note lại rùi e...", "12:46 PM", 0),
                    Conversation("2", "Pizza Hut", "", "Đơn hàng của bạn đang được giao", "Yesterday", 2),
                    Conversation("3", "Starbucks", "", "Chào bạn, mình có thể giúp gì?", "Monday", 0)
                ),
                isLoading = false
            ) }
        }
    }
}