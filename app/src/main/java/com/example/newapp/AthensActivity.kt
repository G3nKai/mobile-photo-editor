package com.example.newapp

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Point
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import com.example.newapp.databinding.ActivityAthensBinding
import java.io.File
import java.io.FileOutputStream

class AthensActivity: AppCompatActivity() {
    private lateinit var binding: ActivityAthensBinding
    private lateinit var originalBitmap: Bitmap
    private lateinit var backUpBitmap: Bitmap
    private lateinit var points: Array<Point?>
    private var currentPointIndex = 0
    private var pointColors = arrayOf(Color.RED, Color.BLUE, Color.GREEN)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAthensBinding.inflate(layoutInflater)
        setContentView(binding.root)

        points = arrayOfNulls(3)

        val imageUri = Uri.parse(intent.getStringExtra("imageUri"))
        binding.imageView2.setImageURI(imageUri)

        originalBitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(imageUri))
        binding.imageView2.setImageBitmap(originalBitmap)

        backUpBitmap = originalBitmap.copy(originalBitmap.config, true)

        binding.accept.setOnClickListener {
            val scaledUri = dispatchToGallery(originalBitmap)

            val intent = Intent(this, ThirdActivity::class.java)
            intent.putExtra("imageSource", "gallery")
            intent.putExtra("imageUri", scaledUri.toString())
            startActivity(intent)
        }

        binding.cancel.setOnClickListener {
            originalBitmap = backUpBitmap
            binding.imageView2.setImageBitmap(originalBitmap)
        }

        binding.imageView2.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    points[currentPointIndex] = Point(event.x.toInt(), event.y.toInt())
                    showPoint(currentPointIndex)

                    println("Точка ${currentPointIndex + 1}: x=${event.x}, y=${event.y}")

                    currentPointIndex++
                    if (currentPointIndex >= points.size)
                        currentPointIndex = 0
                }
            }
            true
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

    private fun showPoint(index: Int) {
        val pointView = when (index) {
            0 -> binding.point1
            1 -> binding.point2
            2 -> binding.point3
            else -> null
        }

        pointView?.apply {
            alpha = 1f
            visibility = View.VISIBLE
            setBackgroundColor(pointColors[index])
        }
    }


}