package com.example.messengerapp.fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.messengerapp.adapters.UserAdapter
import com.example.messengerapp.databinding.FragmentChatBinding
import com.example.messengerapp.model.ChatListData
import com.example.messengerapp.model.UserData
import com.example.messengerapp.viewModels.MessengerViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ChatFragment : Fragment() {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: MessengerViewModel

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

        viewModel = ViewModelProvider(this)[MessengerViewModel::class.java]

        firebaseUser = FirebaseAuth.getInstance().currentUser!!

        chatList = ArrayList()
        val reference =
            FirebaseDatabase.getInstance().reference.child("ChatList").child(firebaseUser.uid)
        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                (chatList as ArrayList).clear()

                for (snap in snapshot.children) {
                    val list = snap.getValue(ChatListData::class.java)
                    (chatList as ArrayList).add(list!!)
                }
                viewModel.getUserList(chatList)
                    .observe(viewLifecycleOwner) { list ->
                        list.let { listOfUserData ->
                            adapter = UserAdapter(requireContext(), listOfUserData, true)
                            recyclerView = binding.recyclerViewChatList
                            recyclerView.adapter = adapter
                            recyclerView.layoutManager = LinearLayoutManager(context)
                        }
                    }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}