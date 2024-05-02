package com.example.newapp

import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.ImageView

class AlgosAdapter(var items: List<Item>, var context: Context, private val byteOfArray: ByteArray?) : RecyclerView.Adapter<AlgosAdapter.MyViewHolder>() {
    class MyViewHolder(view: View): RecyclerView.ViewHolder(view){
        val title: TextView = view.findViewById(R.id.algo_list_title)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.algo_in_list, parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int {
        return items.count()
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = items[position]
        holder.title.text = item.title

        holder.itemView.findViewById<Button>(R.id.algo_list_button).setOnClickListener {
            val activityToOpen = when (position) {
                0 -> RotateActivity::class.java
                1 -> FiltersActivity::class.java
                2 -> ScalingActivity::class.java
                3 -> RecognitionActivity::class.java
                4 -> VectorActivity::class.java
                5 -> RetouchingActivity::class.java
                6 -> MaskingActivity::class.java
                7 -> AthensActivity::class.java
                8 -> CubeActivity::class.java
                else -> null
            }


            activityToOpen?.let {
                val intent = Intent(context, it)

                intent.putExtra("imageByteArray", byteOfArray)
                context.startActivity(intent)
            }
        }
    }
}