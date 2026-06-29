package com.example.fooddelivery.data.local.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.fooddelivery.data.local.room.entity.ConversationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ConversationDao {
    @Query("SELECT * FROM conversations ORDER BY lastMessageTime DESC")
    fun getConversations(): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversations")
    suspend fun getAllConversations(): List<ConversationEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateConversation(conversation: ConversationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(conversations: List<ConversationEntity>)

    @Query("DELETE FROM conversations")
    suspend fun deleteAll()

    @Transaction
    suspend fun replaceAll(conversations: List<ConversationEntity>) {
        deleteAll()
        if (conversations.isNotEmpty()) {
            insertAll(conversations)
        }
    }

    @Query("UPDATE conversations SET unreadCount = 0 WHERE id = :id")
    suspend fun markConversationAsRead(id: String)

    @Query("SELECT * FROM conversations WHERE id = :id LIMIT 1")
    suspend fun getConversationById(id: String): ConversationEntity?

    @Query(
        """
        UPDATE conversations
        SET lastMessage = :lastMessage, lastMessageTime = :lastMessageTime
        WHERE id = :id
        """
    )
    suspend fun updateLastMessage(id: String, lastMessage: String, lastMessageTime: String)
}