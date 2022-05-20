package com.example.messengerapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.messengerapp.databinding.ActivityRegistrationBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit

class RegistrationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegistrationBinding

    private lateinit var auth: FirebaseAuth
    private lateinit var refUsers: DatabaseReference
    private var firebaseUserId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar: Toolbar = binding.toolbarRegister
        setSupportActionBar(toolbar)
        supportActionBar!!.title = "Registration"
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            val intent = Intent(this@RegistrationActivity, WelcomeActivity::class.java)
            startActivity(intent)
            finish()
        }
        auth = FirebaseAuth.getInstance()
        binding.buttonRegister.setOnClickListener {
            registerUser()
        }

    }

    private fun registerUser() {
        val username = binding.usernameRegister.text.toString()
        val email = binding.emailRegister.text.toString()
        val password = binding.passwordRegister.text.toString()

        when {
            username == "" -> {
                Toast
                    .makeText(this, "Please enter a username", Toast.LENGTH_SHORT)
                    .show()
            }
            email == "" -> {
                Toast
                    .makeText(this, "Please enter an e-mail", Toast.LENGTH_SHORT)
                    .show()
            }
            password == "" -> {
                Toast
                    .makeText(this, "Please enter a password", Toast.LENGTH_SHORT)
                    .show()
            }
            else -> {
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            firebaseUserId = auth.currentUser!!.uid
                            refUsers =
                                FirebaseDatabase.getInstance().reference.child("Users").child(
                                    firebaseUserId!!
                                )
                            val userHashMap = HashMap<String, Any>()
                            userHashMap["uid"] = firebaseUserId!!
                            userHashMap["username"] = username
                            userHashMap["profile"] =
                                "https://firebasestorage.googleapis.com/v0/b/messengerapp-d46ca.appspot.com/o/image_placeholder.png?alt=media&token=a801f1f3-261b-4e4d-ad19-d48049290c21"
                            userHashMap["cover"] =
                                "https://firebasestorage.googleapis.com/v0/b/messengerapp-d46ca.appspot.com/o/cover_placeholder.jpg?alt=media&token=90e49e38-12d5-4dcf-873a-a1f635c70f91"
                            userHashMap["status"] = "offline"
                            userHashMap["search"] = username.toLowerCase()
                            userHashMap["facebook"] = "https://m.facebook.com"
                            userHashMap["instagram"] = "https://m.instagram.com"
                            userHashMap["website"] = "https://www.google.com"

                            refUsers.updateChildren(userHashMap)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        Log.d("RegistrationActivity", "User created!")
                                    } else {
                                        Log.d("RegistrationActivity", "User is NOT created!")
                                        Toast
                                            .makeText(
                                                this@RegistrationActivity,
                                                "Error Message: " + task.exception?.message.toString(),
                                                Toast.LENGTH_SHORT
                                            )
                                            .show()
                                    }
                                }
                            val intent = Intent(
                                this@RegistrationActivity,
                                MainActivity::class.java
                            )
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(intent)
                            finish()
                        } else {
                            Toast
                                .makeText(
                                    this,
                                    "Error Message: " + task.exception?.message.toString(),
                                    Toast.LENGTH_SHORT
                                )
                                .show()
                        }
                    }
            }
        }
    }
}