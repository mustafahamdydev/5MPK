package com.wanderer

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.wanderer.databinding.ActivityMapsBinding
import com.google.android.gms.common.api.Status
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MapsActivity : AppCompatActivity() , OnMapReadyCallback {

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>
    private var closeDrawerButton : ImageButton? = null
    private var drawerLayout: DrawerLayout? = null
    private var openDrawerButton: FloatingActionButton? = null
    private var placeLatLng: LatLng? = null
    private var destinationLatLng : LatLng? = null
    private var locationLat: Double? = null
    private var locationLon: Double? = null
    private  var binding: ActivityMapsBinding? = null
    private var mGoogleMap:GoogleMap?=null
    private lateinit var autocompleteFragment: AutocompleteSupportFragment
    private lateinit var defaultCompleteFragment :AutocompleteSupportFragment

    @SuppressLint("SuspiciousIndentation")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        enableEdgeToEdge()

        binding!!.location.setOnClickListener(){
            autocompleteFragment.setText("Your Current Location")
            placeLatLng=null

        }

        binding?.customLocationButton?.setOnClickListener {
            // Perform actions when the custom location button is clicked
            // For example, you can request location updates or animate the camera to the user's location
            // Replace the following code with your desired functionality
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mGoogleMap!!.isMyLocationEnabled = true
                val location = mGoogleMap!!.myLocation
                locationLat = location.latitude
                locationLon = location.longitude
                val currentLatLng = LatLng(location.latitude, location.longitude)
                mGoogleMap!!.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
            } else {
                // Handle the case where location permission is not granted
                // You might want to request permission here
            }
        }

        ///bottomSheet
        binding?.mapsBottomSheet?.viewTreeObserver?.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                binding?.mapsBottomSheet?.viewTreeObserver?.removeOnGlobalLayoutListener(this)
                val sheet = binding?.mapsBottomSheet
                sheet?.let { bottomSheetBehavior = BottomSheetBehavior.from(it) }
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                bottomSheetBehavior.peekHeight = 1000
                bottomSheetBehavior.addBottomSheetCallback(bottomSheetCallback)
            }
        })

        ///Drawer setup
        drawerLayout = findViewById(R.id.main)
        openDrawerButton = findViewById(R.id.btn_open_drawer)
        openDrawerButton!!.setOnClickListener {
            drawerLayout!!.openDrawer(
                GravityCompat.START
            )
        }

        val navHeaderLayout = findViewById<NavigationView>(R.id.nav_view).getHeaderView(0)
        closeDrawerButton = navHeaderLayout.findViewById(R.id.close_drawer)
        closeDrawerButton!!.setOnClickListener(){
            drawerLayout!!.closeDrawer(
                GravityCompat.START
            )
        }

        /// places api
        Places.initialize(applicationContext,getString(R.string.Google_Api_Key))
        autocompleteFragment = supportFragmentManager.findFragmentById(R.id.autocomplete_fragment)
                as AutocompleteSupportFragment

            // Apply the filter to the Autocomplete fragment
            autocompleteFragment.setCountries("EG") // Optionally set the country to restrict results
            autocompleteFragment.setHint("Enter Location")
            autocompleteFragment.setPlaceFields(listOf(Place.Field.ID,Place.Field.NAME,Place.Field.ADDRESS,Place.Field.LAT_LNG))
            autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener{
                override fun onError(p0: Status) {
                    Toast.makeText(this@MapsActivity, "Error in Search", Toast.LENGTH_SHORT).show()
                }

                override fun onPlaceSelected(place: Place) {
                    val latLng: LatLng? = place.latLng
                    placeLatLng = place.latLng
                    val placeName : String? = place.name
                    Toast.makeText(this@MapsActivity, "$placeLatLng", Toast.LENGTH_SHORT).show()
                    Log.e("","$placeName")
                    zoomOnMap(latLng!!)
                    mGoogleMap!!.addMarker(MarkerOptions().position(latLng).title("My location"))
                }

            })
            defaultCompleteFragment = supportFragmentManager.findFragmentById(R.id.des_autocomplete_fragment) as AutocompleteSupportFragment
            defaultCompleteFragment.setPlaceFields(listOf(Place.Field.ID,Place.Field.NAME,Place.Field.ADDRESS,Place.Field.LAT_LNG))
            defaultCompleteFragment.setHint("Enter Destination")
            defaultCompleteFragment.setCountries("EG")
            defaultCompleteFragment.setOnPlaceSelectedListener(object :PlaceSelectionListener{
                override fun onError(p0: Status) {
                    Toast.makeText(this@MapsActivity, "Error in Search", Toast.LENGTH_SHORT).show()
                }

                override fun onPlaceSelected(p0: Place) {
                    val latLng: LatLng? = p0.latLng
                    destinationLatLng = p0.latLng
                    zoomOnMap(latLng!!)
                    mGoogleMap!!.addMarker(MarkerOptions().position(latLng).title("My Destination"))
                    setDrawingPLaceDistance()
                }
            })

        val mapFragment=supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    //Bottom Sheet Function
    private val bottomSheetCallback = object : BottomSheetBehavior.BottomSheetCallback() {
        override fun onStateChanged(bottomSheet: View, newState: Int) {
            // Handle bottom sheet state changes here
        }
        override fun onSlide(bottomSheet: View, slideOffset: Float) {
            // If the bottom sheet is sliding, consume the touch event to prevent interference with the map
        }
    }

    private fun getDestLat(): Double? {
        return destinationLatLng?.latitude
    }
    private fun getDestLon(): Double? {
        return destinationLatLng?.longitude
    }

    private fun getPlaceLat(): Double? {
        return if (placeLatLng?.latitude==null){
            locationLat
        }else{
            placeLatLng?.latitude
        }
    }
    private fun getPlaceLon(): Double? {
        return if (placeLatLng?.longitude==null){
            locationLon
        }else{
            placeLatLng?.longitude
        }
    }


    fun setDrawingPLaceDistance(){
       binding?.submit?.setOnClickListener {
               val output = PyBackend.getRoute(this@MapsActivity, getPlaceLat()!!, getPlaceLon()!!, getDestLat()!!, getDestLon()!!)
               if (output == null){
                   Toast.makeText(this@MapsActivity, "output = null", Toast.LENGTH_SHORT).show()
               }else{
                   PyBackend.startPoint = com.google.maps.model.LatLng(getPlaceLat()!!, getPlaceLon()!!)
                   PyBackend.endPoint = com.google.maps.model.LatLng(getDestLat()!!, getDestLon()!!)
               }
               val intent = Intent(this@MapsActivity, ResultActivity::class.java)
               startActivity(intent)
               finish()

       }
    }

    private fun zoomOnMap(latLng: LatLng){

        val  newLatLngZoom = CameraUpdateFactory.newLatLngZoom(latLng,20f)
        mGoogleMap?.animateCamera(newLatLngZoom)
    }

    private fun executeAfterDelay() {
        // Launch a coroutine in the Main Dispatcher
        CoroutineScope(Dispatchers.Main).launch {
            // Add a delay of 1 second (adjust as needed)
            delay(3000)

            if (ActivityCompat.checkSelfPermission(this@MapsActivity,android.Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED){
                mGoogleMap!!.isMyLocationEnabled = true
                val location = mGoogleMap!!.myLocation
                locationLat = location.latitude
                locationLon = location.longitude


            }else{
                Toast.makeText(this@MapsActivity, "hehehe", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mGoogleMap= googleMap

        val myHome = LatLng (30.033849, 31.463658)
        mGoogleMap!!.addMarker(MarkerOptions().position(myHome).title("My Home"))
        mGoogleMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(myHome,19f))
        mGoogleMap!!.setPadding(0,10,0,50)
        mGoogleMap!!.uiSettings.apply {
            isMapToolbarEnabled = false
            isMyLocationButtonEnabled = false

        }
        checkLocationPermission()
    }
    private fun checkLocationPermission(){
        if (
            ContextCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ){
            mGoogleMap!!.isMyLocationEnabled = true
            Toast.makeText(this, "Already Enabled", Toast.LENGTH_LONG).show()
        }else{
            requestPermission()
        }
    }

    private fun requestPermission (){
        ActivityCompat.requestPermissions(
            this,
            arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
            1
        )
    }

    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode != 1){
            return
        }
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            Toast.makeText(this, "GRANTED", Toast.LENGTH_SHORT).show()
            mGoogleMap!!.isMyLocationEnabled = true
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

}
