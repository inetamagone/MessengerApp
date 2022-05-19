package com.example.messengerapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.messengerapp.databinding.ActivityLoginBinding
import com.example.messengerapp.databinding.ActivityRegistrationBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbarLogin)
    }
}