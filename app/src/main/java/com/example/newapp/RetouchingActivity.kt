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
import android.view.MotionEvent
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.newapp.databinding.ActivityRetouchingBinding

class RetouchingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRetouchingBinding
    private lateinit var originalBitmap: Bitmap
    private lateinit var retouchedBitmap: Bitmap
    private lateinit var imageView: ImageView
    private lateinit var paint: Paint
    private var startX = -1f
    private var startY = -1f
    private var endX = -1f
    private var endY = -1f

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRetouchingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val imageUri = Uri.parse(intent.getStringExtra("imageUri"))
        binding.imageViewRetouched.setImageURI(imageUri)

        originalBitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(imageUri))
        binding.imageViewRetouched.setImageBitmap(originalBitmap)

        paint = Paint().apply {
            color = Color.RED
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeWidth = 8f
        }

        binding.imageViewRetouched.setOnTouchListener { _, event ->
            val action = event.action
            val imageView = binding.imageViewRetouched

            val drawable = imageView.drawable ?: return@setOnTouchListener false

            // матрица для преобразования координат касания
            val matrix = Matrix()
            imageView.imageMatrix.invert(matrix)

            // координаты касания из ImageView в координаты изображения
            val points = floatArrayOf(event.x, event.y)
            matrix.mapPoints(points)

            val touchX = points[0]
            val touchY = points[1]

            when (action) {
                MotionEvent.ACTION_DOWN -> {
                    startX = touchX
                    startY = touchY
                }
                MotionEvent.ACTION_MOVE -> {
                    endX = touchX
                    endY = touchY
                    drawLine(startX, startY, endX, endY)
                    startX = touchX
                    startY = touchY
                }
                MotionEvent.ACTION_UP -> {
                    endX = touchX
                    endY = touchY
                    drawLine(startX, startY, endX, endY)
                }
            }
            true
        }
    }

    private fun drawLine(startX: Float, startY: Float, endX: Float, endY: Float) {
        if (!::retouchedBitmap.isInitialized) {
            retouchedBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true)
        }

        val canvas = Canvas(retouchedBitmap)
        canvas.drawLine(startX, startY, endX, endY, paint)

        binding.imageViewRetouched.setImageBitmap(retouchedBitmap)
    }
}


