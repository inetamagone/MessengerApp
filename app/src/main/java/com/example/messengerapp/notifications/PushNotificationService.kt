package com.example.messengerapp.notifications

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationManagerCompat
import com.example.messengerapp.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class PushNotificationService: FirebaseMessagingService() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onMessageReceived(message: RemoteMessage) {
        val title = message.notification?.title
        val text = message.notification?.body
        val channelId = "Message"
        val name: CharSequence
        val channel = NotificationChannel(channelId, "Message Notification", NotificationManager.IMPORTANCE_HIGH)
        getSystemService(NotificationManager::class.java)
            .createNotificationChannel(channel)
        val context: Context
        val notification = Notification.Builder(this, channelId)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setAutoCancel(true)
        NotificationManagerCompat.from(this).notify(1, notification.build())
        super.onMessageReceived(message)
    }
}