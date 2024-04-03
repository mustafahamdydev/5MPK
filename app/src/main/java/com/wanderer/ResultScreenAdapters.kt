package com.wanderer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class BusAdapter(private val buses: List<Bus>) : RecyclerView.Adapter<BusViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BusViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.buses_rv_list_item, parent, false)
        return BusViewHolder(view)
    }

    override fun onBindViewHolder(holder: BusViewHolder, position: Int) {
        val bus = buses[position]
        holder.bind(bus)
    }

    override fun getItemCount(): Int {
        return buses.size
    }
}

class StopAdapter(private val stops: List<Stop>) : RecyclerView.Adapter<StopViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StopViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.stops_rv_list_item, parent, false)
        return StopViewHolder(view)
    }

    override fun onBindViewHolder(holder: StopViewHolder, position: Int) {
        val stop = stops[position]
        holder.bind(stop)
    }

    override fun getItemCount(): Int {
        return stops.size
    }
}

class BusViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    fun bind(bus: Bus) {
        itemView.findViewById<TextView>(R.id.busName).text = bus.name
        val price : Int = PyBackend.getBusPrice(bus.name)
        itemView.findViewById<TextView>(R.id.busPrice).text = price.toString()
        itemView.findViewById<ImageView>(R.id.busImage).setImageResource(bus.img)
        // Bind the inner RecyclerView here using another adapter
        val innerRecyclerView = itemView.findViewById<RecyclerView>(R.id.rvBusStops)
        innerRecyclerView.layoutManager = LinearLayoutManager(itemView.context)
        innerRecyclerView.adapter = StopAdapter(bus.stops.map { Stop(it, bus.name) })
    }
}

class StopViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    fun bind(stop: Stop) {
        itemView.findViewById<TextView>(R.id.stopName).text = stop.name
    }
}
