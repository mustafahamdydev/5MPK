package com.fivempk.utils

import androidx.annotation.DrawableRes
import com.fivempk.R

data class Bus(
    val name: String,
    val stops: List<String>
)
data class Stop(
    val name: String,
    val busName: String
)


