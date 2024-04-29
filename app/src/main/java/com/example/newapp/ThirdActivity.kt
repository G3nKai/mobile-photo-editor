package com.example.newapp

import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.newapp.databinding.ActivityThirdBinding

class ThirdActivity: AppCompatActivity() {
    private lateinit var binding: ActivityThirdBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityThirdBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val imageSource = intent.getStringExtra("imageSource")
        if (imageSource == "gallery") {
            val imageUri: Uri? = intent.getParcelableExtra("imageUri")
            binding.imageView3.setImageURI(imageUri)
        }
        else if (imageSource == "camera") {
            val byteArray = intent.getByteArrayExtra("imageByteArray")
            val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray!!.size)

            binding.imageView3.setImageBitmap(bitmap)
        }
    }
}
