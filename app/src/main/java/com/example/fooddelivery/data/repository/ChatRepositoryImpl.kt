package com.example.fooddelivery.data.repository

import android.util.Log
import com.example.fooddelivery.data.local.datastore.TokenManager
import com.example.fooddelivery.data.local.room.dao.ConversationDao
import com.example.fooddelivery.data.local.room.dao.MessageDao
import com.example.fooddelivery.data.local.room.entity.ConversationEntity
import com.example.fooddelivery.data.local.room.entity.MessageEntity
import com.example.fooddelivery.data.remote.api.ChatApi
import com.example.fooddelivery.data.remote.dto.ConversationDetailDto
import com.example.fooddelivery.data.remote.dto.ConversationDto
import com.example.fooddelivery.data.remote.dto.CreateConversationRequest
import com.example.fooddelivery.data.remote.dto.OtherUserDto
import com.example.fooddelivery.data.remote.MediaUrlResolver
import com.example.fooddelivery.data.remote.socket.ChatSocketManager
import com.example.fooddelivery.data.remote.unwrapData
import com.example.fooddelivery.data.remote.unwrapUnit
import com.example.fooddelivery.domain.repository.ChatRepository
import com.example.fooddelivery.util.messageCreatedAtMillis
import com.example.fooddelivery.util.normalizeCreatedAt
import com.example.fooddelivery.util.normalizeCreatedAtNow
import com.example.fooddelivery.util.senderIdsMatch
import io.socket.client.Socket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
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
    private val tokenManager: TokenManager,
    private val chatSocketManager: ChatSocketManager
) : ChatRepository {

    private val socketScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val sendTimeoutMs = 15_000L
    @Volatile
    private var activeConversationId: String? = null

    init {
        chatSocketManager.onSocketReplaced { attachSocketListeners() }
        ensureSocketReady()
    }

    private fun ensureSocketReady() {
        chatSocketManager.connectIfNeeded()
    }

    private fun socket(): Socket = chatSocketManager.getSocket()

    private fun attachSocketListeners() {
        val socket = socket()
        socket.off(Socket.EVENT_CONNECT)
        socket.off("text-chat")
        socket.off("exception")
        socket.on(Socket.EVENT_CONNECT) {
            activeConversationId?.let { conversationId ->
                socketScope.launch {
                    emitJoinRoom(conversationId)
                }
            }
        }
        socket.on("text-chat") { args ->
            val data = parseSocketPayload(args) ?: return@on
            try {
                val message = MessageEntity(
                    id = data.opt("id")?.toString()
                        ?: System.currentTimeMillis().toString(),
                    conversationId = data.opt("conversationId")?.toString() ?: return@on,
                    senderId = data.opt("senderId")?.toString() ?: return@on,
                    content = data.optString("content", ""),
                    imageUrl = data.optString("image", null)?.takeIf { it.isNotBlank() }
                        ?.let { MediaUrlResolver.resolve(it) },
                    createdAt = normalizeCreatedAt(
                        data.optString("createdAt", normalizeCreatedAtNow())
                    ),
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
            socketScope.launch {
                messageDao.markOptimisticSendingAsFailed()
            }
        }
    }

    override fun getConversations(): Flow<List<ConversationEntity>> = conversationDao.getConversations()

    override suspend fun syncConversations(): Result<Unit> {
        return try {
            ensureSocketReady()
            chatApi.getConversations()
                .unwrapData("Sync failed")
                .mapCatching { conversations ->
                    val currentUserId = getCurrentUserId()
                    val entities = conversations.map { dto -> mapToEntity(dto, currentUserId) }
                    conversationDao.replaceAll(entities)
                }
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun createConversation(sellerId: Int): Result<ConversationEntity> {
        return try {
            chatApi.createConversation(CreateConversationRequest(sellerId))
                .unwrapData("Create conversation failed")
                .mapCatching { dto ->
                    val entity = mapToEntity(dto)
                    conversationDao.updateConversation(entity)
                    entity
                }
        } catch (e: Exception) { Result.failure(e) }
    }

    private suspend fun getCurrentUserId(): Int? = tokenManager.getUserId.first()

    private suspend fun mapToEntity(dto: ConversationDto, currentUserId: Int? = null): ConversationEntity {
        val userId = currentUserId ?: getCurrentUserId()
        val display = resolveOtherUser(dto, userId)
        return ConversationEntity(
            id = dto.id.toString(),
            restaurantName = display?.name ?: "User #${dto.sellerId}",
            restaurantImage = display?.avatar ?: "",
            lastMessage = dto.lastMessage?.content ?: "",
            lastMessageTime = dto.lastMessage?.createdAt ?: dto.updatedAt ?: dto.createdAt,
            unreadCount = dto.unreadCount
        )
    }

    private fun resolveOtherUser(dto: ConversationDto, currentUserId: Int?): OtherUserDto? {
        if (currentUserId == null) {
            return dto.restaurant?.let { OtherUserDto(it.id, it.name, it.image) }
                ?: dto.seller
                ?: dto.customer
        }
        return when (currentUserId) {
            dto.customerId -> dto.restaurant?.let { OtherUserDto(it.id, it.name, it.image) }
                ?: dto.seller
            dto.sellerId -> dto.customer
            else -> dto.restaurant?.let { OtherUserDto(it.id, it.name, it.image) }
                ?: dto.seller
                ?: dto.customer
        }
    }

    override suspend fun markAsRead(conversationId: String): Result<Unit> {
        return try {
            conversationDao.markConversationAsRead(conversationId)
            messageDao.markMessagesAsRead(conversationId)
            chatApi.markAsRead(conversationId.toInt()).unwrapUnit("Mark as read failed")
        } catch (e: Exception) { Result.failure(e) }
    }

    override fun getMessages(conversationId: String): Flow<List<MessageEntity>> = messageDao.getMessages(conversationId)

    override suspend fun syncMessages(conversationId: String, page: Int): Result<ConversationEntity> {
        return syncConversationDetail(conversationId.toInt(), page)
    }

    override suspend fun syncConversationDetail(conversationId: Int, page: Int): Result<ConversationEntity> {
        return try {
            chatApi.getConversationDetail(conversationId, limit = 20, offset = page * 20)
                .unwrapData("Sync detail failed")
                .mapCatching { body ->
                    persistConversationDetail(body)
                }
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun syncConversationDetailByOrder(orderId: Int, page: Int): Result<ConversationEntity> {
        return try {
            chatApi.getConversationDetailByOrder(orderId, limit = 20, offset = page * 20)
                .unwrapData("Sync detail by order failed")
                .mapCatching { body ->
                    persistConversationDetail(body)
                }
        } catch (e: Exception) { Result.failure(e) }
    }

    private suspend fun persistConversationDetail(body: ConversationDetailDto): ConversationEntity {
        val convEntity = mapToEntity(body.conversation)
        conversationDao.updateConversation(convEntity)

        val messageEntities = body.messages.map { dto ->
            MessageEntity(
                id = dto.id.toString(),
                conversationId = dto.conversationId.toString(),
                senderId = dto.senderId.toString(),
                content = dto.content,
                imageUrl = dto.imageUrl?.takeIf { it.isNotBlank() }?.let { MediaUrlResolver.resolve(it) },
                createdAt = normalizeCreatedAt(dto.createdAt),
                isSending = false,
                isFailed = false,
                isRead = dto.isRead
            )
        }
        messageDao.insertMessages(messageEntities)
        return convEntity
    }

    private fun parseSocketPayload(args: Array<Any>): JSONObject? {
        val root = args.getOrNull(0) as? JSONObject ?: return null
        return if (root.has("data") && root.optString("status") == "success") {
            root.optJSONObject("data")
        } else {
            root
        }
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
            createdAt = normalizeCreatedAtNow(),
            isSending = true,
            isFailed = false,
            senderId = senderId
        )
        messageDao.insertMessage(message)

        return try {
            ensureSocketReady()
            if (!chatSocketManager.awaitConnection()) {
                messageDao.updateMessage(message.copy(isSending = false, isFailed = true))
                return Result.failure(Exception("Chat connection unavailable"))
            }
            val json = JSONObject().apply {
                put("conversationId", message.conversationId.toInt())
                put("content", message.content)
                if (message.imageUrl != null) {
                    put("image", message.imageUrl)
                }
            }
            socket().emit("text-chat", json)
            scheduleSendTimeout(tempId)
            Result.success(Unit)
        } catch (e: Exception) {
            messageDao.updateMessage(message.copy(isSending = false, isFailed = true))
            Result.failure(e)
        }
    }

    override suspend fun joinRoom(conversationId: String) {
        activeConversationId = conversationId
        emitJoinRoom(conversationId)
    }

    override suspend fun leaveRoom(conversationId: String) {
        if (activeConversationId == conversationId) {
            activeConversationId = null
        }
        try {
            if (chatSocketManager.isConnected()) {
                val data = JSONObject().put("conversationId", conversationId.toInt())
                socket().emit("leave-room", data)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun emitJoinRoom(conversationId: String) {
        try {
            ensureSocketReady()
            if (!chatSocketManager.awaitConnection()) {
                Log.w("ChatRepository", "Socket not connected; will join room $conversationId on reconnect")
                return
            }
            val data = JSONObject().put("conversationId", conversationId.toInt())
            socket().emit("join-room", data)
        } catch (e: Exception) {
            Log.e("ChatRepository", "Failed to join room $conversationId", e)
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
            createdAt = normalizeCreatedAtNow(),
            isSending = true,
            isFailed = false,
            senderId = senderId
        )
        messageDao.insertMessage(message)

        val remoteUrl = uploadImage(localImagePath).getOrElse { error ->
            messageDao.updateMessage(message.copy(isSending = false, isFailed = true))
            return Result.failure(error)
        }

        message = message.copy(imageUrl = remoteUrl)
        messageDao.updateMessage(message)

        return try {
            ensureSocketReady()
            if (!chatSocketManager.awaitConnection()) {
                messageDao.updateMessage(message.copy(isSending = false, isFailed = true))
                return Result.failure(Exception("Chat connection unavailable"))
            }
            val json = JSONObject().apply {
                put("conversationId", conversationId.toInt())
                put("content", "")
                put("image", remoteUrl)
            }
            socket().emit("text-chat", json)
            scheduleSendTimeout(tempId)
            Result.success(Unit)
        } catch (e: Exception) {
            messageDao.updateMessage(message.copy(isSending = false, isFailed = true))
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
        val normalizedMessage = message.copy(createdAt = normalizeCreatedAt(message.createdAt))
        val currentUserId = getCurrentUserId()?.toString()
        val messageToInsert = if (currentUserId != null &&
            senderIdsMatch(normalizedMessage.senderId, currentUserId)
        ) {
            val optimisticCreatedAt = messageDao.getLatestOptimisticCreatedAt(
                conversationId = normalizedMessage.conversationId,
                senderId = normalizedMessage.senderId
            )
            messageDao.deleteOptimisticDuplicates(
                conversationId = normalizedMessage.conversationId,
                senderId = normalizedMessage.senderId,
                serverMessageId = normalizedMessage.id
            )
            preserveClientTimestampIfNewer(normalizedMessage, optimisticCreatedAt)
        } else {
            normalizedMessage
        }
        messageDao.insertMessage(messageToInsert)
    }

    private fun preserveClientTimestampIfNewer(
        serverMessage: MessageEntity,
        optimisticCreatedAt: String?
    ): MessageEntity {
        if (optimisticCreatedAt.isNullOrBlank()) return serverMessage
        val serverMillis = messageCreatedAtMillis(serverMessage.createdAt)
        val clientMillis = messageCreatedAtMillis(optimisticCreatedAt)
        return if (clientMillis > serverMillis) {
            serverMessage.copy(createdAt = clientMillis.toString())
        } else {
            serverMessage
        }
    }

    private fun scheduleSendTimeout(tempId: String) {
        socketScope.launch {
            delay(sendTimeoutMs)
            val pending = messageDao.getMessageById(tempId) ?: return@launch
            if (pending.isSending) {
                messageDao.updateMessage(pending.copy(isSending = false, isFailed = true))
            }
        }
    }
}
