package com.example.messengerapp

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.messengerapp.databinding.ActivityVisitProfileBinding
import com.example.messengerapp.model.UserData
import com.example.messengerapp.utils.picassoSetImage
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

private lateinit var binding: ActivityVisitProfileBinding

class VisitProfileActivity : AppCompatActivity() {

    private var visitedUserId = ""
    private lateinit var user: UserData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVisitProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        visitedUserId = intent.getStringExtra("chosen_user_id").toString()

        val reference = FirebaseDatabase.getInstance().reference.child("Users").child(visitedUserId)
        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    user = snapshot.getValue(UserData::class.java)!!
                    binding.usernameVisit.text = user.getUsername()

                    picassoSetImage(user.getProfile(), binding.profileImageVisit)
                    picassoSetImage(user.getCover(), binding.coverImageVisit)

                    binding.aboutVisit.text = user.getAbout()

                }
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })

        binding.facebookVisit.setOnClickListener {
            val uri = Uri.parse(user.getFacebook())

            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        }

        binding.visitSendButton.setOnClickListener {
            val intent = Intent(this@VisitProfileActivity, MessageActivity::class.java)
            intent.putExtra("chosen_user_id", user.getUid())
            startActivity(intent)
        }
    }
}