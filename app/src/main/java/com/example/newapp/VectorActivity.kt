package com.example.newapp

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.newapp.databinding.ActivityVectorBinding

class VectorActivity: AppCompatActivity() {
    private lateinit var binding: ActivityVectorBinding
    private lateinit var originalBitmap: Bitmap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVectorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val imageUri = Uri.parse(intent.getStringExtra("imageUri"))
        binding.imageView2.setImageURI(imageUri)

        originalBitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(imageUri))
        binding.imageView2.setImageBitmap(originalBitmap)
    }
}