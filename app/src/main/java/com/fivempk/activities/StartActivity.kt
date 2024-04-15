package com.fivempk.activities

import android.Manifest
import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.animation.OvershootInterpolator
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.fivempk.utils.Permissions.hasLocationPermission
import com.fivempk.utils.Permissions.requestLocationPermission
import com.fivempk.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions

class StartActivity : AppCompatActivity() ,EasyPermissions.PermissionCallbacks {

    private var binding: ActivityMainBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen().apply {
            setOnExitAnimationListener { screen ->
                val zoomX = ObjectAnimator.ofFloat(
                    screen.iconView,
                    View.SCALE_X,
                    0.4f
                )
                zoomX.interpolator = OvershootInterpolator()
                zoomX.duration = 500L
                zoomX.doOnEnd {
                    screen.remove()
                }
                val zoomY = ObjectAnimator.ofFloat(
                    screen.iconView,
                    View.SCALE_Y,
                    0.4f
                )
                zoomY.interpolator = OvershootInterpolator()
                zoomY.duration = 500L
                zoomY.doOnEnd {
                    screen.remove()
                }
                zoomX.start()
                zoomY.start()
            }

        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        enableEdgeToEdge()

        handleUserRedirection()

    }
    override fun onResume() {
        super.onResume()
        handleUserRedirection()
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        return capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    }

    private fun handleUserRedirection() {
        val user = FirebaseAuth.getInstance().currentUser
        val isConnected = isNetworkAvailable()

        if (hasLocationPermission(this)){
            if (user != null && isConnected) {
                startActivity(Intent(this, MapsActivity::class.java))
                finish()
            } else if (user != null) {
                startActivity(Intent(this, OfflineInputActivity::class.java))
                finish()
            } else if (isConnected) {
                startActivity(Intent(this, SignInActivity::class.java))
                finish()
            } else{
                yohHaveToLoginDialog()
            }
        }else{
            requestLocationPermission(this)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode,permissions,grantResults,this)
    }

    override fun onPermissionsGranted(p0: Int, p1: MutableList<String>) {
        handleUserRedirection()
    }

    override fun onPermissionsDenied(p0: Int, p1: MutableList<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this,p1)){
            AppSettingsDialog.Builder(this).build().show()
        }else{
            requestLocationPermission(this)
        }
    }

    //If the user is directed to the settings app to enable location permission but doesn't
    //This function gives the user the same location request dialog every time he comes back without enabling it
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE) {
            if (!EasyPermissions.hasPermissions(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                requestLocationPermission(this)
            }
        }
    }

    private fun yohHaveToLoginDialog(){
        val builder = AlertDialog.Builder(this)
            .setMessage("Sorry you have to be signed in to use the offline mode :(")
            .setPositiveButton("OK"){
                dialog, _ ->
                dialog.dismiss()
                finish()
            }
        val dialog = builder.create()
        dialog.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}
