package com.wanderer

import androidx.annotation.DrawableRes

data class ResultScreenData(
    val busName: String,
    val stopName: String,
    @DrawableRes val img: Int
)
