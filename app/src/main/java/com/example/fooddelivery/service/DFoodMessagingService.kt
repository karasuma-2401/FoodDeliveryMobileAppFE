package com.example.fooddelivery.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.fooddelivery.MainActivity
import com.example.fooddelivery.R
import com.example.fooddelivery.domain.repository.DeviceRepository
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class DFoodMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var deviceRepository: DeviceRepository

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("DFoodMessagingService", "Refreshed token: $token")
        // Register token with server
        serviceScope.launch {
            deviceRepository.registerDevice(token)
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d("DFoodMessagingService", "From: ${message.from}")

        // Check if message contains a notification payload.
        message.notification?.let {
            Log.d("DFoodMessagingService", "Message Notification Body: ${it.body}")
            sendNotification(it.title ?: "DFood", it.body ?: "")
        }

        // Check if message contains a data payload.
        if (message.data.isNotEmpty()) {
            Log.d("DFoodMessagingService", "Message data payload: ${message.data}")
            val title = message.data["title"] ?: "DFood"
            val body = message.data["body"] ?: ""
            sendNotification(title, body)
        }
    }

    private fun sendNotification(title: String, messageBody: String) {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = "dfood_notifications"
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher) // Make sure you have a proper icon
            .setContentTitle(title)
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "DFood Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(0, notificationBuilder.build())
    }
}
