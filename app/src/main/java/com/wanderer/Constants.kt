package com.wanderer

import android.graphics.Color
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds

object Constants {
    const val PERMISSIONS_LOCATION_CODE =1

    val predefinedColors = listOf(
        Color.parseColor("#b0261c"), //Red
        Color.parseColor("#1f8729"), //Green
        Color.parseColor("#1c67ad"), //Blue
        Color.parseColor("#ffad0a"), //orange
        Color.parseColor("#671cad"), //Purple
        Color.parseColor("#f7ef05"), //Yellow
        Color.parseColor("#f705ab"), //Pink
        Color.parseColor("#18c482") //Aqua
    )

    val cairoBounds = LatLngBounds(
        //South-West bounds (south:Helwan, West:October)
        LatLng(29.80, 30.85),
        //North-East bounds (North:10th of Ramadan, East:10th of Ramadan)
        LatLng(30.35, 31.88)
    )
}