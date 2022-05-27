package com.example.messengerapp.fragments

import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import com.example.messengerapp.R
import com.example.messengerapp.databinding.FragmentSettingsBinding
import com.example.messengerapp.model.UserData
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private lateinit var userReference: DatabaseReference
    private lateinit var firebaseUser: FirebaseUser

    private lateinit var imageUri: Uri
    private lateinit var storageRef: StorageReference
    private var isProfileImage: Boolean = false
    private var isEditingAbout: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        userReference =
            FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser.uid)

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

        storageRef = FirebaseStorage.getInstance().reference.child("User images")

        binding.editCoverImage.setOnClickListener {
            isProfileImage = false
            selectImage()
        }
        binding.editProfileImage.setOnClickListener {
            isProfileImage = true
            selectImage()
        }
        binding.facebookSettings.setOnClickListener {
            isEditingAbout = false
            editPersonalInfo()
        }
        binding.editAboutInfo.setOnClickListener {
            isEditingAbout = true
            editPersonalInfo()
        }
    }

    private fun editPersonalInfo() {
        when {
            !isEditingAbout -> {
                val dialogBuilder: AlertDialog.Builder = AlertDialog.Builder(context)
                dialogBuilder
                    .setTitle(getString(R.string.title_fb_username))
                val editText = EditText(context)
                editText.hint = getString(R.string.hint_username_example)
                dialogBuilder
                    .setView(editText)

                dialogBuilder
                    .setPositiveButton(getString(R.string.create)) { _, _ ->
                        val entryString = editText.text.toString()

                        if (entryString == "") {
                            Toast.makeText(context, getString(R.string.write_something), Toast.LENGTH_LONG)
                                .show()
                        } else {
                            saveEditedInfo(entryString)
                        }
                    }
                dialogBuilder
                    .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                        dialog.cancel()
                    }

                dialogBuilder
                    .show()
            }
            else -> {
                val dialogBuilder: AlertDialog.Builder = AlertDialog.Builder(context)
                dialogBuilder
                    .setTitle(getString(R.string.title_about_info))
                val editText = EditText(context)
                editText.hint = getString(R.string.like_ride_bicycle)
                dialogBuilder
                    .setView(editText)

                dialogBuilder
                    .setPositiveButton(getString(R.string.create)) { _, _ ->
                        val entryString = editText.text.toString()

                        if (entryString == "") {
                            Toast.makeText(context, getString(R.string.write_something), Toast.LENGTH_LONG)
                                .show()
                        } else {
                            saveEditedInfo(entryString)
                        }
                    }
                dialogBuilder
                    .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                        dialog.cancel()
                    }
                dialogBuilder
                    .show()
            }
        }
    }

    private fun saveEditedInfo(entryString: String) {
        when {
            !isEditingAbout -> {
                val mapLink = HashMap<String, Any>()
                mapLink["facebook"] = "https://m.facebook.com/$entryString"

                userReference.updateChildren(mapLink).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast
                            .makeText(context, getString(R.string.updated_successfully), Toast.LENGTH_LONG)
                            .show()
                    }
                }
            }
            else -> {
                val mapLink = HashMap<String, Any>()
                mapLink["about"] = entryString

                userReference.updateChildren(mapLink).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast
                            .makeText(context, getString(R.string.updated_successfully), Toast.LENGTH_LONG)
                            .show()
                    }
                }
            }
        }
    }

    private fun selectImage() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent, 438)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 438 && resultCode == Activity.RESULT_OK && data!!.data != null) {
            imageUri = data.data!!

            saveImageToFirestore()
        }
    }

    private fun saveImageToFirestore() {
        val progressDialog = ProgressDialog(context)
        progressDialog.setMessage(getString(R.string.image_loading))
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}