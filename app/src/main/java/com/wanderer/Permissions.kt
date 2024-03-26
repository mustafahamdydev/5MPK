package com.wanderer

import android.content.Context
import com.wanderer.Constants.PERMISSIONS_LOCATION_CODE
import pub.devrel.easypermissions.EasyPermissions

object Permissions {

    fun hasLocationPermission (context: Context)=
        EasyPermissions.hasPermissions(
            context,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        )


    fun requestLocationPermission(activity: MainActivity) {
        EasyPermissions.requestPermissions(
            activity,
            "This Application cannot work without Location Permission",
            PERMISSIONS_LOCATION_CODE,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        )
    }


}