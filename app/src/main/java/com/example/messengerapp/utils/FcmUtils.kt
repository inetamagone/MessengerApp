package com.example.messengerapp.utils

import android.content.Context
import android.util.Log
import com.example.messengerapp.R
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

fun registerToken(context: Context) {
    FirebaseMessaging.getInstance().token.addOnCompleteListener { task: Task<String> ->
        if (task.isSuccessful) {
            val token = task.result
            sendRegistrationToServer(context, token)
        } else {
            Log.w(TAG, context.getString(R.string.token_fail_message), task.exception)
        }
    }
}

fun sendNotification(context: Context, senderId: String, body: String, receiverId: String) {
    val reference = FirebaseDatabase.getInstance().getReference("Tokens")
    // Get recipient token based on user id
    val query = reference.orderByKey().equalTo(receiverId)
    query.addValueEventListener(object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {

            dataSnapshot.children.forEach { snapshot ->
                val token = snapshot.getValue(NotificationToken::class.java)
                val notificationTitle = context.getString(R.string.new_message)
                val notification = Notification(
                    senderId,
                    notificationTitle,
                    body,
                    "high",
                    "TO_MESSAGES"
                )
                val message = Message(notification, token?.getToken().orEmpty())
                val client = ApiConfig.getApiService(context).sendNotification(message)

                client?.enqueue(object : Callback<FcmResponse?> {
                    override fun onResponse(
                        call: Call<FcmResponse?>,
                        response: Response<FcmResponse?>,
                    ) {
                        when {
                            response.code() == SUCCESS_CODE && response.body() != null -> {

                                when (response.body()!!.success) {
                                    1 -> {
                                        Log.d(TAG, context.getString(R.string.notification_sent))

                                    }
                                    else -> {
                                        Log.d(
                                            TAG,
                                            context.getString(R.string.notification_not_sent)
                                        )
                                    }
                                }
                            }
                            else -> {
                                Log.e(
                                    TAG, context.getString(R.string.send_notification_error)
                                )
                            }
                        }
                    }

                    override fun onFailure(call: Call<FcmResponse?>, t: Throwable) {
                        Log.e(TAG, context.getString(R.string.send_notification_failure), t)
                    }
                })
            }
        }

        override fun onCancelled(error: DatabaseError) {
            Log.e(
                TAG,
                context.getString(R.string.error_notification_cancelled),
                error.toException()
            )
        }
    })
}

// Store latest token to database server
fun sendRegistrationToServer(context: Context, newToken: String) {
    val firebaseUser = FirebaseAuth.getInstance().currentUser
    if (firebaseUser != null) {
        val databaseReference = FirebaseDatabase.getInstance().getReference("Tokens")
        val token = NotificationToken(newToken)
        databaseReference.child(firebaseUser.uid)
            .setValue(token)
            .addOnSuccessListener {
                Log.d(TAG, context.getString(R.string.token_register_success))
            }
            .addOnFailureListener {
                Log.e(TAG, context.getString(R.string.token_register_failure), it)
            }
    } else Log.w(TAG, context.getString(R.string.token_register_cancelled))
}
