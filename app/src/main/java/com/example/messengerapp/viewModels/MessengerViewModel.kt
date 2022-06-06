package com.example.messengerapp.viewModels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.messengerapp.model.ChatListData
import com.example.messengerapp.model.UserData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MessengerViewModel : ViewModel() {

    val userList = MutableLiveData<List<UserData>>()

    fun getUserList(chatList: List<ChatListData>): MutableLiveData<List<UserData>> {
        val list: List<UserData> = ArrayList()
        val reference = FirebaseDatabase.getInstance().reference.child("Users")
        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                (list as ArrayList).clear()

                for (snap in snapshot.children) {
                    val user = snap.getValue(UserData::class.java)
                    for (message in chatList) {
                        if (user!!.getUid() == message.getId()) {
                            list.add(user)
                            userList.value = list
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
        return userList
    }
}