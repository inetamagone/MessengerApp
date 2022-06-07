package com.example.messengerapp.fragments

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
import com.example.messengerapp.viewModels.FragmentViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class ChatFragment : Fragment() {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: FragmentViewModel

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: UserAdapter

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

        viewModel = ViewModelProvider(this)[FragmentViewModel::class.java]

        firebaseUser = FirebaseAuth.getInstance().currentUser!!

        viewModel.isChatFragment = true

        // Get chat list and then user info to display from ViewModel Live data
        viewModel.getChats().observe(viewLifecycleOwner) { listOfChats ->

            viewModel.getUserData(listOfChats)
                .observe(viewLifecycleOwner) { list ->
                    list.let { listOfUserData ->
                        adapter = UserAdapter(requireContext(), listOfUserData, true)
                        recyclerView = binding.recyclerViewChatList
                        recyclerView.adapter = adapter
                        recyclerView.layoutManager = LinearLayoutManager(context)
                    }
                }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}