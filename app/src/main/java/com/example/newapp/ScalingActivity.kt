package com.example.newapp

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.newapp.databinding.ActivityScalingBinding
import java.io.File
import java.io.FileOutputStream
import kotlin.math.roundToInt

class ScalingActivity: AppCompatActivity() {
    private lateinit var binding: ActivityScalingBinding
    private lateinit var originalBitmap: Bitmap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScalingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val imageUri = Uri.parse(intent.getStringExtra("imageUri"))
        binding.imageView2.setImageURI(imageUri)

        originalBitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(imageUri))
        binding.imageView2.setImageBitmap(originalBitmap)

        binding.scaleButton.setOnClickListener {
            val scaleFactorText = binding.scaleFactorEditText.text.toString()

            if (scaleFactorText.isNotEmpty()) {
                val scaleFactor = scaleFactorText.toFloat()

                if (scaleFactor > 1) {//билинейка
                    val scaledUri = scaleAndSaveBitmap(originalBitmap, scaleFactor)

                    val intent = Intent(this, ThirdActivity::class.java)
                    intent.putExtra("imageSource", "gallery")
                    intent.putExtra("imageUri", scaledUri.toString())
                    startActivity(intent)
                }
                else
                    Toast.makeText(this, "Масштаб должен быть положительным", Toast.LENGTH_SHORT).show()
            }
            else//будет трилинейка
                Toast.makeText(this, "Введите коэффициент масштабирования", Toast.LENGTH_SHORT).show()
        }
    }

    private fun scaleAndSaveBitmap(bitmap: Bitmap, scaleFactor: Float): Uri {
        val scaledBitmap = scaleBitmap(bitmap, scaleFactor)
        val savedUri = dispatchToGallery(scaledBitmap)
        return savedUri
    }

    private fun scaleBitmap(bitmap: Bitmap, scaleFactor: Float): Bitmap {
        val newWidth = (bitmap.width * scaleFactor).toInt()
        val newHeight = (bitmap.height * scaleFactor).toInt()

        val newBitmap = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888)

        val scaleX = bitmap.width / newWidth.toFloat()
        val scaleY = bitmap.height / newHeight.toFloat()

        for (y in 0 until newHeight)
            for (x in 0 until newWidth) {
                val xOrigin = (x * scaleX).toInt()
                val yOrigin = (y * scaleY).toInt()
                val xFraction = x * scaleX - xOrigin
                val yFraction = y * scaleY - yOrigin

                val upperLeftPixel = getPixelSafely(bitmap, xOrigin, yOrigin)
                val upperRightPixel = getPixelSafely(bitmap, xOrigin + 1, yOrigin)
                val lowerLeftPixel = getPixelSafely(bitmap, xOrigin, yOrigin + 1)
                val lowerRightPixel = getPixelSafely(bitmap, xOrigin + 1, yOrigin + 1)

                val interpolatedPixel = bilinearInterpolation(
                    upperLeftPixel, upperRightPixel, lowerLeftPixel, lowerRightPixel,
                    xFraction, yFraction)

                newBitmap.setPixel(x, y, interpolatedPixel)
            }

        return newBitmap
    }

    private fun bilinearInterpolation(upperLeft: Int, upperRight: Int, lowerLeft: Int, lowerRight: Int,
                                      xFraction: Float, yFraction: Float): Int {
        val upperBlend = blendColors(upperLeft, upperRight, xFraction)
        val lowerBlend = blendColors(lowerLeft, lowerRight, xFraction)

        return blendColors(upperBlend, lowerBlend, yFraction)
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


    private fun getPixelSafely(bitmap: Bitmap, x: Int, y: Int): Int {
        if (x < 0 || y < 0 || x >= bitmap.width || y >= bitmap.height)
            return 0
        else
            return bitmap.getPixel(x, y)
    }

    private fun blendColors(color1: Int, color2: Int, fraction: Float): Int {
        val inverseFraction = 1 - fraction
        val alpha = (Color.alpha(color1) * inverseFraction + Color.alpha(color2) * fraction).roundToInt()
        val red = (Color.red(color1) * inverseFraction + Color.red(color2) * fraction).roundToInt()
        val green = (Color.green(color1) * inverseFraction + Color.green(color2) * fraction).roundToInt()
        val blue = (Color.blue(color1) * inverseFraction + Color.blue(color2) * fraction).roundToInt()

        return Color.argb(alpha, red, green, blue)
    }
}