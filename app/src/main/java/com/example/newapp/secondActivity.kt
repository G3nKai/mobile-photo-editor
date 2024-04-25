package com.example.newapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class secondActivity : AppCompatActivity() {
    companion object {
        private const val GALLERY_REQUEST_CODE = 1
        private const val CAMERA_REQUEST_CODE = 2
    }
    private lateinit var imageView: ImageView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_second)

        imageView = findViewById(R.id.imageViewer) // Инициализация imageView
        val pickImageButton: Button = findViewById(R.id.button_gallery)

        pickImageButton.setOnClickListener {
            dispatchPickImageIntent()
        }
    }


    private fun dispatchPickImageIntent() {
        val pickImageIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImageIntent.type = "image/*"
        startActivityForResult(pickImageIntent, GALLERY_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GALLERY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val imageUri = data?.data
            imageView.setImageURI(imageUri)
            val intent = Intent(this, thirdActivity::class.java)
            intent.putExtra("imageUri", imageUri)
            startActivity(intent)


        }
    }
}