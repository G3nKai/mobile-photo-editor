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

                if (scaleFactor > 1) {
                    val scaledBitmap = bilinear(originalBitmap, scaleFactor)
                    val scaledUri = dispatchToGallery(scaledBitmap)

                    val intent = Intent(this, ThirdActivity::class.java)
                    intent.putExtra("imageSource", "gallery")
                    intent.putExtra("imageUri", scaledUri.toString())
                    startActivity(intent)
                }
                else if (scaleFactor <= 0) {
                    Toast.makeText(this, "Масштаб должен быть введён корректно", Toast.LENGTH_SHORT).show()
                }
                else {
                    val scaledBitmap = trilinear(originalBitmap, scaleFactor)
                    val scaledUri = dispatchToGallery(scaledBitmap)

                    val intent = Intent(this, ThirdActivity::class.java)
                    intent.putExtra("imageSource", "gallery")
                    intent.putExtra("imageUri", scaledUri.toString())
                    startActivity(intent)
                }
            }
            else
                Toast.makeText(this, "Введите коэффициент масштабирования", Toast.LENGTH_SHORT).show()
        }
    }

    private fun trilinear(bitmap: Bitmap, scaleFactor: Float): Bitmap {
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

                val origin = getPixelSafely(bitmap, xOrigin, yOrigin)
                val right = getPixelSafely(bitmap, xOrigin + 1, yOrigin)
                val upper = getPixelSafely(bitmap, xOrigin, yOrigin + 1)
                val upperRight = getPixelSafely(bitmap, xOrigin + 1, yOrigin + 1)

                val upperLeft = getPixelSafely(bitmap, xOrigin - 1, yOrigin + 1)
                val left = getPixelSafely(bitmap, xOrigin - 1, yOrigin)
                val lowerLeft = getPixelSafely(bitmap, xOrigin - 1, yOrigin - 1)
                val lower = getPixelSafely(bitmap, xOrigin, yOrigin - 1)

                val zFraction = 1.0f

                val interpolatedPixel = trilinearInterpolation(origin, right, upper, upperRight,
                                                               upperLeft, left, lowerLeft, lower,
                                                               xFraction, yFraction, zFraction)

                newBitmap.setPixel(x, y, interpolatedPixel)
            }

        return newBitmap
    }


    private fun trilinearInterpolation(
        origin: Int, right: Int, upper: Int, upperRight: Int,
        upperLeft: Int, left: Int, lowerLeft: Int, lower: Int,
        xFraction: Float, yFraction: Float, zFraction: Float): Int {

        val firstSlice = bilinearInterpolation(origin, right, upper, upperRight, xFraction, yFraction)
        val secondSlice = bilinearInterpolation(upperLeft, left, lowerLeft, lower, xFraction, yFraction)

        return blendColors(firstSlice, secondSlice, zFraction)
    }

    private fun bilinear(bitmap: Bitmap, scaleFactor: Float): Bitmap {
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

                val origin = getPixelSafely(bitmap, xOrigin, yOrigin)
                val right = getPixelSafely(bitmap, xOrigin + 1, yOrigin)
                val upper = getPixelSafely(bitmap, xOrigin, yOrigin + 1)
                val upperRight = getPixelSafely(bitmap, xOrigin + 1, yOrigin + 1)

                val interpolatedPixel = bilinearInterpolation(origin, right, upper, upperRight,
                                                              xFraction, yFraction)

                newBitmap.setPixel(x, y, interpolatedPixel)
            }

        return newBitmap
    }

    private fun bilinearInterpolation(origin: Int, right: Int, upper: Int, upperRight: Int,
                                      xFraction: Float, yFraction: Float): Int {
        val firstSlice = blendColors(upper, upperRight, xFraction)
        val secondSlice = blendColors(origin, right, xFraction)

        return blendColors(firstSlice, secondSlice, yFraction)
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

    private fun blendColors(firstSlice: Int, secondSlice: Int, fraction: Float): Int {
        val inverseFraction = 1 - fraction
        val alpha = (Color.alpha(firstSlice) * inverseFraction + Color.alpha(secondSlice) * fraction).roundToInt()
        val red = (Color.red(firstSlice) * inverseFraction + Color.red(secondSlice) * fraction).roundToInt()
        val green = (Color.green(firstSlice) * inverseFraction + Color.green(secondSlice) * fraction).roundToInt()
        val blue = (Color.blue(firstSlice) * inverseFraction + Color.blue(secondSlice) * fraction).roundToInt()

        return Color.argb(alpha, red, green, blue)
    }
}