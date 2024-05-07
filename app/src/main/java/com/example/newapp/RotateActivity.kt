package com.example.newapp

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.newapp.databinding.ActivityRotateBinding

class RotateActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRotateBinding
    private lateinit var originalBitmap: Bitmap

    fun rotate_photo(bitmap:Bitmap): Bitmap?{

        val width = bitmap.width
        val height = bitmap.height
        var modifiedBitmap: Bitmap = Bitmap.createBitmap(bitmap.height, bitmap.width, Bitmap.Config.ARGB_8888)

        for(x in 0 until width){

            for (y in 0 until height) {

                val pixel = bitmap.getPixel(x, y)
                modifiedBitmap.setPixel(height - y - 1, x, pixel)

            }
        }
        return modifiedBitmap;
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRotateBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        val byteArray = intent.getByteArrayExtra("imageByteArray")
//        val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray!!.size)
//
//        binding.imageView2.setImageBitmap(bitmap)
        val imageUri = Uri.parse(intent.getStringExtra("imageUri"))
        binding.imageView2.setImageURI(imageUri)
        originalBitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(imageUri))
        binding.imageView2.setImageBitmap(originalBitmap)

        

    }
}