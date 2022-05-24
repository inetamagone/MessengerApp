package com.example.messengerapp

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.messengerapp.databinding.ActivityMessageBinding
import com.example.messengerapp.model.UserData
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso

private lateinit var binding: ActivityMessageBinding

class MessageActivity : AppCompatActivity() {

    var messageReceiver: String = ""
    lateinit var firebaseUser: FirebaseUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message)
        binding = ActivityMessageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        intent = intent
        messageReceiver = intent.getStringExtra("chosen_user_id").toString()
        firebaseUser = FirebaseAuth.getInstance().currentUser!!

        // Get the username and picture of the message receiver
        val reference = FirebaseDatabase.getInstance().reference
            .child("Users")
            .child(messageReceiver)
        reference.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                val user: UserData? = snapshot.getValue(UserData::class.java)
                binding.usernameChat.text = user!!.getUsername()
                Picasso
                    .get()
                    .load(user.getProfile())
                    .into(binding.profileImageChat)
            }
            override fun onCancelled(error: DatabaseError) {

            }
        })

        binding.sendButtonChat.setOnClickListener {
            val message = binding.textMessage.text.toString()
            if (message != "") {
                sendMessage(firebaseUser.uid, messageReceiver, message)
            }
            binding.textMessage.setText("")
        }

        binding.attachmentButtonChat.setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            startActivityForResult(Intent.createChooser(intent, "Choose Image"), 438)
        }
    }

    // Store the message to Firebase
    private fun sendMessage(senderId: String, messageReceiverId: String, message: String) {
        val reference = FirebaseDatabase.getInstance().reference
        val messageKey = reference.push().key

        val messageHashMap = HashMap<String, Any?>()
        messageHashMap["sender"] = senderId
        messageHashMap["receiver"] = messageReceiverId
        messageHashMap["message"] = message
        messageHashMap["messageId"] = messageKey
        messageHashMap["is_seen"] = false
        messageHashMap["url"] = ""
        reference.child("Chat")
            .child(messageKey!!)
            .setValue(messageHashMap)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {

                    // Adding the sender for the receiver
                    val chatListReference = FirebaseDatabase.getInstance()
                        .reference
                        .child("ChatList")
                        .child(senderId)
                        .child(messageReceiverId)

                    chatListReference.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (!snapshot.exists()) {
                                chatListReference.child("id").setValue(messageReceiverId)
                            }
                            // Adding the receiver for the sender
                            val chatListReceiverReference = FirebaseDatabase.getInstance()
                                .reference
                                .child("ChatList")
                                .child(messageReceiverId)
                                .child(senderId)
                            chatListReceiverReference.child("id").setValue(senderId)
                        }
                        override fun onCancelled(error: DatabaseError) {
                        }
                    })
                }
            }
//        val userReference = FirebaseDatabase.getInstance().reference
//            .child("Users")
//            .child(senderId)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 438 && resultCode == RESULT_OK && data != null && data.data != null) {

            val progressDialog = ProgressDialog(this)
            progressDialog.setMessage("Sending image...")
            progressDialog.show()

            val fileUri = data.data
            val storageReference = FirebaseStorage.getInstance().reference.child("Chat files")
            val reference = FirebaseDatabase.getInstance().reference
            val messageId = reference.push().key
            val filepath = storageReference.child("$messageId.jpg")

            val saveTask: StorageTask<*>
            saveTask = filepath.putFile(fileUri!!)

            saveTask.continueWithTask<Uri?>(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                }
                return@Continuation filepath.downloadUrl
            }).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val url = task.result.toString()
                    val attachmentHashMap = HashMap<String, Any?>()
                    attachmentHashMap["sender"] = firebaseUser.uid
                    attachmentHashMap["receiver"] = messageReceiver
                    attachmentHashMap["message"] = "Sent you an image"
                    attachmentHashMap["messageId"] = messageId
                    attachmentHashMap["is_seen"] = false
                    attachmentHashMap["url"] = url

                    reference.child("Chat").child(messageId!!).setValue(attachmentHashMap)

                    progressDialog.dismiss()
                }
            }
        }
    }
}