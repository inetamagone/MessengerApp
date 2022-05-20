package com.example.messengerapp.adapters

import android.content.Context
import android.service.autofill.UserData
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.messengerapp.databinding.SearchItemBinding

class UserAdapter(private val context: Context, private val userList: List<UserData>, private val isOnline: Boolean): RecyclerView.Adapter<UserAdapter.UserViewHolder?>() {

    class UserViewHolder(private val binding: SearchItemBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(userData: UserData) {
            binding.apply {

            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        return UserViewHolder(SearchItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(userList[position])
    }

    override fun getItemCount(): Int {
        return userList.size
    }
}