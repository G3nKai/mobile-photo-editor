package com.example.newapp

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.MotionEvent
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.newapp.databinding.ActivityRetouchingBinding

class RetouchingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRetouchingBinding
    private lateinit var originalBitmap: Bitmap
    private lateinit var retouchedBitmap: Bitmap
    private lateinit var drawLineBitmap: Bitmap
    private lateinit var imageView: ImageView
    private lateinit var paint: Paint
    private var startX = -1f
    private var startY = -1f
    private var endX = -1f
    private var endY = -1f
    private val touchedPixels = mutableListOf<Pair<Float, Float>>()

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRetouchingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val imageUri = Uri.parse(intent.getStringExtra("imageUri"))
        binding.imageViewRetouched.setImageURI(imageUri)

        originalBitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(imageUri))
        binding.imageViewRetouched.setImageBitmap(originalBitmap)

        initDrawLineBitmap()

        paint = Paint().apply {
            color = Color.RED
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeWidth = 8f
        }
        updateDrawLineBitmap()
        binding.imageViewRetouched.setOnTouchListener { _, event ->
            val imageView = binding.imageViewRetouched
            val drawable = imageView.drawable ?: return@setOnTouchListener false

            val matrix = Matrix()
            imageView.imageMatrix.invert(matrix)

            val points = floatArrayOf(event.x, event.y)
            matrix.mapPoints(points)

            val touchX = points[0]
            val touchY = points[1]

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initDrawLineBitmap()
                    startX = touchX
                    startY = touchY
                    touchedPixels.clear()
                    clearDrawLineBitmap()
                }
                MotionEvent.ACTION_MOVE -> {
                    endX = touchX
                    endY = touchY
                    drawLine(startX, startY, endX, endY)
                    startX = touchX
                    startY = touchY
                    touchedPixels.add(Pair(startX, startY))
                }
                MotionEvent.ACTION_UP -> {
                    endX = touchX
                    endY = touchY
                    drawLine(startX, startY, endX, endY)
                    touchedPixels.add(Pair(endX, endY))
                }
            }
            true

        }

        val goButton: Button = findViewById(R.id.go)
        goButton.setOnClickListener {
            retouchImage()

            val message = "Ретуширование применено"
            val duration = Toast.LENGTH_SHORT

            val toast = Toast.makeText(applicationContext, message, duration)
            toast.setGravity(Gravity.BOTTOM, 0, 100)
            toast.show()
        }
    }

    private fun initDrawLineBitmap() {
        drawLineBitmap = Bitmap.createBitmap(originalBitmap.width, originalBitmap.height, Bitmap.Config.ARGB_8888)
    }

    private fun clearDrawLineBitmap() {
        drawLineBitmap.eraseColor(Color.TRANSPARENT)
    }

    private fun drawLine(startX: Float, startY: Float, endX: Float, endY: Float) {
        val canvas = Canvas(drawLineBitmap)
        canvas.drawLine(startX, startY, endX, endY, paint)
        updateDrawLineBitmap()
    }

    private fun updateDrawLineBitmap() {
        val combinedBitmap = originalBitmap.copy(originalBitmap.config, true)
        val canvas = Canvas(combinedBitmap)
        canvas.drawBitmap(drawLineBitmap, 0f, 0f, null)
        binding.imageViewRetouched.setImageBitmap(combinedBitmap)
    }

    private fun retouchImage() {
        if (!::retouchedBitmap.isInitialized) {
            retouchedBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true)
        }

        for (pixel in touchedPixels) {
            val retouchX = pixel.first
            val retouchY = pixel.second
            val radius = 16f

            for (x in (retouchX - radius).toInt() until (retouchX + radius).toInt()) {
                for (y in (retouchY - radius).toInt() until (retouchY + radius).toInt()) {
                    if (x >= 0 && x < retouchedBitmap.width && y >= 0 && y < retouchedBitmap.height) {
                        if (Math.pow((x - retouchX).toDouble(), 2.0) + Math.pow((y - retouchY).toDouble(), 2.0) <= Math.pow(radius.toDouble(), 2.0)) {
                            retouchedBitmap.setPixel(x, y, calculateAverageColor(x, y, radius))
                        }
                    }
                }
            }
        }

        binding.imageViewRetouched.setImageBitmap(retouchedBitmap)
        originalBitmap = retouchedBitmap;
        drawLineBitmap = originalBitmap.copy(originalBitmap.config, true)
    }

    private fun calculateAverageColor(centerX: Int, centerY: Int, radius: Float): Int {
        var totalRed = 0
        var totalGreen = 0
        var totalBlue = 0
        var count = 0

        for (x in (centerX - radius).toInt() until (centerX + radius).toInt()) {
            for (y in (centerY - radius).toInt() until (centerY + radius).toInt()) {
                if (x >= 0 && x < retouchedBitmap.width && y >= 0 && y < retouchedBitmap.height) {
                    if (Math.pow((x - centerX).toDouble(), 2.0) + Math.pow((y - centerY).toDouble(), 2.0) <= Math.pow(radius.toDouble(), 2.0)) {
                        val pixelColor = retouchedBitmap.getPixel(x, y)
                        totalRed += Color.red(pixelColor)
                        totalGreen += Color.green(pixelColor)
                        totalBlue += Color.blue(pixelColor)
                        count++
                    }
                }
            }
        }

        val averageRed = totalRed / count
        val averageGreen = totalGreen / count
        val averageBlue = totalBlue / count

        return Color.rgb(averageRed, averageGreen, averageBlue)
    }
}