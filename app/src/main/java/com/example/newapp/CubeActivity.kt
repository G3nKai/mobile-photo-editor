package com.example.newapp

import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.newapp.databinding.ActivityCubeBinding

class CubeActivity: AppCompatActivity() {
    private lateinit var binding: ActivityCubeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCubeBinding.inflate(layoutInflater)
        setContentView(binding.root)

    }
}