package com.example.messengerapp.fragments

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.example.messengerapp.databinding.FragmentSettingsBinding
import com.example.messengerapp.viewModels.FragmentViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: FragmentViewModel

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

        viewModel = ViewModelProvider(this)[FragmentViewModel::class.java]

        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        userReference =
            FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser.uid)

        viewModel.setViews(userReference, binding)

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
            viewModel.editPersonalInfo(requireContext(), userReference, isEditingAbout)
        }
        binding.editAboutInfo.setOnClickListener {
            isEditingAbout = true
            viewModel.editPersonalInfo(requireContext(), userReference, isEditingAbout)
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

            viewModel.saveImageToFirestore(requireContext(), storageRef, imageUri, isProfileImage, userReference)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
