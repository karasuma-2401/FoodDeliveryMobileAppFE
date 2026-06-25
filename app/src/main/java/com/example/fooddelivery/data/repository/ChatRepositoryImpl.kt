package com.example.fooddelivery.data.repository

import android.util.Log
import com.example.fooddelivery.data.local.datastore.TokenManager
import com.example.fooddelivery.data.local.room.dao.ConversationDao
import com.example.fooddelivery.data.local.room.dao.MessageDao
import com.example.fooddelivery.data.local.room.entity.ConversationEntity
import com.example.fooddelivery.data.local.room.entity.MessageEntity
import com.example.fooddelivery.data.remote.api.ChatApi
import com.example.fooddelivery.data.remote.dto.ConversationDto
import com.example.fooddelivery.data.remote.dto.CreateConversationRequest
import com.example.fooddelivery.data.remote.dto.OtherUserDto
import com.example.fooddelivery.data.remote.MediaUrlResolver
import com.example.fooddelivery.data.remote.unwrapData
import com.example.fooddelivery.data.remote.unwrapUnit
import com.example.fooddelivery.domain.repository.ChatRepository
import io.socket.client.Socket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
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
    private val socket: Socket,
    private val tokenManager: TokenManager
) : ChatRepository {

    private val socketScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var socketListenersAttached = false

    init {
        ensureSocketReady()
    }

    private fun ensureSocketReady() {
        synchronized(this) {
            if (!socketListenersAttached) {
                attachSocketListeners()
                socketListenersAttached = true
            }
        }
        if (!socket.connected()) {
            socket.connect()
        }
    }

    private fun attachSocketListeners() {
        socket.on("text-chat") { args ->
            val data = args.getOrNull(0) as? JSONObject ?: return@on
            try {
                val message = MessageEntity(
                    id = data.opt("id")?.toString()
                        ?: System.currentTimeMillis().toString(),
                    conversationId = data.opt("conversationId")?.toString() ?: return@on,
                    senderId = data.opt("senderId")?.toString() ?: return@on,
                    content = data.optString("content", ""),
                    imageUrl = data.optString("image", null)?.takeIf { it.isNotBlank() }
                        ?.let { MediaUrlResolver.resolve(it) },
                    createdAt = data.optString("createdAt", System.currentTimeMillis().toString()),
                    isSending = false,
                    isFailed = false,
                    isRead = data.optBoolean("isRead", false)
                )
                socketScope.launch {
                    handleNewMessage(message)
                }
            } catch (e: Exception) {
                Log.e("ChatRepository", "Error parsing text-chat: ${e.message}")
            }
        }
        socket.on("exception") { args ->
            val data = args.getOrNull(0) as? JSONObject
            Log.e(
                "ChatRepository",
                "Socket exception: [${data?.optString("status")}] ${data?.optString("content")}"
            )
        }
    }

    override fun getConversations(): Flow<List<ConversationEntity>> = conversationDao.getConversations()

    override suspend fun syncConversations(): Result<Unit> {
        return try {
            ensureSocketReady()
            chatApi.getConversations()
                .unwrapData("Sync failed")
                .mapCatching { conversations ->
                    val entities = conversations.map { dto -> mapToEntity(dto) }
                    entities.forEach { conversationDao.updateConversation(it) }
                }
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun createConversation(orderId: Int, sellerId: Int): Result<ConversationEntity> {
        return try {
            chatApi.createConversation(CreateConversationRequest(orderId, sellerId))
                .unwrapData("Create conversation failed")
                .mapCatching { dto ->
                    val entity = mapToEntity(dto)
                    conversationDao.updateConversation(entity)
                    entity
                }
        } catch (e: Exception) { Result.failure(e) }
    }

    private fun mapToEntity(dto: ConversationDto, otherUser: OtherUserDto? = null): ConversationEntity {
        val display = otherUser
            ?: dto.other
            ?: dto.restaurant?.let { OtherUserDto(it.id, it.name, it.image) }
            ?: dto.customer
            ?: dto.seller
        return ConversationEntity(
            id = dto.id.toString(),
            restaurantName = display?.name ?: "User #${dto.sellerId}",
            restaurantImage = display?.avatar ?: "",
            lastMessage = dto.lastMessage?.content ?: "",
            lastMessageTime = dto.lastMessage?.createdAt ?: dto.updatedAt ?: dto.createdAt,
            unreadCount = dto.unreadCount
        )
    }

    override suspend fun markAsRead(conversationId: String): Result<Unit> {
        return try {
            conversationDao.markConversationAsRead(conversationId)
            messageDao.markMessagesAsRead(conversationId)
            chatApi.markAsRead(conversationId.toInt()).unwrapUnit("Mark as read failed")
        } catch (e: Exception) { Result.failure(e) }
    }

    override fun getMessages(conversationId: String): Flow<List<MessageEntity>> = messageDao.getMessages(conversationId)

    override suspend fun syncMessages(conversationId: String, page: Int): Result<Unit> {
        return syncConversationDetail(conversationId.toInt(), page)
    }

    override suspend fun syncConversationDetail(conversationId: Int, page: Int): Result<Unit> {
        return try {
            chatApi.getConversationDetail(conversationId, limit = 20, offset = page * 20)
                .unwrapData("Sync detail failed")
                .mapCatching { body ->
                    val convEntity = mapToEntity(body.conversation, body.other)
                    conversationDao.updateConversation(convEntity)

                    val messageEntities = body.messages.map { dto ->
                        MessageEntity(
                            id = dto.id.toString(),
                            conversationId = dto.conversationId.toString(),
                            senderId = dto.senderId.toString(),
                            content = dto.content,
                            imageUrl = dto.imageUrl?.let { MediaUrlResolver.resolve(it) },
                            createdAt = dto.createdAt,
                            isSending = false,
                            isFailed = false,
                            isRead = dto.isRead
                        )
                    }
                    messageDao.insertMessages(messageEntities)
                }
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun syncConversationDetailByOrder(orderId: Int, page: Int): Result<Unit> {
        return try {
            chatApi.getConversationDetailByOrder(orderId, limit = 20, offset = page * 20)
                .unwrapData("Sync detail by order failed")
                .mapCatching { body ->
                    val convEntity = mapToEntity(body.conversation, body.other)
                    conversationDao.updateConversation(convEntity)

                    val messageEntities = body.messages.map { dto ->
                        MessageEntity(
                            id = dto.id.toString(),
                            conversationId = dto.conversationId.toString(),
                            senderId = dto.senderId.toString(),
                            content = dto.content,
                            imageUrl = dto.imageUrl?.let { MediaUrlResolver.resolve(it) },
                            createdAt = dto.createdAt,
                            isSending = false,
                            isFailed = false,
                            isRead = dto.isRead
                        )
                    }
                    messageDao.insertMessages(messageEntities)
                }
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
            ensureSocketReady()
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
            ensureSocketReady()
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

    override suspend fun uploadAndSendImage(
        conversationId: String,
        senderId: String,
        localImagePath: String
    ): Result<Unit> {
        val tempId = UUID.randomUUID().toString()
        val localPreviewUrl = File(localImagePath).toURI().toString()
        var message = MessageEntity(
            id = tempId,
            conversationId = conversationId,
            content = "",
            imageUrl = localPreviewUrl,
            createdAt = System.currentTimeMillis().toString(),
            isSending = true,
            isFailed = false,
            senderId = senderId
        )
        messageDao.insertMessage(message)

        val remoteUrl = uploadImage(localImagePath).getOrElse { error ->
            messageDao.updateMessage(message.copy(isSending = false, isFailed = true))
            return Result.failure(error)
        }

        message = message.copy(imageUrl = remoteUrl, isSending = false, isFailed = false)
        messageDao.updateMessage(message)

        return try {
            ensureSocketReady()
            val json = JSONObject().apply {
                put("conversationId", conversationId.toInt())
                put("content", "")
                put("image", remoteUrl)
            }
            socket.emit("text-chat", json)
            Result.success(Unit)
        } catch (e: Exception) {
            messageDao.updateMessage(message.copy(isFailed = true))
            Result.failure(e)
        }
    }

    override suspend fun uploadImage(imagePath: String): Result<String> {
        return try {
            val authorization = tokenManager.bearerToken()
                ?: return Result.failure(Exception("Unauthorized: Please login again"))
            val file = File(imagePath)
            if (!file.exists()) {
                return Result.failure(Exception("Image file not found"))
            }
            val mimeType = when (file.extension.lowercase()) {
                "png" -> "image/png"
                "webp" -> "image/webp"
                "gif" -> "image/gif"
                else -> "image/jpeg"
            }
            val body = MultipartBody.Part.createFormData(
                "file",
                file.name,
                file.asRequestBody(mimeType.toMediaTypeOrNull())
            )
            chatApi.uploadImage(authorization, body)
                .unwrapData("Upload failed")
                .mapCatching { response ->
                    response.resolveUrl()?.let { MediaUrlResolver.resolve(it) }
                        ?: throw Exception("Upload response missing image URL")
                }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun handleNewMessage(message: MessageEntity) {
        messageDao.insertMessage(message)
    }
}
