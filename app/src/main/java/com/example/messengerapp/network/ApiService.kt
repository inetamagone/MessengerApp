package com.example.messengerapp.network

import com.example.messengerapp.model.Message
import com.example.messengerapp.utils.SERVER_KEY
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface ApiService {

    @Headers(
        "Content-Type:application/json",
        "Authorization:key=$SERVER_KEY"
    )

    @POST("fcm/send")
    fun sendNotification(@Body body: Message?): Call<FcmResponse?>?
}
