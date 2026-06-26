package com.example.fooddelivery.data.remote.socket

import com.example.fooddelivery.BuildConfig
import com.example.fooddelivery.data.local.datastore.TokenManager
import io.socket.client.IO
import io.socket.client.Socket
import java.net.URI
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatSocketManager @Inject constructor(
    private val tokenManager: TokenManager
) {
    @Volatile
    private var socket: Socket = createSocket()
    private val listenerSetups = mutableListOf<() -> Unit>()
    private val lock = Any()

    fun getSocket(): Socket = socket

    fun onSocketReplaced(setup: () -> Unit) {
        synchronized(lock) {
            listenerSetups.add(setup)
        }
        setup()
    }

    private fun createSocket(): Socket {
        val token = tokenManager.bearerToken()
        val options = IO.Options().apply {
            reconnection = true
            if (!token.isNullOrBlank()) {
                auth = mapOf("token" to token)
            }
        }
        return IO.socket(URI(BuildConfig.SOCKET_URL), options)
    }

    @Synchronized
    fun connectIfNeeded() {
        if (!socket.connected() && !tokenManager.bearerToken().isNullOrBlank()) {
            socket.connect()
        }
    }

    /** Recreate the socket with the latest JWT after login or account switch. */
    @Synchronized
    fun reconnectWithCurrentToken() {
        val previous = socket
        if (previous.connected()) {
            previous.disconnect()
        }
        socket = createSocket()
        synchronized(lock) {
            listenerSetups.forEach { it() }
        }
        connectIfNeeded()
    }

    @Synchronized
    fun disconnect() {
        if (socket.connected()) {
            socket.disconnect()
        }
    }
}
