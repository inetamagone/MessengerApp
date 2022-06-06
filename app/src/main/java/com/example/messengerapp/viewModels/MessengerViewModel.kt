package com.example.messengerapp.viewModels

import android.widget.EditText
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.messengerapp.model.ChatListData
import com.example.messengerapp.model.UserData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MessengerViewModel : ViewModel() {

    var isChatFragment: Boolean = true
    private val userList = MutableLiveData<List<UserData>>()
    private lateinit var searchField: EditText

    // For ChatFragment and SearchFragment
    fun getUserList(chatList: List<ChatListData>): MutableLiveData<List<UserData>> {
        val list: List<UserData> = ArrayList()
        val firebaseUserID = FirebaseAuth.getInstance().currentUser!!.uid
        val reference = FirebaseDatabase.getInstance().reference.child("Users")
        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                (list as ArrayList).clear()

                if (isChatFragment) {
                    for (snap in snapshot.children) {
                        val user = snap.getValue(UserData::class.java)
                        for (message in chatList) {
                            if (user!!.getUid() == message.getId()) {
                                list.add(user)
                                userList.value = list
                            }
                        }
                    }
                } else {
                    if (searchField.text.toString() == "") {

                        for (snap in snapshot.children) {
                            val user: UserData? = snap.getValue(UserData::class.java)
                            if ((user!!.getUid()) != firebaseUserID) {
                               list.add(user)
                                userList.value = list
                            }
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
        return userList
    }

    fun getSearchField(editText: EditText): EditText {
        this.searchField = editText
        return searchField
    }

}