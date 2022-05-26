package com.example.messengerapp.adapters

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.messengerapp.MessageActivity
import com.example.messengerapp.R
import com.example.messengerapp.VisitProfileActivity
import com.example.messengerapp.databinding.SearchItemBinding
import com.example.messengerapp.model.ChatData
import com.example.messengerapp.model.UserData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso

class UserAdapter(
    private val context: Context, private val userList: List<UserData>,
    private var isChatCheck: Boolean
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

            if(isChatCheck) {
                getLastMessage(userData.getUid(), binding.lastMessage)
            } else {
                binding.lastMessage.visibility = View.GONE
            }

            if (isChatCheck)
            {
                if (userData.getStatus() == "online")
                {
                    binding.searchOnline.visibility = View.VISIBLE
                    binding.searchOffline.visibility = View.GONE
                }
                else
                {
                    binding.searchOnline.visibility = View.GONE
                    binding.searchOffline.visibility = View.VISIBLE
                }
            }
            else
            {
                binding.searchOnline.visibility = View.GONE
                binding.searchOffline.visibility = View.GONE
            }
            //setOnlineStatus(isChatCheck, userData, binding)

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
                        val intent = Intent(context, VisitProfileActivity::class.java)
                        intent.putExtra("chosen_user_id", userData.getUid())
                        context.startActivity(intent)
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

    fun setOnlineStatus(isChatCheck: Boolean, userData: UserData, binding: SearchItemBinding) {
        when {
            isChatCheck -> {
                if (userData.getStatus() == "online") {
                    binding.searchOnline.visibility = View.VISIBLE
                    binding.searchOffline.visibility = View.GONE
                } else {
                    binding.searchOnline.visibility = View.GONE
                    binding.searchOffline.visibility = View.VISIBLE
                }
            }
            else -> {
                binding.searchOnline.visibility = View.GONE
                binding.searchOffline.visibility = View.GONE
            }
        }
    }


    private fun getLastMessage(onlineUserId: String, lastMsg: TextView) {
        lastMessage = "defaultMsg" // no messages yet

        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val reference = FirebaseDatabase.getInstance().reference.child("Chat")
        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (snap in snapshot.children) {
                    val chat: ChatData? = snap.getValue(ChatData::class.java)

                    if (firebaseUser != null && chat != null) {
                        if (chat.getReceiver() == firebaseUser.uid
                            && chat.getSender() == onlineUserId
                            ||  chat.getReceiver() == onlineUserId
                            && chat.getSender() == firebaseUser.uid) {
                            lastMessage = chat.getMessage()
                        }
                    }
                }
                when (lastMessage) {
                    "defaultMsg" -> lastMsg.text = context.getString(R.string.no_message)
                    "Sent you an image" -> lastMsg.text = "Sent an image"
                    else -> lastMsg.text = lastMessage
                }
                lastMessage = "defaultMsg"
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

}