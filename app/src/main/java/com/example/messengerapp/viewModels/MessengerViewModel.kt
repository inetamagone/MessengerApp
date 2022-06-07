package com.example.messengerapp.viewModels

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.net.Uri
import android.widget.EditText
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.messengerapp.R
import com.example.messengerapp.databinding.FragmentSettingsBinding
import com.example.messengerapp.model.ChatListData
import com.example.messengerapp.model.UserData
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso

class MessengerViewModel : ViewModel() {

    var isChatFragment: Boolean = true
    private val userList = MutableLiveData<List<UserData>>()
    private val chatList = MutableLiveData<List<ChatListData>>()
    private val chats: List<ChatListData> = ArrayList()
    private val list: List<UserData> = ArrayList()
    private lateinit var searchField: EditText
    private var firebaseUser = FirebaseAuth.getInstance().currentUser

    // For ChatFragment and SearchFragment

    fun getChats(): MutableLiveData<List<ChatListData>> {

        val reference = FirebaseDatabase.getInstance().reference.child("ChatList").child(firebaseUser!!.uid)

        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                (chats as ArrayList).clear()

                for (snap in snapshot.children) {
                    val list = snap.getValue(ChatListData::class.java)
                    chats.add(list!!)
                    chatList.value = chats
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
        return chatList
    }

    fun getUserData(chatList: List<ChatListData>): MutableLiveData<List<UserData>> {

        val reference = FirebaseDatabase.getInstance().reference.child("Users")
        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                (list as ArrayList).clear()

                if (isChatFragment) {
                    for (snap in snapshot.children) {
                        val user = snap.getValue(UserData::class.java)
                        for (message in chatList) {
                            if (user!!.getUid() == message.getId()) {
                                list.add(user)
                                userList.value = list
                            }
                        }
                    }
                } else {
                    if (searchField.text.toString() == "") {

                        for (snap in snapshot.children) {
                            val user: UserData? = snap.getValue(UserData::class.java)
                            if ((user!!.getUid()) != firebaseUser!!.uid) {
                                list.add(user)
                                userList.value = list
                            }
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
        return userList
    }

    // Search Function
    fun searchUsers(queryString: String): MutableLiveData<List<UserData>> {

        val userQuery = FirebaseDatabase.getInstance().reference
            .child("Users").orderByChild("search")
            .startAt(queryString)
            .endAt(queryString + "\uf8ff")

        userQuery.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                (list as ArrayList<UserData>).clear()

                for (snap in snapshot.children) {
                    val user: UserData? = snap.getValue(UserData::class.java)
                    if ((user!!.getUid()) != firebaseUser!!.uid) {
                        list.add(user)
                        userList.value = list
                    }
                }
            }

            override fun onCancelled(p0: DatabaseError) {
            }
        })
        return userList
    }

    fun getSearchField(editText: EditText): EditText {
        this.searchField = editText
        return searchField
    }

    // Settings Fragment

    fun setViews(userReference: DatabaseReference, binding: FragmentSettingsBinding) {
        userReference.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val user: UserData? = snapshot.getValue(UserData::class.java)
                    binding.usernameSettings.text = user!!.getUsername()
                    Picasso
                        .get()
                        .load(user.getProfile())
                        .into(binding.profileImageSettings)
                    Picasso
                        .get()
                        .load(user.getCover())
                        .into(binding.coverImageSettings)
                    binding.aboutSettings.text = user.getAbout()
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    fun editPersonalInfo(
        context: Context,
        userReference: DatabaseReference,
        isEditingAbout: Boolean
    ) {
        if (!isEditingAbout) {
            showDialogBuilder(
                context,
                userReference,
                isEditingAbout,
                context.getString(R.string.title_fb_username),
                context.getString(R.string.hint_username_example)
            )
        } else {
            showDialogBuilder(
                context,
                userReference,
                isEditingAbout,
                context.getString(R.string.title_about_info),
                context.getString(R.string.like_ride_bicycle)
            )
        }
    }

    private fun saveEditedInfo(
        context: Context,
        userReference: DatabaseReference,
        isEditingAbout: Boolean,
        entryString: String
    ) {
        if (!isEditingAbout) {
            updateInFirestore(
                context,
                userReference,
                "facebook",
                "https://m.facebook.com/$entryString"
            )
        } else {
            updateInFirestore(
                context,
                userReference,
                "about",
                entryString
            )
        }
    }

    private fun updateInFirestore(
        context: Context,
        userReference: DatabaseReference,
        key: String,
        value: String
    ) {
        val mapLink = HashMap<String, Any>()
        mapLink[key] = value
        userReference.updateChildren(mapLink).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                showSuccessToast(context)

            }
        }
    }

    fun saveImageToFirestore(context: Context, storageRef: StorageReference,  imageUri: Uri, isProfileImage: Boolean, userReference: DatabaseReference) {
        val progressDialog = ProgressDialog(context)
        progressDialog.setMessage(context.getString(R.string.image_loading))
        progressDialog.show()

        val imageRef = storageRef.child(System.currentTimeMillis().toString() + ".jpg")

        val saveTask: StorageTask<*>
        saveTask = imageRef.putFile(imageUri)

        saveTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            return@Continuation imageRef.downloadUrl
        }).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val url = task.result.toString()

                if (!isProfileImage) {
                    val mapCoverImage = HashMap<String, Any>()
                    mapCoverImage["cover"] = url
                    userReference.updateChildren(mapCoverImage)
                } else {
                    val mapProfileImage = HashMap<String, Any>()
                    mapProfileImage["profile"] = url
                    userReference.updateChildren(mapProfileImage)
                }
                progressDialog.dismiss()
            }
        }
    }

    private fun showDialogBuilder(
        context: Context,
        userReference: DatabaseReference,
        isEditingAbout: Boolean,
        titleText: String,
        hintText: String
    ) {
        val dialogBuilder: AlertDialog.Builder = AlertDialog.Builder(context)
        dialogBuilder
            .setTitle(titleText)
        val editText = EditText(context)
        editText.hint = hintText
        dialogBuilder
            .setView(editText)

        dialogBuilder
            .setPositiveButton(context.getString(R.string.create)) { _, _ ->
                val entryString = editText.text.toString()

                if (entryString == "") {
                    Toast.makeText(
                        context,
                        context.getString(R.string.write_something),
                        Toast.LENGTH_LONG
                    )
                        .show()
                } else {
                    saveEditedInfo(context, userReference, isEditingAbout, entryString)
                }
            }
        dialogBuilder
            .setNegativeButton(context.getString(R.string.cancel)) { dialog, _ ->
                dialog.cancel()
            }
        dialogBuilder
            .show()
    }

    private fun showSuccessToast(context: Context) {
        Toast
            .makeText(
                context,
                context.getString(R.string.updated_successfully),
                Toast.LENGTH_LONG
            )
            .show()
    }

}