package com.example.messengerapp.notifications

import android.content.Context
import android.os.StrictMode
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException
import org.json.JSONObject

class FCMSend {
    private val baseUrl = "https://fcm.googleapis.com/fcm/send"
    private val serverKey = "AAAAMnDAYuI:APA91bHGjDFgLS9fptL1_vmi6uGj0Sdk4Brj6y_FNA0JmeTwDzLltlFCl33KQk4UwjOemu8GKV2ATAcx1M3gtPuAwLMlWHK9l4f0Qfj6MTih644kaZXFFLiWI9ZCuhyW9X0L9i-JPgjb"

    fun pushNotification(context: Context, token: String, title: String, message: String) {
        val policy = StrictMode.ThreadPolicy.Builder()
            .permitAll()
            .build()
        StrictMode.setThreadPolicy(policy)

        val queue = Volley.newRequestQueue(context)

        try {
            val json = JSONObject()
            json.put("to", token)

            val notification = JSONObject()
            notification.put("title", title)
            notification.put("body", message)

            json.put("notification", notification)

            

        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }
}