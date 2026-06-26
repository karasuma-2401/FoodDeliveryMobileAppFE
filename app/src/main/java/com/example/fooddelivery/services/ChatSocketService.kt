package com.example.fooddelivery.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.example.fooddelivery.data.local.room.entity.MessageEntity
import com.example.fooddelivery.data.remote.socket.ChatSocketManager
import com.example.fooddelivery.domain.repository.ChatRepository
import dagger.hilt.android.AndroidEntryPoint
import io.socket.client.Socket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject

@AndroidEntryPoint
class ChatSocketService : Service() {

    @Inject
    lateinit var chatSocketManager: ChatSocketManager

    @Inject
    lateinit var chatRepository: ChatRepository

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var socket: Socket

    override fun onCreate() {
        super.onCreate()
        socket = chatSocketManager.getSocket()
        setupSocketListeners()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        socket = chatSocketManager.getSocket()
        chatSocketManager.connectIfNeeded()
        return START_STICKY
    }

    private fun setupSocketListeners() {
        socket.on("text-chat") { args ->
            val data = parseSocketPayload(args) ?: return@on
            try {
                val message = MessageEntity(
                    id = data.opt("id")?.toString() ?: System.currentTimeMillis().toString(),
                    conversationId = data.opt("conversationId")?.toString() ?: return@on,
                    senderId = data.opt("senderId")?.toString() ?: return@on,
                    content = data.optString("content", ""),
                    imageUrl = data.optString("image", null)?.takeIf { it.isNotBlank() },
                    createdAt = data.optString("createdAt", System.currentTimeMillis().toString()),
                    isSending = false,
                    isFailed = false,
                    isRead = data.optBoolean("isRead", false)
                )
                serviceScope.launch {
                    chatRepository.handleNewMessage(message)
                }
            } catch (e: Exception) {
                Log.e("ChatSocketService", "Error parsing text-chat: ${e.message}")
            }
        }
        socket.on("exception") { args ->
            val data = args.getOrNull(0) as? JSONObject
            val status = data?.optString("status")
            val content = data?.optString("content")
            Log.e("ChatSocketService", "Socket Exception: [$status] $content")
        }
        
        socket.on(Socket.EVENT_CONNECT) {
            Log.d("ChatSocketService", "Socket Connected")
        }
        
        socket.on(Socket.EVENT_DISCONNECT) {
            Log.d("ChatSocketService", "Socket Disconnected")
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        socket.off("text-chat")
        socket.off("exception")
        socket.disconnect()
        serviceScope.cancel()
        super.onDestroy()
    }

    private fun parseSocketPayload(args: Array<Any>): JSONObject? {
        val root = args.getOrNull(0) as? JSONObject ?: return null
        return if (root.has("data") && root.optString("status") == "success") {
            root.optJSONObject("data")
        } else {
            root
        }
    }
}
