package com.example.newapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.newapp.databinding.ActivitySecondBinding
import java.io.ByteArrayOutputStream

class SecondActivity : AppCompatActivity() {
    companion object {
        private const val GALLERY_REQUEST_CODE = 1
        private const val CAMERA_REQUEST_CODE = 2
        private const val CAMERA_PERMISSION_CODE = 3
    }

    private lateinit var binding: ActivitySecondBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySecondBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonGallery.setOnClickListener {
            dispatchPickImageIntent()
        }

        binding.buttonPhoto.setOnClickListener {
            dispatchTakePhotoIntent()
        }
    }

    private fun dispatchTakePhotoIntent() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            cameraLauncher.launch(cameraIntent)
        }
        else {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE)
        }
    }

    private fun dispatchPickImageIntent() {
        val pickImageIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImageIntent.type = "image/*"
        galleryLauncher.launch(pickImageIntent)
    }

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val data: Intent? = result.data
            handleCameraResult(data)
        }
    }

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val data: Intent? = result.data
            handleGalleryResult(data)
        }
    }

    private fun handleGalleryResult(data: Intent?) {
        if (data != null && data.data != null) {
            val imageUri = data.data
            binding.imageViewer.setImageURI(imageUri)
            val intent = Intent(this, ThirdActivity::class.java)
            intent.putExtra("imageSource", "gallery")
            intent.putExtra("imageUri", imageUri)
            startActivity(intent)
        }
    }

    private fun handleCameraResult(data: Intent?) {
        if (data != null && data.extras != null && data.extras!!.containsKey("data")) {
            val photo: ByteArray = data.extras!!.get("data") as ByteArray
            val intent = Intent(this, ThirdActivity::class.java)
            intent.putExtra("imageSource", "camera")
            intent.putExtra("imageByteArray", photo)
            startActivity(intent)
        }
    }
}
