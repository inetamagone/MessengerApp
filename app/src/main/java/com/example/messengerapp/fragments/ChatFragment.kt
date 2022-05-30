package com.example.messengerapp.fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.messengerapp.adapters.UserAdapter
import com.example.messengerapp.databinding.FragmentChatBinding
import com.example.messengerapp.model.ChatListData
import com.example.messengerapp.model.UserData
import com.example.messengerapp.notifications.Token
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessaging

class ChatFragment : Fragment() {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: UserAdapter

    private lateinit var userList: List<UserData>
    private lateinit var chatList: List<ChatListData>
    private lateinit var firebaseUser: FirebaseUser

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = binding.recyclerViewChatList
        recyclerView.layoutManager = LinearLayoutManager(context)

        firebaseUser = FirebaseAuth.getInstance().currentUser!!

        chatList = ArrayList()

        val reference =  FirebaseDatabase.getInstance().reference.child("ChatList").child(firebaseUser.uid)
        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                (chatList as ArrayList).clear()

                for (snap in snapshot.children) {
                    val list = snap.getValue(ChatListData::class.java)
                    (chatList as ArrayList).add(list!!)
                }
                getChatList(requireContext())
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })

        updateToken(FirebaseMessaging.getInstance().token.toString())
    }

    private fun getChatList(context: Context) {
        userList = ArrayList()
        val reference = FirebaseDatabase.getInstance().reference.child("Users")
        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                (userList as ArrayList).clear()

                for (snap in snapshot.children) {
                    val user = snap.getValue(UserData::class.java)
                    for (message in chatList) {
                        if (user!!.getUid() == message.getId()) {
                            (userList as ArrayList).add(user)
                        }
                    }
                }
                adapter = UserAdapter(context, (userList as ArrayList<UserData>), true)
                recyclerView.adapter = adapter
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    // Notifications
    private fun updateToken(token: String) {
        val reference = FirebaseDatabase.getInstance().reference.child("Tokens")
        val token1 = Token(token)
        reference.child(firebaseUser.uid).setValue(token1)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}