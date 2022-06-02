package com.example.messengerapp.adapters

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.messengerapp.FullImageActivity
import com.example.messengerapp.R
import com.example.messengerapp.databinding.MessageItemLeftBinding
import com.example.messengerapp.databinding.MessageItemRightBinding
import com.example.messengerapp.model.ChatData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
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
            if (chatData.getMessage() == context.getString(R.string.sent_you_an_image) && chatData.getUrl() != "") {
                binding.apply {
                    rightMessageText.visibility = View.GONE
                    rightImageView.visibility = View.VISIBLE
                    Picasso.get().load(chatData.getUrl()).into(binding.rightImageView)
                    rightImageView.setOnClickListener {
                        val options = arrayOf<CharSequence>(
                            context.getString(R.string.open_image),
                            context.getString(R.string.delete_image),
                            context.getString(R.string.cancel)
                        )
                        val alertDialog = AlertDialog.Builder(binding.root.context)
                        alertDialog.apply {
                            setTitle(context.getString(R.string.actions_title))
                            setItems(options) { _, which ->
                                when (which) {
                                    0 -> {
                                        val intent = Intent(context, FullImageActivity::class.java)
                                        intent.putExtra("url", chatData.getUrl())
                                        context.startActivity(intent)
                                    }
                                    1 -> {
                                        deleteMessage(position)
                                    }
                                }
                            }
                            alertDialog.show()
                        }
                    }
                }
                // Text messages
            } else {
                binding.apply {
                    rightMessageText.text = chatData.getMessage()

                    rightMessageText.setOnClickListener {
                        val options = arrayOf<CharSequence>(
                            context.getString(R.string.delete_message),
                            context.getString(R.string.cancel)
                        )
                        val alertDialog = AlertDialog.Builder(binding.root.context)
                        alertDialog.apply {
                            setTitle(context.getString(R.string.actions_title))
                            setItems(options) { _, which ->
                                if (which == 0) {
                                    deleteMessage(position)
                                }
                            }
                            alertDialog.show()
                        }
                    }
                }
            }
            // Set messages to sent or seen
            sentOrSeenMessage(chatData, position, binding)
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
            if (chatData.getMessage() == context.getString(R.string.sent_you_an_image) && chatData.getUrl() != "") {
                binding.apply {
                    leftMessageText.visibility = View.GONE
                    leftImageView.visibility = View.VISIBLE
                    Picasso.get().load(chatData.getUrl()).into(binding.leftImageView)

                    leftImageView.setOnClickListener {
                        val options = arrayOf<CharSequence>(
                            context.getString(R.string.open_image),
                            context.getString(R.string.cancel)
                        )
                        val alertDialog = AlertDialog.Builder(binding.root.context)
                        alertDialog.apply {
                            setTitle(context.getString(R.string.actions_title))
                            setItems(options) { _, which ->
                                if (which == 0) {
                                    val intent = Intent(context, FullImageActivity::class.java)
                                    intent.putExtra("url", chatData.getUrl())
                                    context.startActivity(intent)
                                }
                            }
                            alertDialog.show()
                        }
                    }
                }
                // Text messages
            } else {
                binding.leftMessageText.text = chatData.getMessage()
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

    private fun sentOrSeenMessage(
        chatData: ChatData,
        position: Int,
        binding: MessageItemRightBinding
    ) {
        when (position) {
            chatList.size - 1 -> {

                if (chatData.getIsSeen()) {
                    binding.rightSeenText.text = context.getString(R.string.seen)
                } else {
                    binding.rightSeenText.text = context.getString(R.string.sent)
                }
            }
            else -> {
                binding.rightSeenText.visibility = View.GONE
            }
        }
    }

    private fun deleteMessage(position: Int) {
        FirebaseDatabase.getInstance().reference.child("Chat")
            .child(chatList[position].getMessageId())
            .removeValue()
            .addOnCompleteListener { task ->
                when {
                    task.isSuccessful -> {
                        Toast
                            .makeText(context, context.getString(R.string.deleted), Toast.LENGTH_SHORT)
                            .show()
                    }
                    else -> {
                        Toast
                            .makeText(context, context.getString(R.string.not_deleted), Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
    }
}

