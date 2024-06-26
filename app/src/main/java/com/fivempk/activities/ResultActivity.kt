package com.fivempk.activities

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.location.Geocoder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import android.widget.LinearLayout
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.LinearLayoutManager
import com.fivempk.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.maps.DirectionsApi
import com.google.maps.GeoApiContext
import com.google.maps.PendingResult
import com.google.maps.model.DirectionsResult
import com.google.maps.model.TravelMode
import com.fivempk.databinding.ActivityResultBinding
import com.fivempk.utils.Bus
import com.fivempk.utils.BusAdapter
import com.fivempk.utils.Constants
import com.fivempk.utils.PyBackend
import com.fivempk.utils.RouteColorManager
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import java.util.ArrayList
import java.util.Locale

class ResultActivity : AppCompatActivity(), OnMapReadyCallback {
    private var binding: ActivityResultBinding? = null
    private lateinit var mGoogleMap: GoogleMap
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>
    private var totalEstimatedTravelTimeInSeconds: Long = 0
    private var totalRequests = 0
    private var completedRequests = 0
    private var busCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
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

        // Map to link routeID with a pair containing the bus name and list of stops
        val busMap = mutableMapOf<String, Pair<String, MutableList<String>>>()

        for (busInfo in PyBackend.routeStopsList!!) {
            val stop = busInfo[0]
            val busName = busInfo[1]
            val routeId = busInfo[2] // Using routeId as the unique identifier
            val currentPair = busMap[routeId] ?: Pair(busName, mutableListOf())
            val updatedPair = currentPair.copy(second = currentPair.second.apply { add(stop) })
            busMap[routeId] = updatedPair
        }

        // Convert the map entries to Bus objects
        val buses = busMap.entries.map { Bus(it.value.first, it.value.second) }

        buses.let {
            // Calculate the total travel cost
            val totalTravelCost = it.sumOf { bus ->
                busCount += 1
                PyBackend.getBusPrice(bus.name)
            }
            PyBackend.totalTravelCost = totalTravelCost

            // Update the total travel cost TextView
            val totalTravelCostString = "${PyBackend.totalTravelCost} E£"
            binding?.tvTotalTravelCost?.text = totalTravelCostString

            val busAdapter = BusAdapter(it)
            binding?.rvBuses?.layoutManager = LinearLayoutManager(this)
            binding?.rvBuses?.adapter = busAdapter
        }

        val startAddress = getAddressFromLatLng(this@ResultActivity, PyBackend.startPoint!!.lat, PyBackend.startPoint!!.lng)
        val endAddress = getAddressFromLatLng(this@ResultActivity, PyBackend.endPoint!!.lat, PyBackend.endPoint!!.lng)

        binding?.tvStartStop?.text = startAddress
        binding?.tvEndStop?.text = endAddress

