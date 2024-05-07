package com.example.newapp

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.newapp.databinding.ActivityFiltersBinding
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.media.MediaScannerConnection
import android.os.Environment
import java.io.File
import java.io.FileOutputStream
import kotlin.math.atan2
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt
import java.util.Random

class FiltersActivity: AppCompatActivity() {
    private lateinit var binding: ActivityFiltersBinding
    private lateinit var originalBitmap: Bitmap
    private lateinit var currentBitmap: Bitmap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFiltersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val imageUri = Uri.parse(intent.getStringExtra("imageUri"))
        originalBitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(imageUri))
        binding.imageViewFilter.setImageBitmap(originalBitmap)

        currentBitmap = originalBitmap

        binding.buttonNegative.setOnClickListener {
            applyNegativeFilter()
        }

        binding.buttonMosaic.setOnClickListener {
            applyMosaicFilter()
        }

        binding.buttonBlur.setOnClickListener {
            applyGaussianFilter()
        }

        binding.buttonFish.setOnClickListener {
            applyFishFilter()
        }

        binding.buttonNoise.setOnClickListener {
            applyNoiseFilter()
        }

        binding.buttonsContrast.setOnClickListener {
            applyContrastFilter()
        }

        binding.textViewOrigin.setOnClickListener {
            binding.imageViewFilter.setImageBitmap(originalBitmap)
            currentBitmap = originalBitmap
        }

        binding.textViewSave.setOnClickListener {
            val scaledUri = dispatchToGallery(currentBitmap)

            val intent = Intent(this, ThirdActivity::class.java)
            intent.putExtra("imageSource", "gallery")
            intent.putExtra("imageUri", scaledUri.toString())
            startActivity(intent)
        }

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
    private fun applyNegativeFilter() {
        currentBitmap = applyNegativeEffect(currentBitmap)
        binding.imageViewFilter.setImageBitmap(currentBitmap)
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

    private fun applyMosaicFilter() {
        currentBitmap = applyMosaicEffect(currentBitmap, 12)
        binding.imageViewFilter.setImageBitmap(currentBitmap)
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
        for (x in 0 until width step mosaicSize){
            for (y in 0 until height step mosaicSize){

                val mosaicColor = getMosaicColor(bitmap, x, y, mosaicSize)

                for (i in x until minOf(x + mosaicSize, width)){
                    for (j in y until minOf(y + mosaicSize, height)) {
                        mosaicBitmap.setPixel(i, j, mosaicColor)
                    }
                }
            }
        }
        return mosaicBitmap
    }

    private fun applyGaussianFilter() {
        currentBitmap = applyGaussianBlur(currentBitmap, 5.2)
        binding.imageViewFilter.setImageBitmap(currentBitmap)
    }
    fun applyGaussianBlur (bitmap: Bitmap, radius: Double) : Bitmap{
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        val blurredBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val blurredPixels = IntArray(width * height)

        val sum = FloatArray(3)
        val blurRadius = Math.ceil(radius.toDouble()).toInt()

        for(y in 0 until height){
            for (x in 0 until width){
                sum.fill(0f)

                var currPixelCount = 0

                for (blurY in -blurRadius..blurRadius) {
                    for (blurX in -blurRadius..blurRadius){
                        val currX = x + blurX
                        val currY = y + blurY

                        if (currX in 0 until width && currY in 0 until height){
                            val currPixel = pixels[currY * width + currX]
                            sum[0] += Color.red(currPixel)
                            sum[1] += Color.green(currPixel)
                            sum[2] += Color.blue(currPixel)
                            currPixelCount++
                        }

                    }
                }

                val avgColor = Color.rgb(
                    (sum[0] / currPixelCount).toInt(),
                    (sum[1] / currPixelCount).toInt(),
                    (sum[2] / currPixelCount).toInt()
                )
                blurredPixels[y * width + x] = avgColor
            }
        }

        blurredBitmap.setPixels(blurredPixels, 0, width, 0, 0, width, height)
        return blurredBitmap
    }

    private fun applyFishFilter() {
        currentBitmap = applyFishEyeEffect(currentBitmap)
        binding.imageViewFilter.setImageBitmap(currentBitmap)
    }
    fun applyFishEyeEffect(inputBitmap: Bitmap): Bitmap {
        val width = inputBitmap.width
        val height = inputBitmap.height
        val fishingBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        val centerX = width / 2
        val centerY = height / 2
        val radius = min(centerX, centerY).toDouble()

        for (x in 0 until width) {
            for (y in 0 until height) {
                val distanceX = x - centerX //расстояние от текущей до центра
                val distanceY = y - centerY

                val distance = sqrt((distanceX * distanceX + distanceY * distanceY).toDouble()) //расстояние между текущим пикселем и центром
                if (distance < radius) {
                    val relativeDistance = distance / radius
                    val angle = atan2(distanceY.toDouble(), distanceX.toDouble()) // вычисление угла между текущей точкой и центром изображения
                    val newDistance = relativeDistance * relativeDistance * radius //новое расстояние
                    val newX = (centerX + newDistance * kotlin.math.cos(angle)).toInt() //центральная точка + новая под нужным углом
                    val newY = (centerY + newDistance * sin(angle)).toInt()
                    if (newX in 0 until width && newY in 0 until height) {
                        fishingBitmap.setPixel(x, y, inputBitmap.getPixel(newX, newY))
                    }
                } else {
                    fishingBitmap.setPixel(x, y, Color.BLACK)
                }
            }
        }

        return fishingBitmap
    }

    private fun applyNoiseFilter() {
        currentBitmap = applyNoiseEffect(currentBitmap, 50)
        binding.imageViewFilter.setImageBitmap(currentBitmap)
    }
    fun applyNoiseEffect(bitmap: Bitmap, valueOfNoise: Int) : Bitmap {
        val random = Random()
        val width = bitmap.width
        val height = bitmap.height

        val noiseBitmap = Bitmap.createBitmap(width, height, bitmap.config)
        for (x in 0 until width) {
            for (y in 0 until height) {
                val pixel = bitmap.getPixel(x,y)

                val currRed = Color.red(pixel)
                val currGreen = Color.green(pixel)
                val currBlue = Color.blue(pixel)

                val randomValue = random.nextInt(valueOfNoise) //генерация случайного числа от 0 до valueOfNoise

                val newRed = newPix(currRed + randomValue)
                val newGreen = newPix(currGreen + randomValue)
                val newBlue = newPix(currBlue + randomValue)

                noiseBitmap.setPixel(x,y,Color.rgb(newRed, newGreen, newBlue))
            }
        }

        return noiseBitmap
    }

    private fun applyContrastFilter() {
        currentBitmap = increaseContrast(currentBitmap, 2.0f)
        binding.imageViewFilter.setImageBitmap(currentBitmap)
    }
    fun increaseContrast(bitmap: Bitmap, contrastValue : Float) : Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        val newContrastBitmap = Bitmap.createBitmap(width, height, bitmap.config)
        var totalBrightness = 0.0
        for (x in 0 until width) {
            for (y in 0 until height) {
                val pixel = bitmap.getPixel(x,y)
                val brightness = Color.red(pixel) * 0.299 + Color.green(pixel) * 0.587 + Color.blue(pixel) * 0.114
                totalBrightness += brightness
            }
        }
        var avgBrightness = totalBrightness / (width * height)

        for (x in 0 until width) {
            for (y in 0 until height) {
                val pixel = bitmap.getPixel(x,y)

                val currRed = Color.red(pixel)
                val currGreen = Color.green(pixel)
                val currBlue = Color.blue(pixel)

                val contrastedRed = (currRed - avgBrightness) * contrastValue + avgBrightness //по формуле
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
}

private operator fun FloatArray.set(i: Int, value: Int) {
    this[i] = value.toFloat()
}

private fun newPix(value: Int) : Int {
    if (value < 0) return 0
    else if(value > 255) return 255
    else return value
}