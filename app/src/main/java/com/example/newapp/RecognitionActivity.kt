package com.example.newapp

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.newapp.databinding.ActivityRecognitionBinding
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.core.MatOfRect
import org.opencv.core.Rect as OpenCVRect
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc
import org.opencv.objdetect.CascadeClassifier
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class RecognitionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRecognitionBinding
    private lateinit var originalBitmap: Bitmap
    private lateinit var mRgb: Mat
    private lateinit var mGray: Mat
    private lateinit var cascadeClassifier: CascadeClassifier

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecognitionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (!OpenCVLoader.initDebug()) {
            Log.e("OpenCV", "Unable to load OpenCV")
        } else {
            Log.d("OpenCV", "OpenCV loaded successfully")
        }

        val imageUri = Uri.parse(intent.getStringExtra("imageUri"))
        binding.imageView2.setImageURI(imageUri)
        originalBitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(imageUri))
        binding.imageView2.setImageBitmap(originalBitmap)

        mRgb = Mat()
        mGray = Mat()

        Utils.bitmapToMat(originalBitmap, mRgb)
        Imgproc.cvtColor(mRgb, mGray, Imgproc.COLOR_BGR2GRAY)

        //  Loading the model
        try {
            val inputStream = resources.openRawResource(R.raw.haarcascade_frontalface_default)
            val cascadeDir = getDir("cascade", Context.MODE_PRIVATE)
            val cascadeFile = File(cascadeDir, "haarcascade_frontalface_default.xml")
            val outputStream = FileOutputStream(cascadeFile)

            val buffer = ByteArray(4096)
            var bytesRead: Int
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
            }
            inputStream.close()
            outputStream.close()

            // Load the cascade classifier
            cascadeClassifier = CascadeClassifier(cascadeFile.absolutePath)
        } catch (e: Exception) {
            Log.e("OpenCVActivity", "Error loading cascade", e)
        }

        // detecting feces after click
        binding.recButt.setOnClickListener {
            detectFaces()
        }
    }

    private fun detectFaces() {
        val faceDetections = MatOfRect()
        cascadeClassifier.detectMultiScale(mGray, faceDetections)

        val facesArray = faceDetections.toArray()
        Log.d("OpenCVActivity", "Detected ${facesArray.size} faces")

        val mutableBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(mutableBitmap)
        val paint = Paint().apply {
            color = android.graphics.Color.RED
            style = Paint.Style.STROKE
            strokeWidth = 5f
        }

        for (rect in facesArray) {
            val left = rect.x
            val top = rect.y
            val right = rect.x + rect.width
            val bottom = rect.y + rect.height
            canvas.drawRect(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat(), paint)
        }

        binding.imageView2.setImageBitmap(mutableBitmap)
    }
}
