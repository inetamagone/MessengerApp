package com.example.messengerapp.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.messengerapp.databinding.MessageItemLeftBinding
import com.example.messengerapp.databinding.MessageItemRightBinding
import com.example.messengerapp.model.ChatData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.squareup.picasso.Picasso

private var mImageUrl = ""

class ChatAdapter(
    private var context: Context,
    private var chatList: List<ChatData>,
    imageUrl: String
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var mImageUrl = imageUrl
    private var firebaseUser: FirebaseUser = FirebaseAuth.getInstance().currentUser!!

    class RightViewHolder(private val binding: MessageItemRightBinding) :

        RecyclerView.ViewHolder(binding.root) {
        fun bind(chatData: ChatData) {
            // Profile image displaying
            Picasso.get().load(mImageUrl).into(binding.rightImageProfile)
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
        }

        companion object {
            fun from(parent: ViewGroup): RightViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = MessageItemRightBinding.inflate(layoutInflater, parent, false)
                return RightViewHolder(binding)
            }
        }
    }

    class LeftViewHolder(private val binding: MessageItemLeftBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(chatData: ChatData) {
            // Profile image displaying
            Picasso.get().load(mImageUrl).into(binding.leftImageProfile)
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
        }

        companion object {
            fun from(parent: ViewGroup): LeftViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = MessageItemLeftBinding.inflate(layoutInflater, parent, false)
                return LeftViewHolder(binding)
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
            0
        } else {
            1
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == 1) ChatAdapter.RightViewHolder.from(parent)
        else LeftViewHolder.from(parent)
    }

    override fun getItemCount(): Int =
        chatList.size

}

