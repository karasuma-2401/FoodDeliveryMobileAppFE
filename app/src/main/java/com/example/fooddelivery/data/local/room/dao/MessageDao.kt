package com.example.fooddelivery.data.local.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.fooddelivery.data.local.room.entity.MessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<MessageEntity>)

    @Update
    suspend fun updateMessage(message: MessageEntity)

    @Query(
        """
        SELECT * FROM messages
        WHERE conversationId = :conversationId
        ORDER BY CAST(createdAt AS INTEGER) ASC,
            CASE WHEN id GLOB '*-*' THEN 9223372036854775807 ELSE CAST(id AS INTEGER) END ASC
        """
    )
    fun getMessages(conversationId: String): Flow<List<MessageEntity>>

    @Query("DELETE FROM messages WHERE id = :id")
    suspend fun deleteMessage(id: String)

    /** Removes optimistic UUID rows when the server message arrives. */
    @Query(
        """
        DELETE FROM messages
        WHERE conversationId = :conversationId
          AND senderId = :senderId
          AND id != :serverMessageId
          AND id GLOB '*-*'
        """
    )
    suspend fun deleteOptimisticDuplicates(
        conversationId: String,
        senderId: String,
        serverMessageId: String
    )

    @Query("UPDATE messages SET isRead = 1 WHERE conversationId = :conversationId")
    suspend fun markMessagesAsRead(conversationId: String)

    @Query("SELECT * FROM messages WHERE id = :id LIMIT 1")
    suspend fun getMessageById(id: String): MessageEntity?

    @Query(
        """
        SELECT createdAt FROM messages
        WHERE conversationId = :conversationId
          AND senderId = :senderId
          AND id GLOB '*-*'
        ORDER BY CAST(createdAt AS INTEGER) DESC
        LIMIT 1
        """
    )
    suspend fun getLatestOptimisticCreatedAt(
        conversationId: String,
        senderId: String
    ): String?

    @Query(
        """
        UPDATE messages
        SET isSending = 0, isFailed = 1
        WHERE isSending = 1 AND id GLOB '*-*'
        """
    )
    suspend fun markOptimisticSendingAsFailed()
}
