package com.example.messengerapp.notifications

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import androidx.core.app.NotificationCompat
import com.example.messengerapp.MessageActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessaging : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val sent = message.data["sent"]

        val user = message.data["user"]

        val sharedPref = getSharedPreferences("PREFS", Context.MODE_PRIVATE)

        val currentOnlineUser = sharedPref.getString("currentUser", "none")

        val firebaseUser = FirebaseAuth.getInstance().currentUser

        if (firebaseUser != null && sent == firebaseUser.uid) {

            if (currentOnlineUser != user) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    sendOreoNotification(message)
                } else {
                    sendNotification(message)
                }
            }
        }
    }

    private fun sendNotification(message: RemoteMessage) {
        val user = message.data["user"]
        val icon = message.data["icon"]
        val title = message.data["title"]
        val body = message.data["body"]

        val notification = message.notification
        val j = user!!.replace("[\\D]".toRegex(), "").toInt()
        val intent = Intent(this, MessageActivity::class.java)

        val bundle = Bundle()
        bundle.putString("userid", user)
        intent.putExtras(bundle)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val pendingIntent = PendingIntent.getActivity(this, j, intent, PendingIntent.FLAG_ONE_SHOT)

        val defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val builder: NotificationCompat.Builder = NotificationCompat.Builder(this)
            .setSmallIcon(icon!!.toInt())
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setSound(defaultSound)
            .setContentIntent(pendingIntent)

        val notif = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        var i = 0
        when {
            j > 0 -> {
                i = j
            }
        }
        notif.notify(i, builder.build())
    }

    private fun sendOreoNotification(message: RemoteMessage) {
        val user = message.data["user"]
        val icon = message.data["icon"]
        val title = message.data["title"]
        val body = message.data["body"]

        val notification = message.notification
        val j = user!!.replace("[\\D]".toRegex(), "").toInt()
        val intent = Intent(this, MessageActivity::class.java)

        val bundle = Bundle()
        bundle.putString("userid", user)
        intent.putExtras(bundle)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val pendingIntent = PendingIntent.getActivity(this, j, intent, PendingIntent.FLAG_ONE_SHOT)

        val defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val oreoNotification = OreoNotification(this)

        val builder: Notification.Builder = oreoNotification.getOreoNotification(title, body, pendingIntent, defaultSound, icon)

        var i = 0
        when {
            j > 0 -> {
                i = j
            }
        }

        oreoNotification.getManager.notify(i, builder.build())
    }
}