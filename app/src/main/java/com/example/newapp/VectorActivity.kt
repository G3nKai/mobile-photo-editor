package com.example.newapp

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.*
import android.net.Uri
import android.os.Bundle
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import com.example.newapp.databinding.ActivityVectorBinding

class VectorActivity : AppCompatActivity() {
    private lateinit var binding: ActivityVectorBinding
    private lateinit var originalBitmap: Bitmap
    private lateinit var mutableBitmap: Bitmap
    private lateinit var canvas: Canvas

    private val paint = Paint().apply {
        color = Color.RED
        style = Paint.Style.FILL
        strokeWidth = 10f
    }

    private val linePaint = Paint().apply {
        color = Color.RED
        style = Paint.Style.STROKE
        strokeWidth = 5f
    }

    private val points = mutableListOf<Pair<Float, Float>>()
    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVectorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val imageUri = Uri.parse(intent.getStringExtra("imageUri"))
        binding.imageViewVector.setImageURI(imageUri)

        originalBitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(imageUri))
        mutableBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true)
        canvas = Canvas(mutableBitmap)

        binding.imageViewVector.setImageBitmap(mutableBitmap)

        binding.imageViewVector.setOnTouchListener { _, event ->
            createBrokenLine(event)
            true
        }
    }

    private fun createBrokenLine(event: MotionEvent) {
        val action = event.action
        val imageView = binding.imageViewVector

        val drawable = imageView.drawable ?: return

        val matrix = Matrix()
        imageView.imageMatrix.invert(matrix)

        val points = floatArrayOf(event.x, event.y)
        matrix.mapPoints(points)

        val touchX = points[0]
        val touchY = points[1]

        if (action == MotionEvent.ACTION_DOWN) {
            addPoint(touchX, touchY)
        }
    }

    private fun addPoint(x: Float, y: Float) {
        if (points.isNotEmpty()) {
            val (lastX, lastY) = points.last()
            canvas.drawLine(lastX, lastY, x, y, linePaint)
        }
        points.add(Pair(x, y))
        canvas.drawCircle(x, y, 10f, paint)
        binding.imageViewVector.invalidate()
    }
}