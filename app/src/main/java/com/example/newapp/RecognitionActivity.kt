package com.example.newapp

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.newapp.databinding.ActivityRecognitionBinding
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.core.MatOfRect
import org.opencv.core.Rect as OpenCVRect
import org.opencv.core.Scalar
import org.opencv.core.Size
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
    private var absoluteFaceSize: Int = 0

    private fun newPix(value: Int): Int {
        if (value < 0) return 0
        else if (value > 255) return 255
        else return value
    }

    fun increaseContrast(bitmap: Bitmap, contrastValue: Float): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        val newContrastBitmap = Bitmap.createBitmap(width, height, bitmap.config)
        var totalBrightness = 0.0
        for (x in 0 until width) {
            for (y in 0 until height) {
                val pixel = bitmap.getPixel(x, y)
                val brightness =
                    Color.red(pixel) * 0.299 + Color.green(pixel) * 0.587 + Color.blue(pixel) * 0.114
                totalBrightness += brightness
            }
        }
        var avgBrightness = totalBrightness / (width * height)

        for (x in 0 until width) {
            for (y in 0 until height) {
                val pixel = bitmap.getPixel(x, y)

                val currRed = Color.red(pixel)
                val currGreen = Color.green(pixel)
                val currBlue = Color.blue(pixel)

                val contrastedRed =
                    (currRed - avgBrightness) * contrastValue + avgBrightness //по формуле
                val contrastedGreen = (currGreen - avgBrightness) * contrastValue + avgBrightness
                val contrastedBlue = (currBlue - avgBrightness) * contrastValue + avgBrightness

                val resultRed = newPix(contrastedRed.toInt())
                val resultGreen = newPix(contrastedGreen.toInt())
                val resultBlue = newPix(contrastedBlue.toInt())

                newContrastBitmap.setPixel(x, y, Color.rgb(resultRed, resultGreen, resultBlue))
            }
        }
        return newContrastBitmap
    }

    fun getMosaicColor(bitmap: Bitmap, startX: Int, startY: Int, mosaicSize: Int): Int {
        val width = bitmap.width
        val height = bitmap.height

        var redSum = 0
        var greenSum = 0
        var blueSum = 0

        val numberOfPixels = mosaicSize * mosaicSize // Всего пикселей в блоке

        for (x in startX until startX + mosaicSize) {
            for (y in startY until startY + mosaicSize) {
                if (x < width && y < height) { // Проверяем, чтобы не выйти за границы изображения
                    val pixel = bitmap.getPixel(x, y)
                    redSum += Color.red(pixel)
                    greenSum += Color.green(pixel)
                    blueSum += Color.blue(pixel)
                }
            }
        }

        val avgRed = redSum / numberOfPixels
        val avgGreen = greenSum / numberOfPixels
        val avgBlue = blueSum / numberOfPixels
        return Color.rgb(avgRed, avgGreen, avgBlue)
    }


    fun applyMosaicEffect(bitmap: Bitmap, mosaicSize: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        val mosaicBitmap = Bitmap.createBitmap(width, height, bitmap.config)
        for (x in 0 until width step mosaicSize) {
            for (y in 0 until height step mosaicSize) {

                val mosaicColor = getMosaicColor(bitmap, x, y, mosaicSize)

                for (i in x until minOf(x + mosaicSize, width)) {
                    for (j in y until minOf(y + mosaicSize, height)) {
                        mosaicBitmap.setPixel(i, j, mosaicColor)
                    }
                }
            }
        }
        return mosaicBitmap
    }

    private fun applyNegativeEffect(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        val negativeBitmap = Bitmap.createBitmap(width, height, bitmap.config)

        for (x in 0 until width) {
            for (y in 0 until height) {
                val pixel = bitmap.getPixel(x, y)

                val red = 255 - Color.red(pixel)
                val green = 255 - Color.green(pixel)
                val blue = 255 - Color.blue(pixel)

                val negativePixel = Color.rgb(red, green, blue)
                negativeBitmap.setPixel(x, y, negativePixel)
            }
        }
        return negativeBitmap
    }

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
        binding.imageViewFilter.setImageURI(imageUri)
        originalBitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(imageUri))
        var modifiedBitmap = originalBitmap
        binding.imageViewFilter.setImageBitmap(originalBitmap)

        mRgb = Mat()
        mGray = Mat()

        //size of face
        val heightofimg = mGray.rows()
        if (Math.round(heightofimg * 0.2f) > 0) {
            absoluteFaceSize = Math.round(heightofimg * 0.2f)
        }

        Utils.bitmapToMat(originalBitmap, mRgb)
        Imgproc.cvtColor(mRgb, mGray, Imgproc.COLOR_BGR2GRAY)

        //  Loading the model
        try {
            val inputStream = resources.openRawResource(R.raw.haarcascade_frontalface_alt)
            val cascadeDir = getDir("cascade", Context.MODE_PRIVATE)
            val cascadeFile = File(cascadeDir, "haarcascade_frontalface_alt.xml")
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
            modifiedBitmap = detectFaces(0)
            binding.imageViewFilter.setImageBitmap(modifiedBitmap)
        }

        binding.applyNego.setOnClickListener {
            modifiedBitmap = detectFaces(1)
            binding.imageViewFilter.setImageBitmap(modifiedBitmap)
        }

        binding.applyMos.setOnClickListener {
            modifiedBitmap = detectFaces(2)
            binding.imageViewFilter.setImageBitmap(modifiedBitmap)
        }

        binding.applyCont.setOnClickListener {
            modifiedBitmap = detectFaces(3)
            binding.imageViewFilter.setImageBitmap(modifiedBitmap)
        }

        binding.textViewOrigin.setOnClickListener {
            binding.imageViewFilter.setImageBitmap(originalBitmap)
        }

        binding.textViewSave.setOnClickListener {
            val scaledUri = dispatchToGallery(modifiedBitmap!!)
            val intent = Intent(this, ThirdActivity::class.java)
            intent.putExtra("imageSource", "gallery")
            intent.putExtra("imageUri", scaledUri.toString())
            startActivity(intent)
        }
    }

    //function for detecting faces
    private fun detectFaces(flag: Int): Bitmap {
        val faceDetections = MatOfRect()
        cascadeClassifier.detectMultiScale(
            mGray, faceDetections, 1.2, 2, 2,
            Size(absoluteFaceSize.toDouble(), absoluteFaceSize.toDouble()), Size()
        )

        val facesArray = faceDetections.toArray()
        Log.d("OpenCVActivity", "Detected ${facesArray.size} faces")

        val mutableBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(mutableBitmap)
        val paint = Paint().apply {
            color = android.graphics.Color.RED
            style = Paint.Style.STROKE
            strokeWidth = 5f
        }

        if (flag == 0) {
            for (rect in facesArray) {
                val left = rect.x
                val top = rect.y
                val right = rect.x + rect.width
                val bottom = rect.y + rect.height
                canvas.drawRect(
                    left.toFloat(),
                    top.toFloat(),
                    right.toFloat(),
                    bottom.toFloat(),
                    paint
                )
            }
        }

        for (rect in facesArray) {
            val faceBitmap = Bitmap.createBitmap(
                mutableBitmap,
                rect.x.toInt(),
                rect.y.toInt(),
                rect.width.toInt(),
                rect.height.toInt()
            )
            if (flag == 1) {
                val negativeFaceBitmap = applyNegativeEffect(faceBitmap)
                val canvas = Canvas(mutableBitmap)
                canvas.drawBitmap(negativeFaceBitmap, rect.x.toFloat(), rect.y.toFloat(), null)
            }
            if (flag == 2) {
                val MosaicFaceBitmap = applyMosaicEffect(faceBitmap, 25)
                val canvas = Canvas(mutableBitmap)
                canvas.drawBitmap(MosaicFaceBitmap, rect.x.toFloat(), rect.y.toFloat(), null)
            }
            if (flag == 3) {
                val ContrastFaceBitmap = increaseContrast(faceBitmap, 45f)
                val canvas = Canvas(mutableBitmap)
                canvas.drawBitmap(ContrastFaceBitmap, rect.x.toFloat(), rect.y.toFloat(), null)
            }
        }
        return mutableBitmap
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