        binding?.llResultBottomSheet?.viewTreeObserver?.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                binding?.llResultBottomSheet?.viewTreeObserver?.removeOnGlobalLayoutListener(this)
                val sheet = binding?.llResultBottomSheet
                sheet?.let { bottomSheetBehavior = BottomSheetBehavior.from(it) }
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                bottomSheetBehavior.peekHeight = 200
                bottomSheetBehavior.addBottomSheetCallback(bottomSheetCallback)
            }
        })

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager.findFragmentById(R.id.resultMapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        onBackPressedDispatcher.addCallback(this){
            val intent = Intent(this@ResultActivity, MapsActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private val bottomSheetCallback = object : BottomSheetBehavior.BottomSheetCallback() {
        override fun onStateChanged(bottomSheet: View, newState: Int) {
            // Handle bottom sheet state changes here
        }

        override fun onSlide(bottomSheet: View, slideOffset: Float) {
            // If the bottom sheet is sliding, consume the touch event to prevent interference with the map
            if (slideOffset > 0) {
                mGoogleMap.uiSettings.isScrollGesturesEnabled = false
                mGoogleMap.uiSettings.isZoomGesturesEnabled = false
            } else {
                mGoogleMap.uiSettings.isScrollGesturesEnabled = true
                mGoogleMap.uiSettings.isZoomGesturesEnabled = true
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun getAddressFromLatLng(context: Context, latitude: Double, longitude: Double): String {
        val geocoder = Geocoder(context, Locale.getDefault())
        val addresses = geocoder.getFromLocation(latitude, longitude, 1)
        return addresses?.get(0)!!.getAddressLine(0)
    }

    override fun onMapReady(googleMap: GoogleMap){
        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        mGoogleMap = googleMap
        mGoogleMap.clear()
        val apiKey: String = getString(R.string.Google_Api_Key)

        val context = GeoApiContext.Builder()
            .apiKey(apiKey)
            .build()

        //Marker for start location
        googleMap.addMarker(MarkerOptions().icon(
            BitmapDescriptorFactory
            .defaultMarker(208.0f)).position(LatLng(PyBackend.startPoint!!.lat,PyBackend.startPoint!!.lng)))
        //Marker for destination location
        googleMap.addMarker(MarkerOptions().icon(
            BitmapDescriptorFactory
                .defaultMarker(267.0f)).position(LatLng(PyBackend.endPoint!!.lat,PyBackend.endPoint!!.lng)))

        when (currentNightMode) {
            Configuration.UI_MODE_NIGHT_NO -> {
                // Light theme
                val mapStyle = MapStyleOptions.loadRawResourceStyle(this, R.raw.maps_light)
                mGoogleMap.setMapStyle(mapStyle)
            }
            Configuration.UI_MODE_NIGHT_YES -> {
                // Dark theme
                val mapStyle = MapStyleOptions.loadRawResourceStyle(this, R.raw.maps_dark)
                mGoogleMap.setMapStyle(mapStyle)
            }
        }

        mGoogleMap.uiSettings.apply {
            isMapToolbarEnabled = false
            isMyLocationButtonEnabled = false
            isCompassEnabled = false
        }

        //restricts user movement to the set bounds of Cairo
        mGoogleMap.setLatLngBoundsForCameraTarget(Constants.cairoBounds)

        val firstPoint = PyBackend.multiRouteCoordinatesList?.first()?.first()
        val lastPoint = PyBackend.multiRouteCoordinatesList?.last()?.last()

        drawDirectionsWalk(context, PyBackend.startPoint!!,firstPoint!!)
        PyBackend.multiRouteCoordinatesList?.forEach { busRoute ->
            val routeColor = RouteColorManager.getRouteNextColor()
            drawDirectionsBus(context,busRoute,routeColor)
            totalRequests++
        }
        drawDirectionsWalk(context, lastPoint!!, PyBackend.endPoint!!)
        totalRequests += 2

        val totalTravelCostString = "${PyBackend.totalTravelCost} E£"
        binding?.tvTotalTravelCost?.text = totalTravelCostString
    }

    //Draw Buses route only
    private fun drawDirectionsBus(context: GeoApiContext?, waypoints: ArrayList<com.google.maps.model.LatLng>, color: Int){

        // Request directions with waypoints
        val request = DirectionsApi.newRequest(context)
            .origin(waypoints.firstOrNull()) // origin
            .destination(waypoints.last()) // destination
            .mode(TravelMode.DRIVING)
            .waypoints(*waypoints.toTypedArray())

        // Execute the request asynchronously
        request.setCallback(object : PendingResult.Callback<DirectionsResult> {
            override fun onResult(result: DirectionsResult) {

                runOnUiThread {
                    // Process the result here
                    val route = result.routes[0] // Assuming there's only one route

                    // Extract and accumulate the travel time of each leg
                    route.legs.forEach { leg ->
                        totalEstimatedTravelTimeInSeconds += leg.duration.inSeconds
                    }

                    // Draw the polyline on the map
                    val polylineOptions = PolylineOptions()
                    polylineOptions.addAll(
                        route.overviewPolyline.decodePath().map { LatLng(it.lat, it.lng) })

                    polylineOptions.color(color)
                    polylineOptions.width(10f)
                    mGoogleMap.addPolyline(polylineOptions)

                    // Move camera to fit the entire route
                    val builder = LatLngBounds.builder()
                    for (point in route.overviewPolyline.decodePath()
                        .map { LatLng(it.lat, it.lng) }) {
                        builder.include(point)
                    }

                    for (stop in waypoints) {
                        val circleOptions = CircleOptions()
                            .center(LatLng(stop.lat, stop.lng))
                            .radius(20.0)
                            .fillColor(Color.WHITE)
                            .strokeColor(Color.DKGRAY)
                            .strokeWidth(0.5f)
                        mGoogleMap.addCircle(circleOptions)
                    }
                    val bounds = builder.build()
                    mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 300))
                }
                completedRequests++
                calculateTravelTime()
            }

            override fun onFailure(e: Throwable) {
                // Handle failure here
                Log.e("DirectionsRequest", "Failed to get directions: ${e.message}")
            }
        })
    }

    //Draw Route for walking
    private fun drawDirectionsWalk(context: GeoApiContext?, startPoint: com.google.maps.model.LatLng, endPoint: com.google.maps.model.LatLng){
        // Request directions with start and end points only
        val request = DirectionsApi.newRequest(context)
            .origin(startPoint) // Set the start point
            .destination(endPoint) // Set the end point
            .mode(TravelMode.WALKING)

        // Execute the request asynchronously
        request.setCallback(object : PendingResult.Callback<DirectionsResult> {
            override fun onResult(result: DirectionsResult) {

                runOnUiThread {
                    // Process the result here
                    val route = result.routes[0] // Assuming there's only one route

                    // Extract and accumulate the travel time of each leg
                    route.legs.forEach { leg ->
                        totalEstimatedTravelTimeInSeconds += leg.duration.inSeconds
                    }

                    // Draw the points on the map
                    for (point in route.overviewPolyline.decodePath().map { LatLng(it.lat, it.lng) }) {
                        val circleOptions = CircleOptions()
                            .center(point)
                            .radius(2.0) // radius in meters
                            .strokeColor(Color.CYAN)
                            .strokeWidth(2.0f)
                            .fillColor(Color.WHITE)
                        mGoogleMap.addCircle(circleOptions)
                    }

                    // Move camera to fit the entire route
                    val builder = LatLngBounds.builder()
                    for (point in route.overviewPolyline.decodePath()
                        .map { LatLng(it.lat, it.lng) }) {
                        builder.include(point)
                    }
                }
                completedRequests++
                calculateTravelTime()
            }

            override fun onFailure(e: Throwable) {
                // Handle failure here
                Log.e("DirectionsRequest", "Failed to get directions: ${e.message}")
            }
        })
    }

    private fun calculateTravelTime() {
        if (completedRequests == totalRequests) {
            var totalTravelTimeInMinutes = totalEstimatedTravelTimeInSeconds / 60
            // Add extra 10 minutes as waiting time for the arrival of each transport mode
            totalTravelTimeInMinutes += busCount * 10

            val hours = totalTravelTimeInMinutes / 60
            val minutes = totalTravelTimeInMinutes % 60

            // Generate the formatted string
            val formattedTime = if (hours > 0) {
                "$hours hours, $minutes mins"
            } else {
                "$minutes mins"
            }
            binding?.tvTotalTravelTime?.text = formattedTime
        }
    }

    override fun onDestroy(){
        super.onDestroy()
        binding = null
        totalEstimatedTravelTimeInSeconds = 0
        totalRequests = 0
        completedRequests = 0
        PyBackend.resetVariables()
        RouteColorManager.resetColorIndex()
    }
}


