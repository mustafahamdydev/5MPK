package com.fivempk.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.LinearLayoutManager
import com.fivempk.databinding.ActivityOfflineResultBinding
import com.fivempk.utils.Bus
import com.fivempk.utils.BusAdapter
import com.fivempk.utils.PyBackend
import com.fivempk.utils.RouteColorManager

class OfflineResultActivity : AppCompatActivity() {

    var binding: ActivityOfflineResultBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOfflineResultBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        enableEdgeToEdge()

        ViewCompat.setOnApplyWindowInsetsListener(binding?.root!!) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(
                top = insets.top, // this is for the tool bar inset
                bottom = insets.bottom // lift up the bottom part of the UI above navigation bar
            )
            WindowInsetsCompat.CONSUMED
        }

        val busMap = mutableMapOf<String, MutableList<String>>()

        for (busInfo in PyBackend.routeStopsList!!) {
            val busName = busInfo[1]
            val stop = busInfo[0]

            if (busMap.containsKey(busName)) {
                busMap[busName]?.add(stop)
            } else {
                busMap[busName] = mutableListOf(stop)
            }
        }

        val buses = busMap.map { Bus(it.key, it.value) }

        buses.let {
            // Calculate the total travel cost
            val totalTravelCost = it.sumOf { bus ->
                PyBackend.getBusPrice(bus.name)
            }
            PyBackend.totalTravelCost = totalTravelCost

            // Update the total travel cost TextView
            val totalTravelCostString = "${PyBackend.totalTravelCost} E£"
            binding?.tvOfflineTotalTravelCost?.text = totalTravelCostString

            val busAdapter = BusAdapter(it)
            binding?.rvOfflineBusses?.layoutManager = LinearLayoutManager(this)
            binding?.rvOfflineBusses?.adapter = busAdapter
        }

        onBackPressedDispatcher.addCallback(this){
            val intent = Intent(this@OfflineResultActivity, OfflineInputActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
    override fun onDestroy(){
        super.onDestroy()
        binding = null
        PyBackend.resetVariables()
        RouteColorManager.resetColorIndex()
    }
}