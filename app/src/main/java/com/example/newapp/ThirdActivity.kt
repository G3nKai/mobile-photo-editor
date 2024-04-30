package com.example.newapp

import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.Recycler
import androidx.recyclerview.widget.RecyclerView
import com.example.newapp.databinding.ActivityThirdBinding

class ThirdActivity: AppCompatActivity() {
    private lateinit var binding: ActivityThirdBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityThirdBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val imageSource = intent.getStringExtra("imageSource")
        if (imageSource == "gallery") {
            val imageUri: Uri? = intent.getParcelableExtra("imageUri")
            binding.imageView3.setImageURI(imageUri)
        }
        else if (imageSource == "camera") {
            val byteArray = intent.getByteArrayExtra("imageByteArray")
            val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray!!.size)

            binding.imageView3.setImageBitmap(bitmap)
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

        binding.algoList.layoutManager = LinearLayoutManager(this)
        binding.algoList.adapter = AlgosAdapter(items, this)

    }
}
