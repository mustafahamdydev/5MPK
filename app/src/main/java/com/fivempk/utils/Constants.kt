package com.fivempk.utils

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds

object Constants {
    const val USERS:String = "users"
    const val PERMISSIONS_LOCATION_CODE = 1
    const val CTA_TICKET_PRICE = 8
    const val MINIBUS_TICKET_PRICE = 10
    const val AGYAD_TICKET_PRICE = 10
    const val MICROBUS_TICKET_PRICE = 7
    const val TOMNAYA_TICKET_PRICE = 5
    const val BOX_TICKET_PRICE = 5



    val cairoBounds = LatLngBounds(
        //South-West bounds (south:Helwan, West:October)
        LatLng(29.80, 30.85),
        //North-East bounds (North:10th of Ramadan, East:10th of Ramadan)
        LatLng(30.35, 31.88)
    )
}