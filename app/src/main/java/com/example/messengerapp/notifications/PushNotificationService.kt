package com.example.messengerapp.notifications

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.messengerapp.R
import com.example.messengerapp.utils.FcmUtils
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.util.*

private const val TAG = "PushNotificationService"

class PushNotificationService : FirebaseMessagingService() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        pushNotification(message)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun pushNotification(message: RemoteMessage) {
        val channelId = "com.example.messengerapp.notifications"
        val channelName = "Message Notification"

        val title = message.notification?.title
        val text = message.notification?.body
        val clickAction = message.notification?.clickAction

        val intent = Intent(clickAction)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        @SuppressLint("UnspecifiedImmutableFlag")
            val pendingIntent = PendingIntent
            .getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)

        val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH)
        getSystemService(NotificationManager::class.java)
            .createNotificationChannel(channel)

        val builder = NotificationCompat.Builder(this, channelId)
            .setContentIntent(pendingIntent)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(text)
            .setColor(ContextCompat.getColor(this, android.R.color.transparent))
            .setAutoCancel(true)

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                channelId, channelName, message.priority
            )

            builder.setChannelId(channelId)
            notificationManager.createNotificationChannel(notificationChannel)
        }

//        val policy = StrictMode.ThreadPolicy.Builder()
//            .permitAll()
//            .build()
//        StrictMode.setThreadPolicy(policy)

        val notification = builder.build()
        // Keep multiple notifications
        val notificationId = Random().nextInt()
        notificationManager.notify(notificationId, notification)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        FcmUtils().sendRegistrationToServer(token)
        Log.d(TAG, "Refreshed token: $token")
    }

}