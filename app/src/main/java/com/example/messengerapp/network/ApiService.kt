package com.example.messengerapp.network

import com.example.messengerapp.model.Message
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

const val SERVER_KEY = "AAAAMnDAYuI:APA91bHGjDFgLS9fptL1_vmi6uGj0Sdk4Brj6y_FNA0JmeTwDzLltlFCl33KQk4UwjOemu8GKV2ATAcx1M3gtPuAwLMlWHK9l4f0Qfj6MTih644kaZXFFLiWI9ZCuhyW9X0L9i-JPgjb"

interface ApiService {

    @Headers(
        "Content-Type:application/json",
        "Authorization:key=$SERVER_KEY"
    )

    @POST("fcm/send")
    fun sendNotification(@Body body: Message?): Call<FcmResponse?>?
}