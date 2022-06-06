package com.example.messengerapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import com.example.messengerapp.databinding.ActivityFullImageBinding
import com.squareup.picasso.Picasso

class FullImageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFullImageBinding

    private var fullImage: ImageView? = null
    private var imageUrl = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFullImageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        imageUrl = intent.getStringExtra("url").toString()
        fullImage = binding.fullImage

        Picasso.get().load(imageUrl).into(fullImage)
    }
}