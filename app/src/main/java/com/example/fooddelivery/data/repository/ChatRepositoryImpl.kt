package com.example.fooddelivery.data.repository

import com.example.fooddelivery.data.local.room.dao.ConversationDao
import com.example.fooddelivery.data.local.room.dao.MessageDao
import com.example.fooddelivery.data.local.room.entity.ConversationEntity
import com.example.fooddelivery.data.local.room.entity.MessageEntity
import com.example.fooddelivery.data.remote.api.ChatApi
import com.example.fooddelivery.domain.repository.ChatRepository
import io.socket.client.Ack
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File
import java.util.UUID
import javax.inject.Inject
import kotlin.coroutines.resume

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
                val entities = response.body()!!.map { dto ->
                    ConversationEntity(
                        id = dto.id,
                        restaurantName = dto.restaurantName,
                        restaurantImage = dto.restaurantImage,
                        lastMessage = dto.lastMessage,
                        lastMessageTime = dto.lastMessageTime,
                        unreadCount = dto.unreadCount
                    )
                }
                entities.forEach { conversationDao.updateConversation(it) }
                Result.success(Unit)
            } else Result.failure(Exception("Sync failed"))
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun markAsRead(conversationId: String): Result<Unit> {
        return try {
            conversationDao.markConversationAsRead(conversationId)
            val ackResult = withTimeoutOrNull(5000L) {
                suspendCancellableCoroutine { continuation ->
                    socket.emit("mark_read", JSONObject().put("conversationId", conversationId)) { args ->
                        val response = args.getOrNull(0) as? JSONObject
                        val success = response?.optBoolean("success", false) ?: false
                        if (success) {
                            continuation.resume(Result.success(Unit))
                        } else {
                            val error = response?.optString("error", "Mark read failed")
                            continuation.resume(Result.failure(Exception(error)))
                        }
                    }
                }
            }
            ackResult ?: Result.failure(Exception("Mark read timeout"))
        } catch (e: Exception) { Result.failure(e) }
    }

    override fun getMessages(conversationId: String): Flow<List<MessageEntity>> = messageDao.getMessages(conversationId)

    override suspend fun syncMessages(conversationId: String, page: Int): Result<Unit> {
        return try {
            val response = chatApi.getMessages(conversationId, limit = 20, offset = page * 20)
            if (response.isSuccessful && response.body() != null) {
                val entities = response.body()!!.map { dto ->
                    MessageEntity(
                        id = dto.id,
                        conversationId = dto.conversationId,
                        senderId = dto.senderId,
                        content = dto.content,
                        imageUrl = dto.imageUrl,
                        createdAt = dto.createdAt,
                        isSending = false,
                        isFailed = false
                    )
                }
                messageDao.insertMessages(entities)
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
            val ackResult = withTimeoutOrNull(5000L) {
                suspendCancellableCoroutine { continuation ->
                    socket.emit("send_message", json) { args ->
                        val response = args.getOrNull(0) as? JSONObject
                        val success = response?.optBoolean("success", false) ?: false
                        if (success) {
                            continuation.resume(Result.success(Unit))
                        } else {
                            val error = response?.optString("error", "Send message failed")
                            continuation.resume(Result.failure(Exception(error)))
                        }
                    }
                }
            }
            if (ackResult != null && ackResult.isSuccess) {
                messageDao.updateMessage(message.copy(isSending = false))
                Result.success(Unit)
            } else {
                messageDao.updateMessage(message.copy(isSending = false, isFailed = true))
                ackResult ?: Result.failure(Exception("Send message timeout"))
            }
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
