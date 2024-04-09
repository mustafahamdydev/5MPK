package com.fivempk.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.MenuItem
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
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.fivempk.utils.PyBackend
import com.fivempk.R
import com.fivempk.databinding.ActivityMapsBinding
import com.fivempk.firebase.FireBaseClass
import com.fivempk.models.User
import com.fivempk.utils.RouteColorManager
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
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
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MapsActivity : AppCompatActivity() , OnMapReadyCallback,NavigationView.OnNavigationItemSelectedListener {

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
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var auth: FirebaseAuth


    @SuppressLint("SuspiciousIndentation")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        enableEdgeToEdge()



        MobileAds.initialize(this) {}
        loadBanner()
        auth=FirebaseAuth.getInstance()
        val currentUser = auth.currentUser


        if (currentUser != null) {
            FireBaseClass().signInUser(this)
        }else{
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding?.customLocationButton?.setOnClickListener {
            autocompleteFragment.setText("Your Current Location")
            placeLatLng=null
            binding!!.location.visibility= View.VISIBLE
            if (ActivityCompat.checkSelfPermission(
                    this@MapsActivity,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { location: Location? ->
                        // Got last known location. In some rare situations, this can be null.
                        location?.let {
                            locationLat = location.latitude
                            locationLon = location.longitude
                            val currentLatLng = LatLng(location.latitude, location.longitude)
                            mGoogleMap!!.animateCamera(
                                CameraUpdateFactory.newLatLngZoom(
                                    currentLatLng,
                                    15f
                                )
                            )
                        }
                    }
            } else {
                requestPermission()
            }
        }



        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        executeAfterDelay()
        ///bottomSheet
        binding?.mapsBottomSheet?.viewTreeObserver?.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                binding?.mapsBottomSheet?.viewTreeObserver?.removeOnGlobalLayoutListener(this)
                val sheet = binding?.mapsBottomSheet
                sheet?.let { bottomSheetBehavior = BottomSheetBehavior.from(it) }
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                bottomSheetBehavior.peekHeight = 1200
                bottomSheetBehavior.addBottomSheetCallback(bottomSheetCallback)
                bottomSheetBehavior.isDraggable = true
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

        binding!!.navView.setNavigationItemSelectedListener (this)

        val navHeaderLayout = findViewById<NavigationView>(R.id.nav_view).getHeaderView(0)
        closeDrawerButton = navHeaderLayout.findViewById(R.id.close_drawer)
        closeDrawerButton!!.setOnClickListener {
            drawerLayout!!.closeDrawer(
                GravityCompat.START
            )
        }

        //New Places API


        /// places api
        Places.initialize(applicationContext,getString(R.string.Google_Api_Key))
        autocompleteFragment = supportFragmentManager.findFragmentById(R.id.autocomplete_fragment) as AutocompleteSupportFragment
            // Apply the filter to the Autocomplete fragment
            autocompleteFragment.setCountries("EG") // Optionally set the country to restrict results
            autocompleteFragment.setHint("Enter Location")
            autocompleteFragment.setPlaceFields(listOf(Place.Field.ID,Place.Field.NAME,Place.Field.ADDRESS,Place.Field.LAT_LNG))
            autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener{
                override fun onError(p0: Status) {
                    Toast.makeText(this@MapsActivity, "Please Select Place", Toast.LENGTH_SHORT).show()
                }

                override fun onPlaceSelected(place: Place) {
                    binding!!.location.visibility= View.GONE
                    val latLng: LatLng? = place.latLng
                    placeLatLng = place.latLng
                    zoomOnMap(latLng!!)
                    mGoogleMap!!.addMarker(MarkerOptions().position(latLng).title("Your location"))
                }

            })
            defaultCompleteFragment = supportFragmentManager.findFragmentById(R.id.des_autocomplete_fragment) as AutocompleteSupportFragment
            defaultCompleteFragment.setPlaceFields(listOf(Place.Field.ID,Place.Field.NAME,Place.Field.ADDRESS,Place.Field.LAT_LNG))
            defaultCompleteFragment.setHint("Enter Destination")
            defaultCompleteFragment.setCountries("EG")
            defaultCompleteFragment.setOnPlaceSelectedListener(object :PlaceSelectionListener{
                override fun onError(p0: Status) {
                    Toast.makeText(this@MapsActivity, "Please Select A Place", Toast.LENGTH_SHORT).show()
                }

                override fun onPlaceSelected(p0: Place) {
                    val latLng: LatLng? = p0.latLng
                    destinationLatLng = p0.latLng
                    zoomOnMap(latLng!!)
                    mGoogleMap!!.addMarker(MarkerOptions().position(latLng).title("Your Destination"))
                    setDrawingPLaceDistance()
                }
            })

        val mapFragment=supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    //Bottom Sheet Function


    // Determine the screen width (less decorations) to use for the ad width.
    // If the ad hasn't been laid out, default to the full screen width.

    private fun loadBanner() {
        // Step 1: Create an inline adaptive banner ad size using the activity context.
        val adSize = AdSize.getCurrentOrientationInlineAdaptiveBannerAdSize(this, 320)

        // Step 2: Create banner using activity context and set the inline ad size and ad unit ID.
        val adView = AdView(this)
        adView.adUnitId = "ca-app-pub-9050992379264255/2491285948"
        adView.setAdSize(adSize)

        // Step 3: Load an ad.
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)

         val adContainer = findViewById<LinearLayout>(R.id.adView)
         adContainer.addView(adView)
    }






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

    fun setDrawingPLaceDistance() {
        binding?.submit?.setOnClickListener {
            // Launch a new coroutine in background and continue
            lifecycleScope.launch(Dispatchers.IO) {
                val output = PyBackend.getRoute(
                    getPlaceLat()!!,
                    getPlaceLon()!!,
                    getDestLat()!!,
                    getDestLon()!!
                )
                // Switch to the Main Thread to update the UI
                withContext(Dispatchers.Main) {
                    if (output == null) {
                        Toast.makeText(this@MapsActivity, "output = null", Toast.LENGTH_SHORT).show()
                    } else {
                        PyBackend.startPoint =
                            com.google.maps.model.LatLng(getPlaceLat()!!, getPlaceLon()!!)
                        PyBackend.endPoint =
                            com.google.maps.model.LatLng(getDestLat()!!, getDestLon()!!)
                        val intent = Intent(this@MapsActivity, ResultActivity::class.java)
                        RouteColorManager.resetColorIndex()
                        startActivity(intent)
                        finish()
                    }
                }
            }
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
            delay(1000)
            if (ActivityCompat.checkSelfPermission(
                    this@MapsActivity,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { location: Location? ->
                        // Got last known location. In some rare situations, this can be null.
                        location?.let {
                            locationLat = location.latitude
                            locationLon = location.longitude
                            val currentLatLng = LatLng(location.latitude, location.longitude)
                            mGoogleMap!!.animateCamera(
                                CameraUpdateFactory.newLatLngZoom(
                                    currentLatLng,
                                    15f
                                )
                            )
                        }
                    }
            } else {
                requestPermission()
            }
        }
    }


    override fun onMapReady(googleMap: GoogleMap) {
        autocompleteFragment.setText("Your Current Location")
        mGoogleMap= googleMap
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
            mGoogleMap!!.isMyLocationEnabled = true
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.logout -> {
                signOut()
                Toast.makeText(this, "Signed Out", Toast.LENGTH_SHORT).show()
            }
//            R.id.nav_item2 -> {
//                // Handle item 2 click
//            }
            // Add more cases for other menu items as needed
        }

        // Close the drawer after handling the item click
        binding!!.main.closeDrawer(GravityCompat.START)
        return true
    }


    fun updateNavigationUserDetails(user: User ){
            Glide
                .with(this)
                .load(user.image)
                .centerCrop()
                .placeholder(R.drawable.background_witg_vector)
                .into(binding!!.userImage)

        binding!!.tvUserName.text= user.name
        binding!!.tvUserEmail.text= user.email
    }


    private fun signOut() {
        auth.signOut()
         startActivity(Intent(this, SignInActivity::class.java))
         finish() // If you want to finish the current activity after sign-out
    }


}
