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
import com.example.fooddelivery.data.local.datastore.DataStoreManager
import com.example.fooddelivery.domain.usecase.RegisterDeviceTokenUseCase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class DFoodMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var registerDeviceTokenUseCase: RegisterDeviceTokenUseCase

    @Inject
    lateinit var dataStoreManager: DataStoreManager

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Refreshed token: $token")
        serviceScope.launch {
            registerDeviceTokenUseCase.registerToken(token)
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d(TAG, "From: ${message.from}")

        serviceScope.launch {
            val notificationsEnabled = dataStoreManager.readNotificationsState().first()
            if (!notificationsEnabled) return@launch

            val title: String
            val body: String
            when {
                message.notification != null -> {
                    title = message.notification?.title ?: "DFood"
                    body = message.notification?.body ?: ""
                }
                message.data.isNotEmpty() -> {
                    title = message.data["title"] ?: "DFood"
                    body = message.data["body"] ?: ""
                }
                else -> return@launch
            }

            sendNotification(title, body)
        }
    }

    private fun sendNotification(title: String, messageBody: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = "dfood_notifications"
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
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

        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }

    companion object {
        private const val TAG = "DFoodMessagingService"
    }
}
