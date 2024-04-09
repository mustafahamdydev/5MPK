package com.fivempk.utils

import android.graphics.Color

object RouteColorManager {
    private var routeColorIndex = 0
    private var busColorIndex = 0
    private val predefinedColors = listOf(
        Color.parseColor("#b0261c"), //Red
        Color.parseColor("#1f8729"), //Green
        Color.parseColor("#1c67ad"), //Blue
        Color.parseColor("#ffad0a"), //Orange
        Color.parseColor("#671cad"), //Purple
        Color.parseColor("#f7ef05"), //Yellow
        Color.parseColor("#f705ab"), //Pink
        Color.parseColor("#18c482") //Aqua
    )

    fun getRouteNextColor(): Int {
        val color = predefinedColors[routeColorIndex % predefinedColors.size]
        routeColorIndex++
        return color
    }

    fun getBusNextColor(): Int {
        val color = predefinedColors[busColorIndex % predefinedColors.size]
        busColorIndex++
        return color
    }

    fun resetColorIndex() {
        routeColorIndex = 0
        busColorIndex = 0
    }
}