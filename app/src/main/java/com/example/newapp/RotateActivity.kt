package com.example.newapp

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.newapp.databinding.ActivityRotateBinding
import android.graphics.Color
import android.media.MediaScannerConnection
import android.os.Environment
import android.widget.Button
import android.widget.SeekBar
import java.io.File
import java.io.FileOutputStream

class RotateActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRotateBinding
    private lateinit var originalBitmap: Bitmap
    private  lateinit var backUpBitmap: Bitmap

    fun rotate_photo(bitmap: Bitmap, angle: Float): Bitmap {
        val radians = Math.toRadians(angle.toDouble())
        val cosAngle = Math.cos(radians)
        val sinAngle = Math.sin(radians)

        val width = bitmap.width
        val height = bitmap.height
        val centerX = width / 2
        val centerY = height / 2

        val rotatedBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        for (x in 0 until width) {
            for (y in 0 until height) {
                val xOffset = x - centerX
                val yOffset = y - centerY

                val newX = (xOffset * cosAngle - yOffset * sinAngle + centerX).toInt()
                val newY = (xOffset * sinAngle + yOffset * cosAngle + centerY).toInt()

                if (newX >= 0 && newX < width && newY >= 0 && newY < height) {
                    val pixel = bitmap.getPixel(newX, newY)
                    rotatedBitmap.setPixel(x, y, pixel)
                } else {
                    // Заполнение пустых областей
                    rotatedBitmap.setPixel(x, y, Color.TRANSPARENT)
                }
            }
        }

        return rotatedBitmap
    }
    fun rotate_photo(bitmap:Bitmap, flag:Boolean): Bitmap?{

        val width = bitmap.width
        val height = bitmap.height
        var modifiedBitmap: Bitmap = Bitmap.createBitmap(bitmap.height, bitmap.width, Bitmap.Config.ARGB_8888)

        if (flag) {
            for (x in 0 until width) {

                for (y in 0 until height) {

                    val pixel = bitmap.getPixel(x, y)
                    modifiedBitmap.setPixel(height - y - 1, x, pixel)

                }
            }
        }
        else{
            for (x in width-1 downTo  0) {

                for (y in 0 until height) {

                    val pixel = bitmap.getPixel(x, y)
                    modifiedBitmap.setPixel(y, width - x - 1, pixel)
                }
            }
        }

        return modifiedBitmap;
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRotateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val imageUri = Uri.parse(intent.getStringExtra("imageUri"))
        binding.imageView2.setImageURI(imageUri)
        originalBitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(imageUri))
        backUpBitmap = originalBitmap
        binding.imageView2.setImageBitmap(originalBitmap)

        val seekBar = binding.seekBar
        val txtDeg = binding.degree
        var Degree:Int = 0

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                txtDeg.setText("${progress}deg")
                Degree = progress
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }

        })


        val rotate_button = binding.butt
        var modifiedBit: Bitmap? = null
        val drawa = binding.imageView2.drawable

        rotate_button.setOnClickListener {

            if (drawa is BitmapDrawable) {
                val bitmap = drawa.bitmap
                modifiedBit = rotate_photo(bitmap, Degree.toFloat())
            }
            binding.imageView2.setImageBitmap(modifiedBit)
        }

        val rotateTo90:Button = binding.Rotate90
        val rotateTo270 = binding.Rotate270
        rotateTo90.setOnClickListener{

            val drawable = binding.imageView2.drawable

            if (drawable is BitmapDrawable) {
                val bitmap = drawable.bitmap
                modifiedBit = rotate_photo(bitmap,true)
            }
            binding.imageView2.setImageBitmap(modifiedBit)
        }

        rotateTo270.setOnClickListener{
            val drawable = binding.imageView2.drawable

            if (drawable is BitmapDrawable) {
                val bitmap = drawable.bitmap
                modifiedBit = rotate_photo(bitmap,false)
            }
            binding.imageView2.setImageBitmap(modifiedBit)
        }

        binding.saveBut.setOnClickListener {
            val rotatedUri = modifiedBit?.let { dispatchToGallery(it) } ?: dispatchToGallery(originalBitmap)

            val intent = Intent(this, ThirdActivity::class.java)
            intent.putExtra("imageSource", "gallery")
            intent.putExtra("imageUri", rotatedUri.toString())
            startActivity(intent)
        }

        binding.cancleBut.setOnClickListener{
            originalBitmap = backUpBitmap
            binding.imageView2.setImageBitmap(originalBitmap)
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
}