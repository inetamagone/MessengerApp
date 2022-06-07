package com.example.messengerapp

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.messengerapp.adapters.ChatAdapter
import com.example.messengerapp.databinding.ActivityMessageBinding
import com.example.messengerapp.model.ChatData
import com.example.messengerapp.model.UserData
import com.example.messengerapp.utils.registerToken
import com.example.messengerapp.utils.sendNotification
import com.example.messengerapp.viewModels.ActivityViewModel
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask

private lateinit var binding: ActivityMessageBinding
private lateinit var viewModel: ActivityViewModel

class MessageActivity : AppCompatActivity() {

    private var messageReceiver: String = ""
    lateinit var firebaseUser: FirebaseUser

    private lateinit var adapter: ChatAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var reference: DatabaseReference

    var notify = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMessageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[ActivityViewModel::class.java]

        val toolbar: Toolbar = binding.toolbarChat
        setSupportActionBar(toolbar)
        supportActionBar!!.title = ""
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        intent = intent
        messageReceiver = intent.getStringExtra("chosen_user_id").toString()
        firebaseUser = FirebaseAuth.getInstance().currentUser!!

        recyclerView = binding.chatRecyclerView
        val linearLayoutManager = LinearLayoutManager(applicationContext)
        linearLayoutManager.stackFromEnd = true
        recyclerView.layoutManager = linearLayoutManager

        // Get the message receiver
        reference = FirebaseDatabase.getInstance().reference
            .child("Users")
            .child(messageReceiver)

        /* Create the activity user observer and chat observer
         Pass the methods to the ViewModel and then to the adapter */

        val userObserver = Observer<UserData> { user ->
            user.let {
                val image = user.getProfile()

                val chatObserver = Observer<List<ChatData>> { chat ->
                    chat.let {
                        adapter = ChatAdapter(
                            this@MessageActivity,
                            chat,
                            image
                        )
                        recyclerView.adapter = adapter
                    }
                }
                viewModel.getMessages(firebaseUser.uid, messageReceiver, user!!.getProfile()).observe(this, chatObserver)
            }
        }
        viewModel.getChatUser(reference, binding).observe(this, userObserver)

        registerToken()

        binding.sendButtonChat.setOnClickListener {
            notify = true
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
            startActivityForResult(
                Intent.createChooser(
                    intent,
                    getString(R.string.title_choose_image)
                ), 438
            )
        }
        seenMessage(messageReceiver)
    }

    // Store the message to Firebase
    private fun sendMessage(senderId: String, messageReceiverId: String, message: String) {
        reference = FirebaseDatabase.getInstance().reference
        val messageKey = reference.push().key

        val messageHashMap = HashMap<String, Any?>()
        messageHashMap["sender"] = senderId
        messageHashMap["receiver"] = messageReceiverId
        messageHashMap["message"] = message
        messageHashMap["messageId"] = messageKey
        messageHashMap["isseen"] = false
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

        //implement the push notifications using fcm
        val usersReference = FirebaseDatabase.getInstance().reference
            .child("Users").child(firebaseUser.uid)

        usersReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(UserData::class.java)
                if (notify) {
                    sendNotification(
                        this@MessageActivity,
                        senderId,
                        "${user!!.getUsername()}: $message",
                                messageReceiverId)
                }
                notify = false
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 438 && resultCode == RESULT_OK && data != null && data.data != null) {

            val progressDialog = ProgressDialog(this)
            progressDialog.setMessage(getString(R.string.sending_image))
            progressDialog.show()

            val fileUri = data.data
            val storageReference = FirebaseStorage.getInstance().reference.child("Chat files")
            reference = FirebaseDatabase.getInstance().reference
            val messageId = reference.push().key
            val filepath = storageReference.child("$messageId.jpg")

            val saveTask: StorageTask<*>
            saveTask = filepath.putFile(fileUri!!)

            saveTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
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
                    attachmentHashMap["message"] = getString(R.string.sent_you_an_image)
                    attachmentHashMap["messageId"] = messageId
                    attachmentHashMap["isseen"] = false
                    attachmentHashMap["url"] = url

                    reference.child("Chat").child(messageId!!).setValue(attachmentHashMap)

                    progressDialog.dismiss()
                }
            }
        }
    }

    private var seenListener: ValueEventListener? = null

    private fun seenMessage(userId: String) {
        val reference = FirebaseDatabase.getInstance().reference.child("Chat")

        seenListener = reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (snap in snapshot.children) {
                    val chat = snap.getValue(ChatData::class.java)

                    if (chat!!.getReceiver() == firebaseUser.uid && chat.getSender() == userId) {
                        val hashMap = HashMap<String, Any>()
                        hashMap["isseen"] = true
                        snap.ref.updateChildren(hashMap)
                    }
                }
            }

            override fun onCancelled(p0: DatabaseError) {
            }
        })
    }

    override fun onPause() {
        super.onPause()
        reference.removeEventListener(seenListener!!)
    }
}