package com.example.newapp

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.Gravity
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.newapp.databinding.ActivityMaskingBinding
import java.io.File
import java.io.FileOutputStream

class MaskingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMaskingBinding
    private lateinit var originalBitmap: Bitmap
    private lateinit var backUpBitmap: Bitmap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMaskingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val imageUri = Uri.parse(intent.getStringExtra("imageUri"))

        backUpBitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(imageUri))
        originalBitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(imageUri))
        binding.imageView2.setImageBitmap(originalBitmap)

        binding.button3.setOnClickListener {
            val unsharpMaskedBitmap = applyUnsharpMasking(originalBitmap, 5, 1.0f)
            
            binding.imageView2.setImageBitmap(unsharpMaskedBitmap)

            val message = "Фильтр применён"
            val duration = Toast.LENGTH_SHORT

            val toast = Toast.makeText(applicationContext, message, duration)
            toast.setGravity(Gravity.BOTTOM, 0, 100)
            toast.show()
        }

        binding.accept.setOnClickListener{
            val scaledUri = dispatchToGallery(originalBitmap)

            val intent = Intent(this, ThirdActivity::class.java)
            intent.putExtra("imageSource", "gallery")
            intent.putExtra("imageUri", scaledUri.toString())
            startActivity(intent)
        }

        binding.cancel.setOnClickListener {
            originalBitmap = backUpBitmap
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

    private fun applyUnsharpMasking(bitmap: Bitmap, radius: Int, amount: Float): Bitmap {
        val blurredBitmap = applyGaussianBlur(bitmap, radius.toDouble())
        val width = bitmap.width
        val height = bitmap.height
        val unsharpMaskedBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val originalPixels = IntArray(width * height)
        val blurredPixels = IntArray(width * height)

        bitmap.getPixels(originalPixels, 0, width, 0, 0, width, height)
        blurredBitmap.getPixels(blurredPixels, 0, width, 0, 0, width, height)

        for (i in originalPixels.indices) {
            val originalPixel = originalPixels[i]
            val blurredPixel = blurredPixels[i]

            val r = Color.red(originalPixel) + (amount * (Color.red(originalPixel) - Color.red(blurredPixel))).toInt()
            val g = Color.green(originalPixel) + (amount * (Color.green(originalPixel) - Color.green(blurredPixel))).toInt()
            val b = Color.blue(originalPixel) + (amount * (Color.blue(originalPixel) - Color.blue(blurredPixel))).toInt()

            val correctedPixel = Color.rgb(
                r.coerceIn(0, 255),
                g.coerceIn(0, 255),
                b.coerceIn(0, 255)
            )

            unsharpMaskedBitmap.setPixel(i % width, i / width, correctedPixel)
        }

        return unsharpMaskedBitmap
    }

    private fun applyGaussianBlur(bitmap: Bitmap, radius: Double): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        val blurredBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val blurredPixels = IntArray(width * height)

        val sum = FloatArray(3)
        val blurRadius = Math.ceil(radius).toInt()

        for (y in 0 until height) {
            for (x in 0 until width) {
                sum.fill(0f)

                var currPixelCount = 0

                for (blurY in -blurRadius..blurRadius) {
                    for (blurX in -blurRadius..blurRadius) {
                        val currX = x + blurX
                        val currY = y + blurY

                        if (currX in 0 until width && currY in 0 until height) {
                            val currPixel = pixels[currY * width + currX]
                            sum[0] =  sum[0] + Color.red(currPixel)
                            sum[1] =  sum[1] + Color.green(currPixel)
                            sum[2] =  sum[2] + Color.blue(currPixel)
                            currPixelCount++
                        }
                    }
                }

                val avgColor = Color.rgb(
                    (sum[0] / currPixelCount).toInt(),
                    (sum[1] / currPixelCount).toInt(),
                    (sum[2] / currPixelCount).toInt()
                )
                blurredPixels[y * width + x] = avgColor
            }
        }

        blurredBitmap.setPixels(blurredPixels, 0, width, 0, 0, width, height)
        return blurredBitmap
    }
}
