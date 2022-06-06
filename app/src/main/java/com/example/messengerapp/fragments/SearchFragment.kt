package com.example.messengerapp.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.messengerapp.adapters.UserAdapter
import com.example.messengerapp.databinding.FragmentSearchBinding
import com.example.messengerapp.model.ChatListData
import com.example.messengerapp.model.UserData
import com.example.messengerapp.viewModels.MessengerViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: MessengerViewModel

    private lateinit var recyclerView: RecyclerView
    private var adapter: UserAdapter? = null

    private var firebaseUserID = ""
    private lateinit var chatList: List<ChatListData>
    private var userList: List<UserData> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[MessengerViewModel::class.java]

        viewModel.isChatFragment = false

        recyclerView = binding.searchRecyclerView
        recyclerView.layoutManager = LinearLayoutManager(context)

        chatList = ArrayList()
        viewModel.getSearchField(binding.searchField)

        viewModel.getUserList(chatList).observe(viewLifecycleOwner) { list ->
            list.let { listOfUserData ->
                adapter = UserAdapter(requireContext(), listOfUserData, false)
                recyclerView = binding.searchRecyclerView
                recyclerView.adapter = adapter
                recyclerView.layoutManager = LinearLayoutManager(context)
            }
        }

        binding.searchField.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                searchUsers(p0.toString().toLowerCase())
            }

            override fun afterTextChanged(p0: Editable?) {

            }
        })
    }

    // TODO: take this fun to viewModel
    private fun searchUsers(queryString: String) {
        firebaseUserID = FirebaseAuth.getInstance().currentUser!!.uid

        val userQuery = FirebaseDatabase.getInstance().reference
            .child("Users").orderByChild("search")
            .startAt(queryString)
            .endAt(queryString + "\uf8ff")

        userQuery.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                (userList as ArrayList<UserData>).clear()

                for (snapshot in dataSnapshot.children) {
                    val user: UserData? = snapshot.getValue(UserData::class.java)
                    if ((user!!.getUid()) != firebaseUserID) {
                        (userList as ArrayList<UserData>).add(user)
                    }
                }
                adapter = UserAdapter(context!!, userList, false)
                recyclerView.adapter = adapter
            }

            override fun onCancelled(p0: DatabaseError) {
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}