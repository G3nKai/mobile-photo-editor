package com.example.newapp

import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toBitmap
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.Recycler
import androidx.recyclerview.widget.RecyclerView
import com.example.newapp.databinding.ActivityThirdBinding
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream

class ThirdActivity: AppCompatActivity() {
    private lateinit var binding: ActivityThirdBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityThirdBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val imageSource = intent.getStringExtra("imageSource")
        if (imageSource == "gallery") {
            val imageUri = Uri.parse(intent.getStringExtra("imageUri"))
            binding.imageView3.setImageURI(imageUri)
        }
        else if (imageSource == "camera") {
            val imageUri = Uri.parse(intent.getStringExtra("imageUri"))
            binding.imageView3.setImageURI(imageUri)
        }


        binding.algoList
        val items = arrayListOf<Item>()
        items.add(Item(1, "Поворот изображения" ))
        items.add(Item(2, "Цветовые фильтры" ))
        items.add(Item(3, "Масштабирование изображения" ))
        items.add(Item(4, "Распознавание лиц/людей на фото" ))
        items.add(Item(5, "Векторный редактор" ))
        items.add(Item(6, "Ретуширование" ))
        items.add(Item(7, "Нерезкое маскирование" ))
        items.add(Item(8, "Аффинные преобразования" ))
        items.add(Item(9, "3D кубик" ))

        val imageUri = Uri.parse(intent.getStringExtra("imageUri"))

        binding.button2.setOnClickListener {
            val bitmap = (binding.imageView3.drawable).toBitmap()

            val savedUri = saveBitmap(bitmap)
            if (savedUri != null) {
                addImageToGallery(savedUri)
                Toast.makeText(this, "Изображение сохранено в галерею", Toast.LENGTH_SHORT).show()
            }
            else
                Toast.makeText(this, "Ошибка при сохранении изображения", Toast.LENGTH_SHORT).show()
        }

        binding.algoList.layoutManager = LinearLayoutManager(this)
        binding.algoList.adapter = AlgosAdapter(items, this, imageUri)

    }

    private fun addImageToGallery(imageUri: Uri) {
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "saved_image")
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }

        val contentResolver = contentResolver
        val collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val imageUriSaved = contentResolver.insert(collection, values)

        if (imageUriSaved != null) {
            try {
                val outputStream: OutputStream? = contentResolver.openOutputStream(imageUriSaved)
                val inputStream = contentResolver.openInputStream(imageUri)
                inputStream?.use { input ->
                    outputStream?.use { output ->
                        input.copyTo(output)
                    }
                }
                values.clear()
                values.put(MediaStore.Images.Media.IS_PENDING, 0)
                contentResolver.update(imageUriSaved, values, null, null)
            }
            catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun saveBitmap(bitmap: Bitmap): Uri? {
        if (isExternalStorageWritable()) {
            val directory = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            val file = File(directory, "saved_image.png") // Измените на .png

            try {
                val outputStream: OutputStream = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream) // Используйте PNG
                outputStream.flush()
                outputStream.close()

                return Uri.fromFile(file)
            }
            catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return null
    }

    private fun isExternalStorageWritable(): Boolean {
        val state = Environment.getExternalStorageState()
        return Environment.MEDIA_MOUNTED == state
    }


}
