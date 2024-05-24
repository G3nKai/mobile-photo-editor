package com.example.newapp

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.app.NotificationCompat.Style
import kotlin.math.cos
import kotlin.math.sin
class CubeView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0):
    View(context,attrs,defStyleAttr){

        private val paint = Paint().apply {
            style = Paint.Style.FILL
        }

        private val points = arrayOf(
            floatArrayOf(-1f, -1f, -1f),
            floatArrayOf(1f, -1f, -1f),
            floatArrayOf(1f, 1f, -1f),
            floatArrayOf(-1f, 1f, -1f),
            floatArrayOf(-1f, -1f, 1f),
            floatArrayOf(1f, -1f, 1f),
            floatArrayOf(1f, 1f, 1f),
            floatArrayOf(-1f, 1f, 1f)
        )

        val projectedPoints = Array(8){FloatArray(2)}

        val SideColors = arrayOf(
            0xFFFF0000.toInt(),
            0xFF00FF00.toInt(),
            0xFF0000FF.toInt(),
            0xFFFFFF00.toInt(),
            0xFFFF00FF.toInt(),
            0xFF00FFFF.toInt()
        )

        var angelX = 0f
        var angelY = 0f
        var lastTouchX = 0f
        var lastTouch = 0f

        override fun onDraw(canvas: Canvas){
            super.onDraw(canvas)
            val width = width.toFloat()
            val height = height.toFloat()
            val size = Math.min(width,height) / 4

            canvas.drawColor(Color.BLACK)
        }
    }