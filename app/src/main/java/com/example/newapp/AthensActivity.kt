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
import kotlin.math.roundToInt

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
            val scaledUri = dispatchToGallery(mutableBitmap)

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

            if (pointsStart.any { it == null }) {
                Toast.makeText(this, "Start не до конца заполнен.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Start заполнен корректно.", Toast.LENGTH_SHORT).show()
            }
        }

        binding.athenesEnd.setOnClickListener {
            if (pointsStart.any { it == null }) {
                Toast.makeText(this, "Сначала заполните точки Start", Toast.LENGTH_SHORT).show()
            } else {
                pointsEnd = points.copyOf()
                mutableBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true)
                binding.imageViewAthens.setImageBitmap(mutableBitmap)
                Toast.makeText(this, "End заполнен корректно.", Toast.LENGTH_SHORT).show()
            }
        }

        binding.athenesButton.setOnClickListener {
            performAthenesTransformation()
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

    private fun performAthenesTransformation() {
        if (pointsStart.any { it == null } || pointsEnd.any { it == null }) {
            Toast.makeText(this, "Заполните Start и End", Toast.LENGTH_SHORT).show()
            return
        }

        val matrix = Matrix()
        val src = floatArrayOf(
            pointsStart[0]!!.x, pointsStart[0]!!.y,
            pointsStart[1]!!.x, pointsStart[1]!!.y,
            pointsStart[2]!!.x, pointsStart[2]!!.y
        )
        val dst = floatArrayOf(
            pointsEnd[0]!!.x, pointsEnd[0]!!.y,
            pointsEnd[1]!!.x, pointsEnd[1]!!.y,
            pointsEnd[2]!!.x, pointsEnd[2]!!.y
        )
        matrix.setPolyToPoly(src, 0, dst, 0, 3)

        val newWidth = originalBitmap.width
        val newHeight = originalBitmap.height
        val transformedBitmap = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888)

        val canvas = Canvas(transformedBitmap)

        val scaleFactorX = originalBitmap.width.toFloat() / transformedBitmap.width.toFloat()
        val scaleFactorY = originalBitmap.height.toFloat() / transformedBitmap.height.toFloat()


        if (scaleFactorX > 1 && scaleFactorY > 1) {
            val scaleFactor = maxOf(scaleFactorX, scaleFactorY)
            val newBitmap = bilinear(originalBitmap, scaleFactor)
            canvas.drawBitmap(newBitmap, matrix, Paint())
        } else {
            val scaleFactor = minOf(scaleFactorX, scaleFactorY)
            val newBitmap = trilinear(originalBitmap, scaleFactor)
            canvas.drawBitmap(newBitmap, matrix, Paint())
        }

        mutableBitmap = transformedBitmap
        binding.imageViewAthens.setImageBitmap(mutableBitmap)
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

                val interpolatedPixel = trilinearInterpolation(
                    origin, right, upper, upperRight,
                    upperLeft, left, lowerLeft, lower,
                    xFraction, yFraction, zFraction
                )

                newBitmap.setPixel(x, y, interpolatedPixel)
            }

        return newBitmap
    }

    private fun trilinearInterpolation(
        origin: Int, right: Int, upper: Int, upperRight: Int,
        upperLeft: Int, left: Int, lowerLeft: Int, lower: Int,
        xFraction: Float, yFraction: Float, zFraction: Float
    ): Int {

        val firstSlice =
            bilinearInterpolation(origin, right, upper, upperRight, xFraction, yFraction)
        val secondSlice =
            bilinearInterpolation(upperLeft, left, lowerLeft, lower, xFraction, yFraction)

        return blendColors(firstSlice, secondSlice, zFraction)
    }

    private fun getPixelSafely(bitmap: Bitmap, x: Int, y: Int): Int {
        if (x < 0 || y < 0 || x >= bitmap.width || y >= bitmap.height)
            return 0
        else
            return bitmap.getPixel(x, y)
    }

    private fun blendColors(firstSlice: Int, secondSlice: Int, fraction: Float): Int {
        val inverseFraction = 1 - fraction
        val alpha =
            (Color.alpha(firstSlice) * inverseFraction + Color.alpha(secondSlice) * fraction).roundToInt()
        val red =
            (Color.red(firstSlice) * inverseFraction + Color.red(secondSlice) * fraction).roundToInt()
        val green =
            (Color.green(firstSlice) * inverseFraction + Color.green(secondSlice) * fraction).roundToInt()
        val blue =
            (Color.blue(firstSlice) * inverseFraction + Color.blue(secondSlice) * fraction).roundToInt()

        return Color.argb(alpha, red, green, blue)
    }

    private fun bilinearInterpolation(
        origin: Int, right: Int, upper: Int, upperRight: Int,
        xFraction: Float, yFraction: Float
    ): Int {
        val firstSlice = blendColors(upper, upperRight, xFraction)
        val secondSlice = blendColors(origin, right, xFraction)

        return blendColors(firstSlice, secondSlice, yFraction)
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

                val interpolatedPixel = bilinearInterpolation(
                    origin, right, upper, upperRight,
                    xFraction, yFraction
                )

                newBitmap.setPixel(x, y, interpolatedPixel)
            }

        return newBitmap
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