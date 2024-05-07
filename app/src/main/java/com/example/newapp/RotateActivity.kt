package com.example.newapp

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.newapp.databinding.ActivityRotateBinding
import android.graphics.Color
import android.media.MediaScannerConnection
import android.os.Environment
import android.widget.SeekBar
import java.io.File
import java.io.FileOutputStream

class RotateActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRotateBinding
    private lateinit var originalBitmap: Bitmap

    fun rotate_photo(bitmap: Bitmap, angle: Float): Bitmap? {
        val radians = Math.toRadians(angle.toDouble())
        val cosAngle = Math.cos(radians)
        val sinAngle = Math.sin(radians)

        val width = bitmap.width
        val height = bitmap.height
        val centerX = width / 2
        val centerY = height / 2

        val rotatedBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        for (x in 0 until width) {
            for (y in 0 until height) {
                val xOffset = x - centerX
                val yOffset = y - centerY

                val newX = (xOffset * cosAngle - yOffset * sinAngle + centerX).toInt()
                val newY = (xOffset * sinAngle + yOffset * cosAngle + centerY).toInt()

                if (newX >= 0 && newX < width && newY >= 0 && newY < height) {
                    val pixel = bitmap.getPixel(newX, newY)
                    rotatedBitmap.setPixel(x, y, pixel)
                } else {
                    // Заполнение пустых областей
                    rotatedBitmap.setPixel(x, y, Color.TRANSPARENT)
                }
            }
        }

        return rotatedBitmap
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRotateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val imageUri = Uri.parse(intent.getStringExtra("imageUri"))
        binding.imageView2.setImageURI(imageUri)
        originalBitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(imageUri))
        binding.imageView2.setImageBitmap(originalBitmap)

        val seekBar = binding.seekBar
        val txtDeg = binding.degree
        var Degree:Int = 90

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                txtDeg.setText("${progress}deg")
                Degree = progress
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }

        })


        val rotate_button = binding.butt
        var modifiedBitmap: Bitmap? = null
        val drawable = binding.imageView2.drawable

        rotate_button.setOnClickListener {

            if (drawable is BitmapDrawable) {
                val bitmap = drawable.bitmap
                modifiedBitmap = rotate_photo(bitmap, Degree.toFloat())
            }
            binding.imageView2.setImageBitmap(modifiedBitmap)
        }

        binding.button4.setOnClickListener {
            val rotatedUri = modifiedBitmap?.let { dispatchToGallery(it) } ?: dispatchToGallery(originalBitmap)

            val intent = Intent(this, ThirdActivity::class.java)
            intent.putExtra("imageSource", "gallery")
            intent.putExtra("imageUri", rotatedUri.toString())
            startActivity(intent)
        }
    }

    private fun dispatchToGallery(bitmap: Bitmap): Uri {
        val imagesDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val imageFile = File(imagesDir, "scaled_image.png")

        val outputStream = FileOutputStream(imageFile)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        outputStream.close()

        MediaScannerConnection.scanFile(this, arrayOf(imageFile.absolutePath), null, null)

        return Uri.fromFile(imageFile)
    }
}