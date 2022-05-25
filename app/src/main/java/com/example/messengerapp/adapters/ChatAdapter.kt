package com.example.messengerapp.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.messengerapp.R
import com.example.messengerapp.databinding.MessageItemLeftBinding
import com.example.messengerapp.databinding.MessageItemRightBinding
import com.example.messengerapp.model.ChatData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.squareup.picasso.Picasso

class ChatAdapter(
    private var context: Context,
    private var chatList: List<ChatData>,
    private var imageUrl: String
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val TYPE_RIGHT = true
    }

    private var firebaseUser: FirebaseUser = FirebaseAuth.getInstance().currentUser!!

    inner class RightViewHolder(private val binding: MessageItemRightBinding) :
        RecyclerView.ViewHolder(binding.root) {

        constructor(parent: ViewGroup) : this(
            MessageItemRightBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
        fun bind(chatData: ChatData) {
            // Profile image displaying
            Picasso.get().load(imageUrl).into(binding.rightImageProfile)
            // image sending
            if (chatData.getMessage() == "Sent you an image" && chatData.getUrl() != "") {
                binding.apply {
                    rightMessageText.visibility = View.GONE
                    rightImageView.visibility = View.VISIBLE
                    Picasso.get().load(chatData.getUrl()).into(binding.rightImageView)
                }
                // Text messages
            } else {
                binding.rightMessageText.text = chatData.getMessage()
            }
            // Set messages to sent or seen
            if (position == chatList.size - 1) {

                if (chatData.getIsSeen()) {

                    binding.rightSeenText.text = context.getString(R.string.seen)
                    if (chatData.getMessage() == "Sent you an image" && chatData.getUrl() != "") {
                        val layoutParameters: RelativeLayout.LayoutParams? = binding.rightSeenText as RelativeLayout.LayoutParams?
                        layoutParameters!!.setMargins(0, 245, 10, 0)
                        binding.rightSeenText.layoutParams = layoutParameters
                    }
                } else {
                    binding.rightSeenText.text = context.getString(R.string.sent)
                    if (chatData.getMessage() == "Sent you an image" && chatData.getUrl() != "") {
                        val layoutParameters: RelativeLayout.LayoutParams? = binding.rightSeenText as RelativeLayout.LayoutParams?
                        layoutParameters!!.setMargins(0, 245, 10, 0)
                        binding.rightSeenText.layoutParams = layoutParameters
                    }
                }
            } else {
                binding.rightSeenText.visibility = View.GONE
            }
        }
    }

    inner class LeftViewHolder(private val binding: MessageItemLeftBinding) :
        RecyclerView.ViewHolder(binding.root) {

        constructor(parent: ViewGroup) : this(
            MessageItemLeftBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
        fun bind(chatData: ChatData) {
            // Profile image displaying
            Picasso.get().load(imageUrl).into(binding.leftImageProfile)
            // image sending
            if (chatData.getMessage() == "Sent you an image" && chatData.getUrl() != "") {
                binding.apply {
                    leftMessageText.visibility = View.GONE
                    leftImageView.visibility = View.VISIBLE
                    Picasso.get().load(chatData.getUrl()).into(binding.leftImageView)
                }
                // Text messages
            } else {
                binding.leftMessageText.text = chatData.getMessage()
            }
            // Set messages to sent or seen
            if (position == chatList.size - 1) {

                if (chatData.getIsSeen()) {

                    binding.leftSeenText.text = context.getString(R.string.seen)
                    if (chatData.getMessage() == "Sent you an image" && chatData.getUrl() != "") {
                        val layoutParameters: RelativeLayout.LayoutParams? = binding.leftSeenText as RelativeLayout.LayoutParams?
                        layoutParameters!!.setMargins(0, 245, 10, 0)
                        binding.leftSeenText.layoutParams = layoutParameters
                    }
                } else {
                    binding.leftSeenText.text = context.getString(R.string.sent)
                    if (chatData.getMessage() == "Sent you an image" && chatData.getUrl() != "") {
                        val layoutParameters: RelativeLayout.LayoutParams? = binding.leftSeenText as RelativeLayout.LayoutParams?
                        layoutParameters!!.setMargins(0, 245, 10, 0)
                        binding.leftSeenText.layoutParams = layoutParameters
                    }
                }
            } else {
                binding.leftSeenText.visibility = View.GONE
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ChatAdapter.RightViewHolder -> holder.bind(chatList[position])
            is ChatAdapter.LeftViewHolder -> holder.bind(chatList[position])
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (chatList[position].getSender() == firebaseUser.uid) {
            0 // right side
        } else {
            1 // left side
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType == 0) {
            TYPE_RIGHT -> RightViewHolder(parent)
            else -> LeftViewHolder(parent)
        }
    }

    override fun getItemCount(): Int =
        chatList.size

}

