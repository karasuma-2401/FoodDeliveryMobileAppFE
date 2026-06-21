package com.example.fooddelivery.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.example.fooddelivery.data.local.room.entity.MessageEntity
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
    lateinit var socket: Socket

    @Inject
    lateinit var chatRepository: ChatRepository

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        setupSocketListeners()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!socket.connected()) {
            socket.connect()
        }
        return START_STICKY
    }

    private fun setupSocketListeners() {
        socket.on("text-chat") { args ->
            val data = args.getOrNull(0) as? JSONObject ?: return@on
            try {
                val message = MessageEntity(
                    id = data.optString("id", System.currentTimeMillis().toString()),
                    conversationId = data.getString("conversationId"),
                    senderId = data.getString("senderId"),
                    content = data.optString("content"),
                    imageUrl = data.optString("image", null),
                    createdAt = data.optString("createdAt", System.currentTimeMillis().toString()),
                    isSending = false,
                    isFailed = false
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
            Log.e("ChatSocketService", "Socket Exception: ${data?.toString()}")
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
}
