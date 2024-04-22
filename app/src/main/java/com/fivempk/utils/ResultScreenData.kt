package com.fivempk.utils

data class Bus(
    val name: String,
    val stops: List<String>,
    var color: Int? = null
)
data class Stop(
    val name: String,
    val busName: String
)


