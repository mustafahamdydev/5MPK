package com.fivempk.activities

import android.animation.ObjectAnimator
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.animation.OvershootInterpolator
import androidx.activity.enableEdgeToEdge
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.fivempk.utils.Permissions.hasLocationPermission
import com.fivempk.utils.Permissions.requestLocationPermission
import com.fivempk.databinding.ActivityMainBinding
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions

class MainActivity : AppCompatActivity() ,EasyPermissions.PermissionCallbacks {
    private var binding: ActivityMainBinding? = null
    private var requestState : Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (hasLocationPermission(this)){
            requestState = false
        }else{
            requestState = true
            requestLocationPermission(this)
        }

        installSplashScreen().apply {

//            setKeepOnScreenCondition {
//                requestState
//            }
            setOnExitAnimationListener{
                screen -> val zoomX = ObjectAnimator.ofFloat(
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

        intent = Intent(this, SignInActivity::class.java)
        startActivity(intent)
        finish()






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
        intent = Intent(this, MapsActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onPermissionsDenied(p0: Int, p1: MutableList<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this,p1)){
            AppSettingsDialog.Builder(this).build().show()

        }else{
            requestLocationPermission(this)
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}
