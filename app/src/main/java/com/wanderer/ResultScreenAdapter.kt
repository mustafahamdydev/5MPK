package com.wanderer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ResultScreenAdapter(
    private val data: List<ResultScreenData>
): RecyclerView.Adapter<ResultScreenAdapter.ItemViewHolder>() {
    inner class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view){
        val stopName: TextView = view.findViewById(R.id.stopName)
        val busName: TextView = view.findViewById(R.id.busName)
        val img: ImageView = view.findViewById(R.id.image)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val inflatedView: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_places,parent,false)

        return ItemViewHolder(inflatedView)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val resData : ResultScreenData = data[position]
        holder.stopName.text= resData.stopName
        holder.busName.text= resData.busName
        holder.img.setImageResource(resData.img) // Set image resource

    }
}