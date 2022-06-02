package com.example.messengerapp

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.messengerapp.adapters.ChatAdapter
import com.example.messengerapp.databinding.ActivityMessageBinding
import com.example.messengerapp.model.ChatData
import com.example.messengerapp.model.UserData
import com.example.messengerapp.utils.registerToken
import com.example.messengerapp.utils.sendNotification
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso

private const val TAG = "MessageActivity"
private lateinit var binding: ActivityMessageBinding

class MessageActivity : AppCompatActivity() {

    private var messageReceiver: String = ""
    lateinit var firebaseUser: FirebaseUser

    private lateinit var adapter: ChatAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var chatList: List<ChatData>
    private lateinit var reference: DatabaseReference

    var notify = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMessageBinding.inflate(layoutInflater)
        setContentView(binding.root)

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

        // Get the username and picture of the message receiver
        reference = FirebaseDatabase.getInstance().reference
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

                getMessages(firebaseUser.uid, messageReceiver, user.getProfile())
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
        registerToken()

        binding.sendButtonChat.setOnClickListener {
            notify = true
            val message = binding.textMessage.text.toString()
            if (message != "") {
                sendMessage(firebaseUser.uid, messageReceiver, message)
            }

//            val notificationTitle = "New Message"
//
//            FcmUtils().sendNotification(
//                this@MessageActivity,
//                messageReceiver,
//                notificationTitle, message
//            )

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

    private fun getMessages(senderId: String, receiverId: String, receiverImageUrl: String) {
        chatList = ArrayList()
        val reference = FirebaseDatabase.getInstance().reference.child("Chat")
        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                (chatList as ArrayList<ChatData>).clear()
                for (snap in snapshot.children) {
                    val chat = snap.getValue(ChatData::class.java)

                    if (chat!!.getReceiver() == senderId && chat.getSender() == receiverId
                        || chat.getReceiver() == receiverId && chat.getSender() == senderId
                    ) {

                        // Populate arrayList with messages
                        (chatList as ArrayList<ChatData>).add(chat)
                    }
                    adapter = ChatAdapter(
                        this@MessageActivity,
                        (chatList as ArrayList<ChatData>),
                        receiverImageUrl
                    )
                    recyclerView.adapter = adapter
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
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