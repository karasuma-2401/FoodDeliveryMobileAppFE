package com.example.fooddelivery.ui.screens.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fooddelivery.domain.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UnreadChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository
) : ViewModel() {

    val unreadCount: StateFlow<Int> = chatRepository.getConversations()
        .map { conversations -> conversations.sumOf { it.unreadCount } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = 0
        )

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            chatRepository.syncConversations()
        }
    }
}
