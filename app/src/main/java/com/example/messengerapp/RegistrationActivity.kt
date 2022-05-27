package com.example.messengerapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.messengerapp.databinding.ActivityRegistrationBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

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
        supportActionBar!!.title = getString(R.string.title_registration)
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
                    .makeText(this, getString(R.string.enter_username), Toast.LENGTH_SHORT)
                    .show()
            }
            email == "" -> {
                Toast
                    .makeText(this, getString(R.string.enter_email), Toast.LENGTH_SHORT)
                    .show()
            }
            password == "" -> {
                Toast
                    .makeText(this, getString(R.string.enter_password), Toast.LENGTH_SHORT)
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
                            userHashMap["profile"] = getString(R.string.profile_placeholder)
                            userHashMap["cover"] = getString(R.string.cover_placeholder)
                            userHashMap["status"] = getString(R.string.offline)
                            userHashMap["search"] = username.toLowerCase()
                            userHashMap["facebook"] = getString(R.string.fb_placeholder)
                            userHashMap["about"] = getString(R.string.tell_about_hint)

                            refUsers.updateChildren(userHashMap)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
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
                                                this@RegistrationActivity,
                                                getString(R.string.error_message) + task.exception?.message.toString(),
                                                Toast.LENGTH_SHORT
                                            )
                                            .show()
                                    }
                                }
                        } else {
                            Toast
                                .makeText(
                                    this,
                                    getString(R.string.error_message) + task.exception?.message.toString(),
                                    Toast.LENGTH_SHORT
                                )
                                .show()
                        }
                    }
            }
        }
    }
}