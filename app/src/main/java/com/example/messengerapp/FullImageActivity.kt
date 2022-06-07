package com.example.messengerapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.messengerapp.databinding.ActivityFullImageBinding
import com.example.messengerapp.utils.picassoSetImage

class FullImageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFullImageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFullImageBinding.inflate(layoutInflater)
        setContentView(binding.root)

       picassoSetImage(intent.getStringExtra("url").toString(), binding.fullImage)
    }
}