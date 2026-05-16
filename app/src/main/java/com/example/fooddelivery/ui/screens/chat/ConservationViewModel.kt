package com.example.fooddelivery.ui.screens.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fooddelivery.data.local.room.entity.ConversationEntity
import com.example.fooddelivery.domain.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ConversationListState(
    val conversations: List<ConversationEntity> = emptyList(),
    val isLoading: Boolean = false,
    val searchQuery: String = ""
)

sealed interface ConversationEvent {
    object SyncConversations : ConversationEvent
    data class OnSearchQueryChanged(val query: String) : ConversationEvent
    data class MarkAsRead(val conversationId: String) : ConversationEvent
}

@HiltViewModel
class ConversationViewModel @Inject constructor(
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _isLoading = MutableStateFlow(false)

    val state: StateFlow<ConversationListState> = combine(
        chatRepository.getConversations(),
        _searchQuery,
        _isLoading
    ) { conversations, query, loading ->
        val filteredConversations = if (query.isEmpty()) {
            conversations
        } else {
            conversations.filter {
                it.restaurantName.contains(query, ignoreCase = true)
            }
        }
        ConversationListState(
            conversations = filteredConversations,
            searchQuery = query,
            isLoading = loading
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ConversationListState()
    )

    init {
        onEvent(ConversationEvent.SyncConversations)
    }

    fun onEvent(event: ConversationEvent) {
        when (event) {
            is ConversationEvent.SyncConversations -> syncConversations()
            is ConversationEvent.MarkAsRead -> markAsRead(event.conversationId)
            is ConversationEvent.OnSearchQueryChanged -> {
                _searchQuery.value = event.query
            }
        }
    }

    private fun syncConversations() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                chatRepository.syncConversations()
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun markAsRead(id: String) {
        viewModelScope.launch {
            chatRepository.markAsRead(id)
        }
    }
}
