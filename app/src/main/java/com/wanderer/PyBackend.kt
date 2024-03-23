package com.wanderer

import android.content.Context
import android.util.Log
import com.chaquo.python.PyObject
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.google.maps.model.LatLng

object PyBackend {

    var routeType : Int? = null
    var startPoint: LatLng? = null
    var endPoint: LatLng? = null
    var multiRouteCoordinatesList: ArrayList<ArrayList<LatLng>>? = null
    var singleRouteCoordinatesList: ArrayList<LatLng>? = null
    var routeName : String? = null
    var routeStopsList : ArrayList<List<String>>? = null

    fun getRoute(context: Context, startLat: Double, startLon: Double, endLat: Double, endLon: Double): String? {
        resetVariables()

        try {
            if (!Python.isStarted()) {
                Python.start(AndroidPlatform(context))
            }
            val py = Python.getInstance()
            val pyObj: PyObject = py.getModule("Algorithm")
            val output: PyObject = pyObj.callAttr("main", startLat, startLon, endLat, endLon)

            routeName = output.callAttr("__getitem__", 0).toString()

            return when (routeName) {
                "multi" -> {
                    routeType = 1
                    val stringOutput = output.callAttr("__getitem__", 1).asList().toString()
                    val pair = getBusAndStopsList(stringOutput)
                    routeStopsList = pair.first
                    multiRouteCoordinatesList = pair.second
                    Log.e("route", routeStopsList.toString())
                    stringOutput
                }
                else -> {
                    routeType = 0
                    val stringOutput = output.callAttr("__getitem__", 1).asList().toString()
                    singleRouteCoordinatesList = processSingleRoute(stringOutput)
                    stringOutput
                }
            }
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
            val (latStr, lonStr, _) = matchResult.destructured
            val lat = latStr.toDouble()
            val lon = lonStr.toDouble()
            result.add(LatLng(lat, lon))
        }
        return result
    }

    private fun getBusAndStopsList(input: String): Pair<ArrayList<List<String>>, ArrayList<ArrayList<LatLng>>> {
        val regex = Regex("""\('([^']+)', '([^']+)', ([\d.]+), ([\d.]+), '([^']+)', '([^']+)'\)""")
        val matches = regex.findAll(input)
        val routes = mutableMapOf<String, ArrayList<LatLng>>()
        val result = ArrayList<List<String>>()
        var lastLatLng: LatLng? = null
        matches.forEach { match ->
            val (_, stopName, latStr, lonStr, routeId, busName) = match.destructured
            val lat = latStr.toDouble()
            val lon = lonStr.toDouble()
            val latLng = LatLng(lat, lon)
            if (routes.containsKey(routeId)) {
                routes[routeId]!!.add(latLng)
            } else {
                val newRoute = ArrayList<LatLng>()
                lastLatLng?.let { newRoute.add(it) }
                newRoute.add(latLng)
                routes[routeId] = newRoute
            }
            result.add(arrayListOf(stopName, busName))
            lastLatLng = latLng
        }
        val routeCoordinates = ArrayList(routes.values)
        return Pair(result, routeCoordinates)
    }

    private fun resetVariables(){
        routeType = null
        startPoint = null
        endPoint = null
        multiRouteCoordinatesList = null
        singleRouteCoordinatesList = null
        routeName = null
        routeStopsList = null
    }
}
