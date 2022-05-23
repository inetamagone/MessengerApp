package com.example.messengerapp.fragments

import android.os.Bundle

import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.messengerapp.databinding.FragmentSettingsBinding
import com.example.messengerapp.model.UserData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.squareup.picasso.Picasso

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    var userReference: DatabaseReference? = null
    var firebaseUser: FirebaseUser? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)

        firebaseUser = FirebaseAuth.getInstance().currentUser
        userReference = FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser!!.uid)

        userReference!!.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()) {
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
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}