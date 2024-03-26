package com.wanderer

import androidx.annotation.DrawableRes

data class Bus(
    val name: String,
    val stops: List<String>,
    @DrawableRes val img: Int = R.drawable.bus1
)
data class Stop(
    val name: String,
    val busName: String
)


