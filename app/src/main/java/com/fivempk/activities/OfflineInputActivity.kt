package com.fivempk.activities

import android.R.layout.simple_spinner_item
import android.R.layout.simple_spinner_dropdown_item
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.fivempk.databinding.ActivityOfflineInputBinding
import com.fivempk.models.DatabaseHelper
import com.fivempk.models.OfflineStop
import com.fivempk.utils.PyBackend
import com.fivempk.utils.RouteColorManager
import com.google.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class OfflineInputActivity : AppCompatActivity() {
    var binding: ActivityOfflineInputBinding? = null
    private lateinit var stops: List<OfflineStop>
    private var locationSelected = false
    private var destinationSelected = false

    private var locationLatitude: Double = 0.0
    private var locationLongitude: Double = 0.0
    private var destinationLatitude: Double = 0.0
    private var destinationLongitude: Double = 0.0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOfflineInputBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding?.root)

        // Initialize database helper and get all stops
        val dbHelper = DatabaseHelper(this)
        stops = dbHelper.getAllStops()
        val stopNames = stops.map { it.name }.toMutableList()
        stopNames.add(0, "None")

        // Create an ArrayAdapter using the stop names
        val adapter = ArrayAdapter(this, simple_spinner_item, stopNames)
        adapter.setDropDownViewResource(simple_spinner_dropdown_item)

        // Apply the adapter to both Spinners
        binding?.spinnerLocation?.adapter = adapter
        binding?.spinnerDestination?.adapter = adapter

        // Set up item selected listeners for both spinners
        binding?.spinnerLocation?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                if (position == 0) {
                    locationSelected = false
                    binding?.btnStart?.isEnabled = false
                } else {
                    val selectedStop = stops[position]
                    // Save the coordinates of the selected stop
                    locationLatitude = selectedStop.latitude
                    locationLongitude = selectedStop.longitude
                    locationSelected = true
                    updateStartButtonState()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                locationSelected = false
                updateStartButtonState()
            }
        }

        binding?.spinnerDestination?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                if (position == 0) {
                    destinationSelected = false
                    binding?.btnStart?.isEnabled = false
                } else {
                    val selectedStop = stops[position]
                    // Save the coordinates of the selected stop
                    destinationLatitude = selectedStop.latitude
                    destinationLongitude = selectedStop.longitude
                    destinationSelected = true
                    updateStartButtonState()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                destinationSelected = false
                updateStartButtonState()
            }
        }

        binding?.btnStart?.setOnClickListener {
            if (locationSelected && destinationSelected) {
                lifecycleScope.launch(Dispatchers.IO) {
                    val output = PyBackend.getRoute(
                        locationLatitude,
                        locationLongitude,
                        destinationLatitude,
                        destinationLongitude
                    )
                    // Switch to the Main Thread to update the UI
                    withContext(Dispatchers.Main) {
                        if (output == null) {
                            Toast.makeText(this@OfflineInputActivity, "No Routes :(", Toast.LENGTH_SHORT).show()
                        } else {
                            PyBackend.startPoint =
                                LatLng(locationLatitude, locationLongitude)
                            PyBackend.endPoint =
                                LatLng(locationLatitude, locationLongitude)

                            val intent = Intent(this@OfflineInputActivity, OfflineResultActivity::class.java)
                            RouteColorManager.resetColorIndex()
                            startActivity(intent)
                            finish()
                        }
                    }
                }
            } else {
                Toast.makeText(this@OfflineInputActivity, "Please select items in both dropdown menus", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun updateStartButtonState() {
        binding?.btnStart?.isEnabled = locationSelected && destinationSelected
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}