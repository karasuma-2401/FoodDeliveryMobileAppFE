package com.example.fooddelivery.data.repository

import com.example.fooddelivery.data.local.room.dao.ConversationDao
import com.example.fooddelivery.data.local.room.dao.MessageDao
import com.example.fooddelivery.data.local.room.entity.ConversationEntity
import com.example.fooddelivery.data.local.room.entity.MessageEntity
import com.example.fooddelivery.data.remote.api.ChatApi
import com.example.fooddelivery.domain.repository.ChatRepository
import io.socket.client.Socket
import kotlinx.coroutines.flow.Flow
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File
import java.util.UUID
import javax.inject.Inject

class ChatRepositoryImpl @Inject constructor(
    private val chatApi: ChatApi,
    private val conversationDao: ConversationDao,
    private val messageDao: MessageDao,
    private val socket: Socket
) : ChatRepository {

    override fun getConversations(): Flow<List<ConversationEntity>> = conversationDao.getConversations()

    override suspend fun syncConversations(): Result<Unit> {
        return try {
            val response = chatApi.getConversations()
            if (response.isSuccessful && response.body() != null) {
                response.body()!!.forEach { conversationDao.updateConversation(it) }
                Result.success(Unit)
            } else Result.failure(Exception("Sync failed"))
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun markAsRead(conversationId: String): Result<Unit> {
        return try {
            conversationDao.markConversationAsRead(conversationId)
            socket.emit("mark_read", JSONObject().put("conversationId", conversationId))
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    override fun getMessages(conversationId: String): Flow<List<MessageEntity>> = messageDao.getMessages(conversationId)

    override suspend fun syncMessages(conversationId: String, page: Int): Result<Unit> {
        return try {
            val response = chatApi.getMessages(conversationId, limit = 20, offset = page * 20)
            if (response.isSuccessful && response.body() != null) {
                messageDao.insertMessages(response.body()!!)
                Result.success(Unit)
            } else Result.failure(Exception("Fetch failed"))
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun sendMessage(
        conversationId: String,
        senderId: String,
        content: String,
        imageUrl: String?
    ): Result<Unit> {
        val tempId = UUID.randomUUID().toString()
        val message = MessageEntity(
            id = tempId,
            conversationId = conversationId,
            content = content,
            imageUrl = imageUrl,
            createdAt = System.currentTimeMillis().toString(),
            isSending = true,
            isFailed = false,
            senderId = senderId
        )
        messageDao.insertMessage(message)

        return try {
            val json = JSONObject().apply {
                put("id", message.id)
                put("conversationId", message.conversationId)
                put("senderId", message.senderId)
                put("content", message.content)
                put("imageUrl", message.imageUrl)
            }
            socket.emit("send_message", json)
            messageDao.updateMessage(message.copy(isSending = false))
            Result.success(Unit)
        } catch (e: Exception) {
            messageDao.updateMessage(message.copy(isSending = false, isFailed = true))
            Result.failure(e)
        }
    }

    override suspend fun uploadImage(imagePath: String): Result<String> {
        return try {
            val file = File(imagePath)
            val body = MultipartBody.Part.createFormData("image", file.name, file.asRequestBody("image/*".toMediaTypeOrNull()))
            val response = chatApi.uploadImage(body)
            if (response.isSuccessful && response.body() != null) Result.success(response.body()!!)
            else Result.failure(Exception("Upload failed"))
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun handleNewMessage(message: MessageEntity) {
        messageDao.insertMessage(message)
    }
}
