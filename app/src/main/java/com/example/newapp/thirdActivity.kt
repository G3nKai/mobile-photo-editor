package com.example.newapp

import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class thirdActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_third)
        val imageUri: Uri? = intent.getParcelableExtra("imageUri")
        findViewById<ImageView>(R.id.imageView3).setImageURI(imageUri)

    }
}