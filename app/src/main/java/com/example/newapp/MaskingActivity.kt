package com.example.newapp

import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.newapp.databinding.ActivityMaskingBinding

class MaskingActivity: AppCompatActivity() {
    private lateinit var binding: ActivityMaskingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMaskingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val byteArray = intent.getByteArrayExtra("imageByteArray")
        val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray!!.size)

        binding.imageView2.setImageBitmap(bitmap)
    }
}