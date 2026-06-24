package com.example.fooddelivery.data.repository

import com.example.fooddelivery.data.local.room.dao.ConversationDao
import com.example.fooddelivery.data.local.room.dao.MessageDao
import com.example.fooddelivery.data.local.room.entity.ConversationEntity
import com.example.fooddelivery.data.local.room.entity.MessageEntity
import com.example.fooddelivery.data.remote.api.ChatApi
import com.example.fooddelivery.data.remote.parseErrorMessage
import com.example.fooddelivery.data.remote.dto.ConversationDto
import com.example.fooddelivery.data.remote.dto.CreateConversationRequest
import com.example.fooddelivery.data.remote.dto.OtherUserDto
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
                val entities = response.body()!!.conversations.map { dto -> mapToEntity(dto) }
                entities.forEach { conversationDao.updateConversation(it) }
                Result.success(Unit)
            } else Result.failure(Exception(response.parseErrorMessage("Sync failed")))
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun createConversation(orderId: Int, sellerId: Int): Result<ConversationEntity> {
        return try {
            val response = chatApi.createConversation(CreateConversationRequest(orderId, sellerId))
            if (response.isSuccessful && response.body() != null) {
                val dto = response.body()!!
                val entity = mapToEntity(dto)
                conversationDao.updateConversation(entity)
                Result.success(entity)
            } else Result.failure(Exception(response.parseErrorMessage("Create conversation failed")))
        } catch (e: Exception) { Result.failure(e) }
    }

    private fun mapToEntity(dto: ConversationDto, otherUser: OtherUserDto? = null): ConversationEntity {
        val other = otherUser ?: dto.other
        return ConversationEntity(
            id = dto.id.toString(),
            restaurantName = other?.name ?: "User #${other?.id ?: dto.sellerId}",
            restaurantImage = other?.avatar ?: "",
            lastMessage = dto.lastMessage?.content ?: "",
            lastMessageTime = dto.lastMessage?.createdAt ?: dto.createdAt,
            unreadCount = dto.unreadCount
        )
    }

    override suspend fun markAsRead(conversationId: String): Result<Unit> {
        return try {
            conversationDao.markConversationAsRead(conversationId)
            messageDao.markMessagesAsRead(conversationId)
            val response = chatApi.markAsRead(conversationId.toInt())
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.parseErrorMessage("Mark as read failed")))
            }
        } catch (e: Exception) { Result.failure(e) }
    }

    override fun getMessages(conversationId: String): Flow<List<MessageEntity>> = messageDao.getMessages(conversationId)

    override suspend fun syncMessages(conversationId: String, page: Int): Result<Unit> {
        return syncConversationDetail(conversationId.toInt(), page)
    }

    override suspend fun syncConversationDetail(conversationId: Int, page: Int): Result<Unit> {
        return try {
            val response = chatApi.getConversationDetail(conversationId, limit = 20, offset = page * 20)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                
                val convEntity = mapToEntity(body.conversation, body.other)
                conversationDao.updateConversation(convEntity)

                val messageEntities = body.messages.map { dto ->
                    MessageEntity(
                        id = dto.id.toString(),
                        conversationId = dto.conversationId.toString(),
                        senderId = dto.senderId.toString(),
                        content = dto.content,
                        imageUrl = dto.imageUrl,
                        createdAt = dto.createdAt,
                        isSending = false,
                        isFailed = false,
                        isRead = dto.isRead
                    )
                }
                messageDao.insertMessages(messageEntities)
                Result.success(Unit)
            } else Result.failure(Exception(response.parseErrorMessage("Sync detail failed")))
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun syncConversationDetailByOrder(orderId: Int, page: Int): Result<Unit> {
        return try {
            val response = chatApi.getConversationDetailByOrder(orderId, limit = 20, offset = page * 20)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                
                val convEntity = mapToEntity(body.conversation, body.other)
                conversationDao.updateConversation(convEntity)

                val messageEntities = body.messages.map { dto ->
                    MessageEntity(
                        id = dto.id.toString(),
                        conversationId = dto.conversationId.toString(),
                        senderId = dto.senderId.toString(),
                        content = dto.content,
                        imageUrl = dto.imageUrl,
                        createdAt = dto.createdAt,
                        isSending = false,
                        isFailed = false,
                        isRead = dto.isRead
                    )
                }
                messageDao.insertMessages(messageEntities)
                Result.success(Unit)
            } else Result.failure(Exception(response.parseErrorMessage("Sync detail by order failed")))
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
                put("conversationId", message.conversationId.toInt())
                put("content", message.content)
                if (message.imageUrl != null) {
                    put("image", message.imageUrl)
                }
            }
            socket.emit("text-chat", json)
            messageDao.updateMessage(message.copy(isSending = false))
            Result.success(Unit)
        } catch (e: Exception) {
            messageDao.updateMessage(message.copy(isSending = false, isFailed = true))
            Result.failure(e)
        }
    }

    override suspend fun joinRoom(conversationId: String) {
        try {
            val data = JSONObject().put("conversationId", conversationId.toInt())
            socket.emit("join-room", data)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun leaveRoom(conversationId: String) {
        try {
            val data = JSONObject().put("conversationId", conversationId.toInt())
            socket.emit("leave-room", data)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun uploadImage(imagePath: String): Result<String> {
        return try {
            val file = File(imagePath)
            val body = MultipartBody.Part.createFormData("file", file.name, file.asRequestBody("image/*".toMediaTypeOrNull()))
            val response = chatApi.uploadImage(body)
            if (response.isSuccessful && response.body() != null) Result.success(response.body()!!.imageUrl)
            else Result.failure(Exception(response.parseErrorMessage("Upload failed")))
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun handleNewMessage(message: MessageEntity) {
        messageDao.insertMessage(message)
    }
}
