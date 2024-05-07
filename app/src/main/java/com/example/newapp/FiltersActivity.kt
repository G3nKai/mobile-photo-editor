package com.example.newapp

import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.newapp.databinding.ActivityFiltersBinding
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import kotlin.math.atan2
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

class FiltersActivity: AppCompatActivity() {
    private lateinit var binding: ActivityFiltersBinding
    private lateinit var originalBitmap: Bitmap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFiltersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val imageUri = Uri.parse(intent.getStringExtra("imageUri"))
        binding.imageViewFilter.setImageURI(imageUri)

        originalBitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(imageUri))
        binding.imageViewFilter.setImageBitmap(originalBitmap)
    }
}