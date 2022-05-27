package com.example.messengerapp.notifications

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService

class MyFirebaseInstanceId : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val refreshToken = FirebaseMessaging.getInstance().token.toString()

        if (firebaseUser!= null) {
            updateToken(refreshToken)
        }
    }

    private fun updateToken(refreshToken: String?) {
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val ref = FirebaseDatabase.getInstance().reference.child("Tokens")
        val token = Token(refreshToken!!)
        ref.child(firebaseUser!!.uid).setValue(token)
    }
}