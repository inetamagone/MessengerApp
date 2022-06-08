package com.example.messengerapp.viewModels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.messengerapp.databinding.ActivityMessageBinding
import com.example.messengerapp.model.ChatData
import com.example.messengerapp.model.UserData
import com.example.messengerapp.utils.picassoSetImage
import com.google.firebase.database.*

class MessageActivityViewModel: ViewModel() {

    private lateinit var user: UserData
    private var userInfo = MutableLiveData<UserData>()
    private var chatInfo: List<ChatData> = ArrayList()
    private var chatList = MutableLiveData<List<ChatData>>()

    fun getChatUser(reference: DatabaseReference, binding: ActivityMessageBinding): MutableLiveData<UserData> {
        reference.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                user = snapshot.getValue(UserData::class.java)!!
                binding.usernameChat.text = user.getUsername()
                picassoSetImage(user.getProfile(), binding.profileImageChat)
                userInfo.value = user
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
        return userInfo
    }

    fun getMessages(senderId: String, receiverId: String, receiverImageUrl: String): MutableLiveData<List<ChatData>> {
        val reference = FirebaseDatabase.getInstance().reference.child("Chat")
        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                (chatInfo as ArrayList<ChatData>).clear()

                for (snap in snapshot.children) {
                    val chat = snap.getValue(ChatData::class.java)

                    if (chat!!.getReceiver() == senderId && chat.getSender() == receiverId
                        || chat.getReceiver() == receiverId && chat.getSender() == senderId
                    ) {

                        // Populate arrayList with messages
                        (chatInfo as ArrayList<ChatData>).add(chat)
                        chatList.value = chatInfo
                    }

                }
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
        return chatList
    }

}
