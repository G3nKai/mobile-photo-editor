@file:Suppress("UNREACHABLE_CODE")

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
import androidx.appcompat.app.AppCompatActivity
import com.example.newapp.databinding.ActivityVectorBinding
import kotlin.math.pow

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

    private val splinePaint = Paint().apply {
        color = Color.GREEN
        style = Paint.Style.STROKE
        strokeWidth = 3f
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

        binding.buttonSpline.setOnClickListener {
            drawSpline()
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

        val imageWidth = drawable.intrinsicWidth
        val imageHeight = drawable.intrinsicHeight

        if (touchX >= 0 && touchX <= imageWidth && touchY >= 0 && touchY <= imageHeight) {
            if (action == MotionEvent.ACTION_DOWN) {
                addPoint(touchX, touchY)
            }
        }
    }

    private fun addPoint(x: Float, y: Float) {
        if (points.isNotEmpty()) {
            val (lastX, lastY) = points.last()
            drawWuLine(lastX, lastY, x, y, splinePaint)
        }
        points.add(Pair(x, y))
        canvas.drawCircle(x, y, 10f, paint)
        binding.imageViewVector.invalidate()
    }

    private fun drawSpline() {
        if (points.size < 2) return

        canvas.drawColor(0, android.graphics.PorterDuff.Mode.CLEAR)
        canvas.drawBitmap(originalBitmap, 0f, 0f, null)

        for (i in 0 until points.size - 1) {
            val (x0, y0) = if (i == 0) points[i] else points[i - 1]
            val (x1, y1) = points[i]
            val (x2, y2) = points[i + 1]
            val (x3, y3) = if (i + 2 < points.size) points[i + 2] else points[i + 1]

            val cPointX1 = x1 + (x2 - x0) / 6.0f
            val cPointY1 = y1 + (y2 - y0) / 6.0f
            val cPointX2 = x2 - (x3 - x1) / 6.0f
            val cPointY2 = y2 - (y3 - y1) / 6.0f

            val numSegments = 15
            val segmentsLength = 1.0f / numSegments

            var prevX = x1
            var prevY = y1

            for (j in 1..numSegments) {
                val t = segmentsLength * j
                val t2 = t * t
                val t3 = t2 * t

                val reverseT = 1.0f - t
                val reverseT2 = reverseT * reverseT
                val reverseT3 = reverseT2 * reverseT

                val x = reverseT3 * x1 + 3 * reverseT2 * t * cPointX1 + 3 * reverseT * t2 * cPointX2 + t3 * x2
                val y = reverseT3 * y1 + 3 * reverseT2 * t * cPointY1 + 3 * reverseT * t2 * cPointY2 + t3 * y2

                drawWuLine(prevX, prevY, x, y, splinePaint)

                prevX = x
                prevY = y
            }
        }

        for (point in points) {
            canvas.drawCircle(point.first, point.second, 10f, paint)
        }


        binding.imageViewVector.invalidate()
    }

    private fun fractionalPart(x: Float): Float {
        return x - x.toInt()
    }

    private fun ipart(x: Float): Int {
        return x.toInt()
    }

    private fun drawWuLine(x0: Float, y0: Float, x1: Float, y1: Float, paint: Paint) {
        var x0 = x0
        var y0 = y0
        var x1 = x1
        var y1 = y1

        val steepness = Math.abs(y1 - y0) > Math.abs(x1 - x0)

        if (steepness) {
            val currX0 = x0
            val currY0 = y0
            x0 = currY0
            y0 = currX0

            val currX1 = x1
            val currY1 = y1
            x1 = currY1
            y1 = currX1
        }

        if (x1 < x0) {
            val currX0 = x0
            val currY0 = y0
            x0 = x1
            y0 = y1
            x1 = currX0
            y1 = currY0
        }

        val dx = x1 - x0
        val dy = y1 - y0
        val valueGradient = dy / dx

        var finX = Math.round(x0)
        var finY = y0 + valueGradient * (finX - x0)
        var xGap = 1 - fractionalPart(x0 + 0.5f)
        val xpxl1 = finX
        val ypxl1 = finY.toInt()

        if (steepness) {
            drawPoint(ypxl1, xpxl1, paint, 1 - fractionalPart(finY) * xGap)
            drawPoint(ypxl1 + 1, xpxl1, paint, fractionalPart(finY) * xGap)
        } else {
            drawPoint(xpxl1, ypxl1, paint, 1 - fractionalPart(finY) * xGap)
            drawPoint(xpxl1, ypxl1 + 1, paint, fractionalPart(finY) * xGap)
        }

        var intery = finY + valueGradient

        finX = Math.round(x1)
        finY = y1 + valueGradient * (finX - x1)
        xGap = fractionalPart(x1 + 0.5f)
        val xpxl2 = finX
        val ypxl2 = finY.toInt()

        if (steepness) {
            drawPoint(ypxl2, xpxl2, paint, 1 - fractionalPart(finY) * xGap)
            drawPoint(ypxl2 + 1, xpxl2, paint, fractionalPart(finY) * xGap)
        } else {
            drawPoint(xpxl2, ypxl2, paint, 1 - fractionalPart(finY) * xGap)
            drawPoint(xpxl2, ypxl2 + 1, paint, fractionalPart(finY) * xGap)
        }

        if (steepness) {
            for (x in xpxl1 + 1 until xpxl2) {
                val alpha = 1 - fractionalPart(intery)
                drawPoint(ipart(intery), x, paint, alpha)
                drawPoint(ipart(intery) + 1, x, paint, 1 - alpha)
                intery += valueGradient
            }
        } else {
            for (x in xpxl1 + 1 until xpxl2) {
                val alpha = 1 - fractionalPart(intery)
                drawPoint(x, ipart(intery), paint, alpha)
                drawPoint(x, ipart(intery) + 1, paint, 1 - alpha)
                intery += valueGradient
            }
        }
    }

    private fun drawPoint(x: Int, y: Int, paint: Paint, alpha: Float) {
        val radius = paint.strokeWidth / 2
        val scaledAlpha = (alpha * 255).toInt()

        if (x >= 0 && y >= 0 && x < mutableBitmap.width && y < mutableBitmap.height) {
            val distanceFromCenter = Math.sqrt((x - (x + 0.5)).pow(2) + (y - (y + 0.5)).pow(2))
            val scaledDistance = 1 - distanceFromCenter / radius
            val alphaToUse = (scaledAlpha * scaledDistance).toInt()

            paint.alpha = alphaToUse
            canvas.drawCircle(x.toFloat(), y.toFloat(), radius, paint)
        }
    }
}