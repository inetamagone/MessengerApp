package com.example.messengerapp.notifications

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.StrictMode
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationManagerCompat
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.messengerapp.R
import com.example.messengerapp.model.NotificationToken
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.json.JSONException
import org.json.JSONObject

private const val TAG = "PushNotificationService"

class PushNotificationService : FirebaseMessagingService() {

    private val baseUrl = "https://fcm.googleapis.com/fcm/send"
    private val serverKey = "key=AAAAMnDAYuI:APA91bHGjDFgLS9fptL1_vmi6uGj0Sdk4Brj6y_FNA0JmeTwDzLltlFCl33KQk4UwjOemu8GKV2ATAcx1M3gtPuAwLMlWHK9l4f0Qfj6MTih644kaZXFFLiWI9ZCuhyW9X0L9i-JPgjb"
    private lateinit var newToken: String

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val refreshToken = FirebaseMessaging.getInstance().token.toString()

        if (firebaseUser != null) {
            updateToken(refreshToken)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onMessageReceived(message: RemoteMessage) {
        val title = message.notification?.title
        val text = message.notification?.body
        val channelId = "com.example.messengerapp.notifications"
        val name = "Message Notification"

        val channel = NotificationChannel(channelId, name, NotificationManager.IMPORTANCE_HIGH)
        getSystemService(NotificationManager::class.java)
            .createNotificationChannel(channel)
        //val context: Context

        val notification = Notification.Builder(this, channelId)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setAutoCancel(true)
        NotificationManagerCompat.from(this).notify(1, notification.build())

        pushNotification(applicationContext, newToken, title, text)
    }


    private fun pushNotification(context: Context, token: String, title: String?, text: String?) {
        val policy = StrictMode.ThreadPolicy.Builder()
            .permitAll()
            .build()
        StrictMode.setThreadPolicy(policy)

        try {
            val json = JSONObject()
            json.put("to", token)

            val notification = JSONObject()
            notification.put("title", title)
            notification.put("text", text)

            json.put("notification", notification)

            val jsonObjectRequest: JsonObjectRequest = object : JsonObjectRequest(
                Method.POST, baseUrl, json, Response.Listener { response: JSONObject ->
                Log.d(TAG, "onResponse: $response")
            },
                Response.ErrorListener {
                    Log.d(TAG, "onError: $it")
                }) {
                override fun getHeaders(): MutableMap<String, String> {
                    val map: MutableMap<String, String> = HashMap()

                    map["Authorization"] = serverKey
                    map["Content-type"] = "application/json"
                    return map
                }

                override fun getBodyContentType(): String {
                    return "application/json"
                }
            }

            val requestQueue = Volley.newRequestQueue(context)
            jsonObjectRequest.retryPolicy = DefaultRetryPolicy(
                30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
            )
            requestQueue.add(jsonObjectRequest)

        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun updateToken(refreshToken: String?) {
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val ref = FirebaseDatabase.getInstance().reference.child("Tokens")
        val token = NotificationToken(refreshToken!!)
        newToken = ref.child(firebaseUser!!.uid).setValue(token).toString()
    }

}