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
import com.example.messengerapp.viewModels.MessengerViewModel

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: MessengerViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: UserAdapter
    private var chatList: List<ChatListData> = ArrayList()

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

        viewModel.getSearchField(binding.searchField)

        viewModel.getUserData(chatList).observe(viewLifecycleOwner) { list ->
            list.let { listOfUserData ->
                adapter = UserAdapter(requireContext(), listOfUserData, false)
                recyclerView.adapter = adapter
            }
        }

        binding.searchField.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                viewModel.searchUsers(p0.toString().toLowerCase()).observe(viewLifecycleOwner) { list ->
                    list.let { listOfUserData ->
                        adapter = UserAdapter(context!!, listOfUserData, false)
                        recyclerView.adapter = adapter
                    }
                }
            }

            override fun afterTextChanged(p0: Editable?) {

            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}