package com.example.fooddelivery.data.repository

import com.example.fooddelivery.data.local.room.dao.ConversationDao
import com.example.fooddelivery.data.local.room.dao.MessageDao
import com.example.fooddelivery.data.local.room.entity.ConversationEntity
import com.example.fooddelivery.data.local.room.entity.MessageEntity
import com.example.fooddelivery.data.remote.api.ChatApi
import com.example.fooddelivery.data.remote.dto.ConversationDto
import com.example.fooddelivery.data.remote.dto.CreateConversationRequest
import com.example.fooddelivery.domain.repository.ChatRepository
import io.socket.client.Ack
import io.socket.client.Socket
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
                val entities = response.body()!!.map { dto -> mapToEntity(dto) }
                entities.forEach { conversationDao.updateConversation(it) }
                Result.success(Unit)
            } else Result.failure(Exception("Sync failed"))
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
            } else Result.failure(Exception("Create conversation failed"))
        } catch (e: Exception) { Result.failure(e) }
    }

    private fun mapToEntity(dto: ConversationDto): ConversationEntity {
        // Tự động nhận diện tên/ảnh đối phương dựa trên role (BE trả về cái nào thì dùng cái đó)
        val name = dto.sellerName ?: dto.customerName ?: "User #${dto.sellerId}/${dto.customerId}"
        val image = dto.sellerImage ?: dto.customerImage ?: ""
        
        return ConversationEntity(
            id = dto.id.toString(),
            restaurantName = name, // Trong entity đặt tên là restaurantName nhưng có thể hiểu là "Đối phương"
            restaurantImage = image,
            lastMessage = dto.lastMessage?.content ?: "",
            lastMessageTime = dto.lastMessage?.createdAt ?: dto.createdAt,
            unreadCount = dto.unreadCount ?: 0
        )
    }

    override suspend fun markAsRead(conversationId: String): Result<Unit> {
        return try {
            conversationDao.markConversationAsRead(conversationId)
            val ackResult = withTimeoutOrNull(5000L) {
                suspendCancellableCoroutine { continuation ->
                    val data = JSONObject().put("conversationId", conversationId.toInt())
                    socket.emit("mark_read", arrayOf(data), object : Ack {
                        override fun call(vararg args: Any?) {
                            val response = args.getOrNull(0) as? JSONObject
                            val success = response?.optBoolean("success", false) ?: false
                            if (success) {
                                continuation.resume(Result.success(Unit))
                            } else {
                                continuation.resume(Result.failure(Exception("Mark read failed")))
                            }
                        }
                    })
                }
            }
            ackResult ?: Result.failure(Exception("Mark read timeout"))
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
                
                val convEntity = mapToEntity(body.conversation)
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
                        isFailed = false
                    )
                }
                messageDao.insertMessages(messageEntities)
                Result.success(Unit)
            } else Result.failure(Exception("Sync detail failed"))
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun syncConversationDetailByOrder(orderId: Int, page: Int): Result<Unit> {
        return try {
            val response = chatApi.getConversationDetailByOrder(orderId, limit = 20, offset = page * 20)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                
                val convEntity = mapToEntity(body.conversation)
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
                        isFailed = false
                    )
                }
                messageDao.insertMessages(messageEntities)
                Result.success(Unit)
            } else Result.failure(Exception("Sync detail by order failed"))
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
                put("image", message.imageUrl)
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

    override suspend fun uploadImage(imagePath: String): Result<String> {
        return try {
            val file = File(imagePath)
            val body = MultipartBody.Part.createFormData("file", file.name, file.asRequestBody("image/*".toMediaTypeOrNull()))
            val response = chatApi.uploadImage(body)
            if (response.isSuccessful && response.body() != null) Result.success(response.body()!!.imageUrl)
            else Result.failure(Exception("Upload failed"))
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun handleNewMessage(message: MessageEntity) {
        messageDao.insertMessage(message)
    }
}
