package com.example.fooddelivery.data.remote.socket

import com.example.fooddelivery.BuildConfig
import com.example.fooddelivery.data.local.datastore.TokenManager
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import java.net.URI
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull

@Singleton
class ChatSocketManager @Inject constructor(
    private val tokenManager: TokenManager
) {
    @Volatile
    private var socket: Socket = createSocket()
    private val listenerSetups = mutableListOf<() -> Unit>()
    private val lock = Any()

    fun getSocket(): Socket = socket

    fun isConnected(): Boolean = socket.connected()

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

    suspend fun awaitConnection(timeoutMs: Long = 10_000L): Boolean {
        if (socket.connected()) return true
        connectIfNeeded()
        return withTimeoutOrNull(timeoutMs) {
            suspendCancellableCoroutine { continuation ->
                val currentSocket = socket
                if (currentSocket.connected()) {
                    continuation.resume(true)
                    return@suspendCancellableCoroutine
                }
                val listener = Emitter.Listener {
                    if (continuation.isActive) {
                        continuation.resume(true)
                    }
                }
                currentSocket.once(Socket.EVENT_CONNECT, listener)
                continuation.invokeOnCancellation {
                    currentSocket.off(Socket.EVENT_CONNECT, listener)
                }
            }
        } ?: false
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
