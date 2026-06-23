package com.example.fooddelivery.domain.repository

import com.example.fooddelivery.data.local.room.entity.ConversationEntity
import com.example.fooddelivery.data.local.room.entity.MessageEntity
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    fun getConversations(): Flow<List<ConversationEntity>>
    suspend fun syncConversations(): Result<Unit>
    suspend fun createConversation(orderId: Int, sellerId: Int): Result<ConversationEntity>
    suspend fun markAsRead(conversationId: String): Result<Unit>

    fun getMessages(conversationId: String): Flow<List<MessageEntity>>
    suspend fun syncMessages(conversationId: String, page: Int): Result<Unit>
    suspend fun syncConversationDetail(conversationId: Int, page: Int): Result<Unit>
    suspend fun syncConversationDetailByOrder(orderId: Int, page: Int): Result<Unit>
    
    suspend fun sendMessage(conversationId: String, senderId: String, content: String, imageUrl: String? = null): Result<Unit>
    suspend fun joinRoom(conversationId: String)
    suspend fun leaveRoom(conversationId: String)
    suspend fun uploadImage(imagePath: String): Result<String>
    suspend fun handleNewMessage(message: MessageEntity)
}
