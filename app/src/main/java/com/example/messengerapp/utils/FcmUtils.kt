package com.example.messengerapp.utils

import android.content.Context
import android.util.Log
import com.example.messengerapp.model.Message
import com.example.messengerapp.model.Notification
import com.example.messengerapp.model.NotificationToken
import com.example.messengerapp.network.ApiConfig
import com.example.messengerapp.network.FcmResponse
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessaging
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private const val TAG = "FcmUtils"

class FcmUtils {

    private val intentAction = "OPEN_APP"

    fun registerToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task: Task<String> ->
            if (task.isSuccessful) {
                val token = task.result
                sendRegistrationToServer(token)
            } else {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
            }
        }
    }

    fun sendNotification(
        context: Context,
        recipientId: String, notificationTitle: String, notificationMessage: String) {
        Log.d(TAG, "sendNotification called")
        val reference = FirebaseDatabase.getInstance().getReference("Tokens")
        // Get recipient token based on user id
        val query = reference.orderByKey().equalTo(recipientId)
        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                Log.d(
                    TAG,
                    "sendNotification onDataChange: success get recipient token (${dataSnapshot.children})"
                )
                dataSnapshot.children.forEach { snapshot ->
                    val token = snapshot.getValue(NotificationToken::class.java)
                    val notification = Notification(
                        notificationTitle, notificationMessage,
                        "high", intentAction
                    )
                    val message = Message(notification, token?.getToken().orEmpty())

                    val client = ApiConfig.getApiService(context).sendNotification(message)
                    client.enqueue(object : Callback<FcmResponse?> {
                        override fun onResponse(
                            call: Call<FcmResponse?>,
                            response: Response<FcmResponse?>,
                        ) {
                            if (response.code() == 200 && response.body() != null) {
                                if (response.body()!!.success == 1) {
                                    Log.d(TAG, "sendNotification onResponse: notification sent")
                                }
                            } else {
                                Log.e(TAG, "sendNotification onResponse: error code ${response.code()}"
                                )
                            }
                        }

                        override fun onFailure(call: Call<FcmResponse?>, t: Throwable) {
                            Log.e(TAG, "sendNotification onFailure", t)
                        }
                    })
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "sendNotification onCancelled", error.toException())
            }
        })
    }

    // Store latest token to database server
    fun sendRegistrationToServer(newToken: String) {
        Log.d(TAG, "sendRegistrationToServer called: $newToken")
        // This token is required as a notification receiver
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        if (firebaseUser != null) {
            val databaseReference = FirebaseDatabase.getInstance().getReference("Tokens")
            val token = NotificationToken(newToken)
            databaseReference.child(firebaseUser.uid)
                .setValue(token)
                .addOnSuccessListener {
                    Log.d(TAG, "sendRegistrationToServer onSuccess")
                }
                .addOnFailureListener {
                    Log.e(TAG, "sendRegistrationToServer onFailure", it)
                }
        } else Log.w(TAG, "sendRegistrationToServer cancelled: no user logged in")
    }

}