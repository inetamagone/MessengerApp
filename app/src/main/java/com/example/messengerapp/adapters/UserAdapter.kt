package com.example.messengerapp.adapters

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.messengerapp.MessageActivity
import com.example.messengerapp.R
import com.example.messengerapp.databinding.SearchItemBinding
import com.example.messengerapp.model.UserData
import com.squareup.picasso.Picasso

class UserAdapter(
    private val context: Context, private val userList: List<UserData>,
    private var isOnline: Boolean
) : RecyclerView.Adapter<UserAdapter.UserViewHolder?>() {

    var lastMessage: String = ""

    inner class UserViewHolder(private val binding: SearchItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(userData: UserData) {
            binding.searchUsername.text = userData.getUsername()
            Picasso
                .get()
                .load(userData.getProfile())
                .placeholder(R.drawable.profile)
                .into(binding.searchImage)
            setOnlineStatus(isOnline, userData, binding)

            // Clicking on particular user gives two options
            binding.root.setOnClickListener { 
                val options = arrayOf("Send a Message", "Visit Profile")

                val dialogBuilder: AlertDialog.Builder = AlertDialog.Builder(context)
                dialogBuilder
                    .setTitle("Actions")
                dialogBuilder.setItems(options, DialogInterface.OnClickListener { dialogInterface, choice ->
                    if (choice == 0) {
                        // Pass the UID of the chosen user
                        val intent = Intent(context, MessageActivity::class.java)
                        intent.putExtra("chosen_user_id", userData.getUid())
                        context.startActivity(intent)

                    } else {
                        TODO("Visit profile")
                    }
                })
                dialogBuilder.show()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        return UserViewHolder(
            SearchItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: UserAdapter.UserViewHolder, position: Int) =
        holder.bind(userList[position])

    override fun getItemCount(): Int =
        userList.size

    fun setOnlineStatus(isOnline: Boolean, userData: UserData, binding: SearchItemBinding) {
        when {
            isOnline -> {
                if (userData.getStatus() == "online") {
                    binding.searchOnline.visibility = View.VISIBLE
                    binding.searchOffline.visibility = View.GONE
                } else {
                    binding.searchOnline.visibility = View.GONE
                    binding.searchOffline.visibility = View.VISIBLE
                }
            }
//            else -> {
//                binding.searchOnline.visibility = View.GONE
//                binding.searchOffline.visibility = View.GONE
//            }
        }
    }
}