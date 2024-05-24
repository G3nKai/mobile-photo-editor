package com.example.newapp

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.*
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.MotionEvent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.newapp.databinding.ActivityAthensBinding
import java.io.File
import java.io.FileOutputStream

class AthensActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAthensBinding
    private lateinit var originalBitmap: Bitmap
    private lateinit var mutableBitmap: Bitmap
    private lateinit var points: Array<PointF?>
    private lateinit var pointsStart: Array<PointF?>
    private lateinit var pointsEnd: Array<PointF?>
    private var currentPointIndex = 0
    private val pointColors = arrayOf(Color.RED, Color.GREEN, Color.BLUE)

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAthensBinding.inflate(layoutInflater)
        setContentView(binding.root)

        points = arrayOfNulls(3)
        pointsStart = arrayOfNulls(3)
        pointsEnd = arrayOfNulls(3)

        val imageUri = Uri.parse(intent.getStringExtra("imageUri"))
        val inputStream = contentResolver.openInputStream(imageUri)
        originalBitmap = BitmapFactory.decodeStream(inputStream)
        inputStream?.close()

        mutableBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true)
        binding.imageViewAthens.setImageBitmap(mutableBitmap)

        binding.textViewSave.setOnClickListener {
            val scaledUri = dispatchToGallery(originalBitmap)

            val intent = Intent(this, ThirdActivity::class.java)
            intent.putExtra("imageSource", "gallery")
            intent.putExtra("imageUri", scaledUri.toString())
            startActivity(intent)
        }

        binding.textViewOrigin.setOnClickListener {
            mutableBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true)
            binding.imageViewAthens.setImageBitmap(mutableBitmap)
            points = arrayOfNulls(3)
            pointsStart = arrayOfNulls(3)
            pointsEnd = arrayOfNulls(3)
            currentPointIndex = 0
        }

        binding.imageViewAthens.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN && currentPointIndex < 3) {
                touchPoint(event)
                true
            } else
                false
        }

        binding.athenesStart.setOnClickListener {
            pointsStart = points.copyOf()
            mutableBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true)
            binding.imageViewAthens.setImageBitmap(mutableBitmap)
            points = arrayOfNulls(3)
            currentPointIndex = 0
        }

        binding.athenesEnd.setOnClickListener {
            if (pointsStart.any { it == null }) {
                Toast.makeText(this, "Сначала заполните точки Start", Toast.LENGTH_SHORT).show()
            } else {
                pointsEnd = points.copyOf()
                mutableBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true)
                binding.imageViewAthens.setImageBitmap(mutableBitmap)
            }
        }

    }

    private fun touchPoint(event: MotionEvent) {
        val imageView = binding.imageViewAthens

        val drawable = imageView.drawable ?: return

        val matrix = Matrix()
        imageView.imageMatrix.invert(matrix)

        val points = floatArrayOf(event.x, event.y)
        matrix.mapPoints(points)

        val touchX = points[0]
        val touchY = points[1]

        val imageWidth = drawable.intrinsicWidth
        val imageHeight = drawable.intrinsicHeight

        if (touchX >= 0 && touchX <= imageWidth && touchY >= 0 && touchY <= imageHeight) {
            addPoint(touchX, touchY)
        }
    }

    private fun addPoint(x: Float, y: Float) {
        points[currentPointIndex] = PointF(x, y)
        drawPoint(x, y, pointColors[currentPointIndex])
        currentPointIndex++
    }

    private fun drawPoint(x: Float, y: Float, color: Int) {
        val canvas = Canvas(mutableBitmap)
        val paint = Paint().apply {
            this.color = color
            style = Paint.Style.FILL
        }
        canvas.drawCircle(x, y, 20f, paint)
        binding.imageViewAthens.invalidate()
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