package com.wanderer

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.MovementMethod
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import android.widget.LinearLayout
import android.window.OnBackInvokedDispatcher
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
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
import com.wanderer.databinding.ActivityResultBinding
import java.util.ArrayList
import kotlin.random.Random

class ResultActivity : AppCompatActivity(), OnMapReadyCallback {

    private var binding: ActivityResultBinding? = null
    private lateinit var map: GoogleMap
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>
    private val cairoBounds = LatLngBounds(
        //South-West bounds (south:Helwan, West:October)
        LatLng(29.80, 30.85),
        //North-East bounds (North:10th of Ramadan, East:10th of Ramadan)
        LatLng(30.35, 31.88)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        enableEdgeToEdge()

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
            PyBackend.resetVariables()
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
                map.uiSettings.isScrollGesturesEnabled = false
                map.uiSettings.isZoomGesturesEnabled = false
            } else {
                map.uiSettings.isScrollGesturesEnabled = true
                map.uiSettings.isZoomGesturesEnabled = true
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap){
        map = googleMap
        map.clear()
        val apiKey: String = getString(R.string.Google_Api_Key)

        // Set up the GeoApiContext with your API key
        val context = GeoApiContext.Builder()
            .apiKey(apiKey)
            .build()

        //center the camera within the given bounds
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(cairoBounds.center, 10.0f))

        //restricts user movement to the set bounds of Cairo
        map.setLatLngBoundsForCameraTarget(cairoBounds)

        if (PyBackend.routeType == 1){
            val firstPoint = PyBackend.multiRouteCoordinatesList?.first()?.first()
            val lastPoint = PyBackend.multiRouteCoordinatesList?.last()?.last()

            drawDirectionsWalk(context, PyBackend.startPoint!!,firstPoint!!)
            PyBackend.multiRouteCoordinatesList?.forEach { busRoute ->
                drawDirectionsBus(context,busRoute)
            }
            drawDirectionsWalk(context, lastPoint!!, PyBackend.endPoint!!)

            binding?.tvBtmSheet?.text = PyBackend.routeStopsList.toString()
            binding?.tvBtmSheet?.movementMethod = ScrollingMovementMethod()
        } else{
            drawDirectionsBus(context, PyBackend.singleRouteCoordinatesList!!)
            drawDirectionsWalk(context, PyBackend.startPoint!!,PyBackend.singleRouteCoordinatesList!!.first())
            drawDirectionsWalk(context, PyBackend.singleRouteCoordinatesList!!.last(), PyBackend.endPoint!!)
            binding?.tvBtmSheet?.text = PyBackend.routeName.toString()
        }

    }

    //Draw Buses route only
    private fun drawDirectionsBus(context: GeoApiContext?, waypoints: ArrayList<com.google.maps.model.LatLng>){

        // Request directions with waypoints
        val request = DirectionsApi.newRequest(context)
            .origin(waypoints.firstOrNull()) // Example origin
            .destination(waypoints.last()) // Example destination
            .mode(TravelMode.DRIVING)
            .waypoints(*waypoints.toTypedArray())

        // Execute the request asynchronously
        request.setCallback(object : PendingResult.Callback<DirectionsResult> {
            override fun onResult(result: DirectionsResult) {

                runOnUiThread {
                    // Process the result here
                    val route = result.routes[0] // Assuming there's only one route

                    // Draw the polyline on the map
                    val polylineOptions = PolylineOptions()
                    polylineOptions.addAll(
                        route.overviewPolyline.decodePath().map { LatLng(it.lat, it.lng) })

                    val rnd = Random
                    val color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256))

                    polylineOptions.color(color)
                    polylineOptions.width(10f)
                    map.addPolyline(polylineOptions)

                    // Move camera to fit the entire route
                    val builder = LatLngBounds.builder()
                    for (point in route.overviewPolyline.decodePath()
                        .map { LatLng(it.lat, it.lng) }) {
                        builder.include(point)
                    }
                    val bounds = builder.build()
                    map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))
                }
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

                    // Draw the points on the map
                    for (point in route.overviewPolyline.decodePath().map { LatLng(it.lat, it.lng) }) {
                        val circleOptions = CircleOptions()
                            .center(point)
                            .radius(2.0) // radius in meters
                            .strokeColor(Color.CYAN)
                            .strokeWidth(2.0f)
                            .fillColor(Color.WHITE)
                        map.addCircle(circleOptions)
                    }

                    // Move camera to fit the entire route
                    val builder = LatLngBounds.builder()
                    for (point in route.overviewPolyline.decodePath()
                        .map { LatLng(it.lat, it.lng) }) {
                        builder.include(point)
                    }
                    val bounds = builder.build()
                    map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))
                }
            }

            override fun onFailure(e: Throwable) {
                // Handle failure here
                Log.e("DirectionsRequest", "Failed to get directions: ${e.message}")
            }
        })
    }

    override fun onDestroy(){
        super.onDestroy()
        binding = null
    }
}

