package com.example.messengerapp.adapters

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.messengerapp.FullImageActivity
import com.example.messengerapp.R
import com.example.messengerapp.databinding.MessageItemLeftBinding
import com.example.messengerapp.databinding.MessageItemRightBinding
import com.example.messengerapp.model.ChatData
import com.example.messengerapp.utils.picassoSetImage
import com.example.messengerapp.utils.showToast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase

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
            picassoSetImage(imageUrl, binding.rightImageProfile)
            // image sending
            when {
                chatData.getMessage() == context.getString(R.string.sent_you_an_image) && chatData.getUrl() != "" -> {
                    binding.apply {
                        rightMessageText.visibility = View.GONE
                        rightImageView.visibility = View.VISIBLE
                        picassoSetImage(chatData.getUrl(), binding.rightImageView)
                        rightImageView.setOnClickListener {
                            val options = arrayOf<CharSequence>(
                                context.getString(R.string.open_image),
                                context.getString(R.string.delete_image),
                                context.getString(R.string.cancel)
                            )
                            val dialogBuilder = AlertDialog.Builder(binding.root.context)
                            dialogBuilder.apply {
                                setTitle(context.getString(R.string.actions_title))
                                setItems(options) { _, choice ->
                                    when (choice) {
                                        0 -> {
                                            val intent =
                                                Intent(context, FullImageActivity::class.java)
                                            intent.putExtra("url", chatData.getUrl())
                                            context.startActivity(intent)
                                        }
                                        1 -> {
                                            deleteMessage(layoutPosition)
                                        }
                                    }
                                }
                                dialogBuilder.show()
                            }
                        }
                    }
                    // Text messages
                }
                else -> {
                    binding.apply {
                        rightMessageText.text = chatData.getMessage()

                        rightMessageText.setOnClickListener {
                            val options = arrayOf<CharSequence>(
                                context.getString(R.string.delete_message),
                                context.getString(R.string.cancel)
                            )
                            val dialogBuilder = AlertDialog.Builder(binding.root.context)
                            dialogBuilder.apply {
                                setTitle(context.getString(R.string.actions_title))
                                setItems(options) { _, choice ->
                                    if (choice == 0) {
                                        deleteMessage(layoutPosition)
                                    }
                                }
                                dialogBuilder.show()
                            }
                        }
                    }
                }
            }
            // Set messages to sent or seen
            sentOrSeenMessage(chatData, layoutPosition, binding)
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
            picassoSetImage(imageUrl, binding.leftImageProfile)
            // image sending
            when {
                chatData.getMessage() == context.getString(R.string.sent_you_an_image) && chatData.getUrl() != "" -> {
                    binding.apply {
                        leftMessageText.visibility = View.GONE
                        leftImageView.visibility = View.VISIBLE
                        picassoSetImage(chatData.getUrl(), binding.leftImageView)

                        leftImageView.setOnClickListener {
                            val options = arrayOf<CharSequence>(
                                context.getString(R.string.open_image),
                                context.getString(R.string.cancel)
                            )
                            val dialogBuilder = AlertDialog.Builder(binding.root.context)
                            dialogBuilder.apply {
                                setTitle(context.getString(R.string.actions_title))
                                setItems(options) { _, choice ->
                                    if (choice == 0) {
                                        val intent = Intent(context, FullImageActivity::class.java)
                                        intent.putExtra("url", chatData.getUrl())
                                        context.startActivity(intent)
                                    }
                                }
                                dialogBuilder.show()
                            }
                        }
                    }
                    // Text messages
                }
                else -> {
                    binding.leftMessageText.text = chatData.getMessage()
                }
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
        return when (firebaseUser.uid) {
            chatList[position].getSender() -> {
                0 // right side
            }
            else -> {
                1 // left side
            }
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
        layoutPosition: Int,
        binding: MessageItemRightBinding
    ) {
        when (layoutPosition) {
            chatList.size - 1 -> {

                when {
                    chatData.getIsSeen() -> {
                        binding.rightSeenText.text = context.getString(R.string.seen)
                    }
                    else -> {
                        binding.rightSeenText.text = context.getString(R.string.sent)
                    }
                }
            }
            else -> {
                binding.rightSeenText.visibility = View.GONE
            }
        }
    }

    private fun deleteMessage(layoutPosition: Int) {
        FirebaseDatabase.getInstance().reference.child("Chat")
            .child(chatList[layoutPosition].getMessageId())
            .removeValue()
            .addOnCompleteListener { task ->
                when {
                    task.isSuccessful -> {
                        showToast(context, context.getString(R.string.deleted))
                    }
                    else -> {
                        showToast(context, context.getString(R.string.not_deleted))
                    }
                }
            }
    }
}
