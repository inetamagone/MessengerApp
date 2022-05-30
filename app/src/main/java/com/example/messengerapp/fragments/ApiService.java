package com.example.messengerapp.fragments;

import com.example.messengerapp.model.NotificationSender;
import com.example.messengerapp.notifications.MyResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface ApiService {

    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAAMnDAYuI:APA91bHGjDFgLS9fptL1_vmi6uGj0Sdk4Brj6y_FNA0JmeTwDzLltlFCl33KQk4UwjOemu8GKV2ATAcx1M3gtPuAwLMlWHK9l4f0Qfj6MTih644kaZXFFLiWI9ZCuhyW9X0L9i-JPgjb"
    })

    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body NotificationSender body);
}
