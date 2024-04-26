package com.example.newapp

import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class thirdActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_third)

        val imageSource = intent.getStringExtra("imageSource")
        if (imageSource == "gallery") {
            val imageUri: Uri? = intent.getParcelableExtra("imageUri")
            findViewById<ImageView>(R.id.imageView3).setImageURI(imageUri)
        }
        else if (imageSource == "camera") {
            val byteArray = intent.getByteArrayExtra("imageByteArray")
            val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray!!.size)

            findViewById<ImageView>(R.id.imageView3).setImageBitmap(bitmap)
        }
    }
}