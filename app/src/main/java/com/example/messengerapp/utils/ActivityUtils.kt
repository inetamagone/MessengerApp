package com.example.messengerapp.utils

import android.content.Context
import android.widget.ImageView
import android.widget.Toast
import com.squareup.picasso.Picasso

fun picassoSetImage(imageUrl: String, imageView: ImageView?) {
    Picasso.get().load(imageUrl).into(imageView)
}

fun showToast(context: Context, toastText: String) {
    Toast
        .makeText(context, toastText, Toast.LENGTH_SHORT)
        .show()
}
