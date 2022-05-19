package com.example.messengerapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.example.messengerapp.databinding.ActivityLoginBinding
import com.example.messengerapp.databinding.ActivityRegistrationBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar: Toolbar = binding.toolbarLogin
        setSupportActionBar(toolbar)
        supportActionBar!!.title = "Login"
        // Back navigation
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            val intent = Intent(this@LoginActivity, WelcomeActivity::class.java)
            startActivity(intent)
            finish()
        }

        auth = FirebaseAuth.getInstance()
        binding.buttonLogin.setOnClickListener {
            loginUser()
        }
    }

    private fun loginUser() {
        val email = binding.emailLogin.text.toString()
        val password = binding.passwordLogin.text.toString()

        when {
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
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val intent = Intent(this@LoginActivity, MainActivity::class.java)
                            // Navigation back won't lead to the login
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(intent)
                            finish()
                        } else {
                            Toast
                                .makeText(this, "Error Message: " + task.exception?.message.toString(), Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
            }
        }
    }
}