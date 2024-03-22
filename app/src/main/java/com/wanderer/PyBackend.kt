package com.wanderer

import android.content.Context
import android.util.Log
import com.chaquo.python.PyObject
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.google.maps.model.LatLng

object PyBackend {

    var startPoint: LatLng? = null
    var endPoint: LatLng? = null
    var coordinatesList: ArrayList<LatLng>? = null

    fun getRoute(context: Context, startLat: Double, startLon: Double, endLat: Double, endLon: Double): String? {
        try {
            if (!Python.isStarted()) {
                Python.start(AndroidPlatform(context))
            }
            val py = Python.getInstance()
            val pyObj: PyObject = py.getModule("Algorithm")
            val output: PyObject = pyObj.callAttr("main", startLat, startLon, endLat, endLon)

            val routeName = output.callAttr("__getitem__", 0).toString()
            val startStopInfo = output.callAttr("__getitem__", 2).asList().toString()
            val endStopInfo = output.callAttr("__getitem__", 3).asList().toString()

            return when (routeName) {
                "multi" -> {
                    val stringOutput = output.callAttr("__getitem__", 1).asList().toString()
                    coordinatesList = processMultiRoute(stringOutput)
                    Log.e("Hello", stringOutput)
                    stringOutput
                }

                else -> {
                    val stringOutput = output.callAttr("__getitem__", 1).asList().toString()
                    coordinatesList = processSingleRoute(stringOutput)
                    Log.e("Hello", stringOutput)
                    stringOutput
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    // Returns the a LatLng object of a single stop
    private fun processStop(stopInfoStr: String): LatLng? {
        try {
            val parts = stopInfoStr.removeSurrounding("[", "]").split(", ")
            val latitude = parts[0].toDouble()
            val longitude = parts[1].toDouble()
            val name = parts[2]
            Log.e("stop info", "$latitude, $longitude, $name")
            return LatLng(latitude, longitude)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    // Returns ArrayList<LatLng> for a single bus route
    private fun processSingleRoute(coordinatesListStr: String): ArrayList<LatLng> {
        val result = ArrayList<LatLng>()
        val pattern = Regex("\\((-?\\d+\\.\\d+), (-?\\d+\\.\\d+), '([^']*)'\\)")
        pattern.findAll(coordinatesListStr).forEach { matchResult ->
            val (latStr, lonStr, name) = matchResult.destructured
            val lat = latStr.toDouble()
            val lon = lonStr.toDouble()
            result.add(LatLng(lat, lon))
        }
        return result
    }

    // Returns ArrayList<LatLng> for a multi bus route
    private fun processMultiRoute(stopInfoListStr: String): ArrayList<LatLng> {
        val result = ArrayList<LatLng>()
        val pattern = Regex("\\('([^']+)', '([^']+)', (-?\\d+\\.\\d+), (-?\\d+\\.\\d+), '[^']+', '[^']+'\\)")
        pattern.findAll(stopInfoListStr).forEach { matchResult ->
            val (stopId, name, latStr, lonStr) = matchResult.destructured
            val lat = latStr.toDouble()
            val lon = lonStr.toDouble()
            result.add(LatLng(lat, lon))
        }
        return result
    }
}
