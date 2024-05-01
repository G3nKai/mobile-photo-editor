package com.example.newapp

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.newapp.databinding.ActivityRotateBinding

class RotateActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRotateBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRotateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val imageUri: Uri? = intent.getParcelableExtra("imageUri")
        binding.imageView2.setImageURI(imageUri)
    }
}