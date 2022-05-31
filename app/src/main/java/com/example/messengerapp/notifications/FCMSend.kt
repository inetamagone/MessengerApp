package com.example.messengerapp.notifications

import android.content.Context
import android.os.StrictMode
import android.util.Log
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException
import org.json.JSONObject

private const val TAG = "FCMSend"

class FCMSend {
    private val baseUrl = "https://fcm.googleapis.com/fcm/send"
    private val serverKey = "key=AAAAMnDAYuI:APA91bHGjDFgLS9fptL1_vmi6uGj0Sdk4Brj6y_FNA0JmeTwDzLltlFCl33KQk4UwjOemu8GKV2ATAcx1M3gtPuAwLMlWHK9l4f0Qfj6MTih644kaZXFFLiWI9ZCuhyW9X0L9i-JPgjb"

    fun pushNotification(context: Context, token: String, title: String, sender: String) {
        val policy = StrictMode.ThreadPolicy.Builder()
            .permitAll()
            .build()
        StrictMode.setThreadPolicy(policy)

        try {
            val json = JSONObject()
            json.put("to", token)

            val notification = JSONObject()
            notification.put("title", title)
            notification.put("sender", sender)

            json.put("notification", notification)

            val jsonObjectRequest: JsonObjectRequest = object : JsonObjectRequest(Method.POST, baseUrl, json, Response.Listener { response: JSONObject ->
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
}