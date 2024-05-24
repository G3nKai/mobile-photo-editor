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
        data class Face(val indices: List<Int>, val color: Int, val averageDepth: Float)

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


            //обновление вращения точек
            val transPoints = Array(8) {FloatArray(3)}

            for(i in points.indices){
                val x = points[i][0]
                val y = points[i][1]
                val z = points[i][2]

                val tempX = x * cos(angelY) - z * sin(angelY)
                val tempZ = x * sin(angelY) + z * cos(angelY)
                val newX = tempX
                val newY = y * cos(angelX) - tempZ * sin(angelX)
                val newZ = y * sin(angelX) + tempZ * cos(angelX)

                transPoints[i][0] = width / 2 + newX * size
                transPoints[i][1] = height / 2 - newY * size
            }

            fun averageDepth(transformedPoints: Array<FloatArray>, indices: List<Int>): Float {
                return indices.map { transformedPoints[it][2] }.average().toFloat()
            }

            val faces = listOf(
                Face(listOf(0, 1, 2, 3), SideColors[0], averageDepth(transPoints, listOf(0, 1, 2, 3))),
                Face(listOf(4, 5, 6, 7), SideColors[1], averageDepth(transPoints, listOf(4, 5, 6, 7))),
                Face(listOf(0, 1, 5, 4), SideColors[2], averageDepth(transPoints, listOf(0, 1, 5, 4))),
                Face(listOf(2, 3, 7, 6), SideColors[3], averageDepth(transPoints, listOf(2, 3, 7, 6))),
                Face(listOf(0, 3, 7, 4), SideColors[4], averageDepth(transPoints, listOf(0, 3, 7, 4))),
                Face(listOf(1, 2, 6, 5), SideColors[5], averageDepth(transPoints, listOf(1, 2, 6, 5)))
            )

            val sortedFaces = faces.sortedByDescending{ it.averageDepth}
        }
    }