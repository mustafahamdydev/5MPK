package com.fivempk.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import com.fivempk.utils.Permissions.hasLocationPermission
import com.fivempk.utils.Permissions.requestLocationPermission
import com.fivempk.databinding.ActivityMainBinding
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions

class MainActivity : AppCompatActivity() ,EasyPermissions.PermissionCallbacks {
    private var binding: ActivityMainBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        enableEdgeToEdge()

        binding?.btnStart?.setOnClickListener {
            if (hasLocationPermission(this)) {
                intent = Intent(this, SignInActivity::class.java)
                startActivity(intent)
                finish()
            }else{
                requestLocationPermission(this)
            }
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
