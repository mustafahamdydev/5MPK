package com.fivempk.utils

import com.chaquo.python.PyObject
import com.chaquo.python.Python
import com.google.maps.model.LatLng

object PyBackend {

    var startPoint: LatLng? = null
    var endPoint: LatLng? = null
    var multiRouteCoordinatesList: ArrayList<ArrayList<LatLng>>? = null
    var routeStopsList : ArrayList<List<String>>? = null
    var totalTravelCost : Int = 0

    fun getRoute(startLat: Double, startLon: Double, endLat: Double, endLon: Double): String?{
        try {
            val py = Python.getInstance()
            val pyObj: PyObject = py.getModule("Algorithm")
            val output: PyObject = pyObj.callAttr("main", startLat, startLon, endLat, endLon)

            val stringOutput = output.callAttr("__getitem__", 0).asList().toString()

            val pair = getBusAndStopsList(stringOutput)
            routeStopsList = pair.first
            multiRouteCoordinatesList = pair.second

            return stringOutput

        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
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
            result.add(arrayListOf(stopName, busName, routeId))
            lastLatLng = latLng
        }
        val routeCoordinates = ArrayList(routes.values)
        return Pair(result, routeCoordinates)
    }

    fun getBusPrice (name : String) : Int{
        return if (name.contains("CTA")){
            Constants.CTA_TICKET_PRICE
        }else if (name.contains("Minibus")){
            Constants.MINIBUS_TICKET_PRICE
        }else if (name.contains("Microbus")){
            Constants.MICROBUS_TICKET_PRICE
        }else if(name.contains("Tomnaya")){
            Constants.TOMNAYA_TICKET_PRICE
        }else if (name.contains("Box")){
            Constants.BOX_TICKET_PRICE
        }else if (name.contains("Agyad")){
            Constants.AGYAD_TICKET_PRICE
        } else{
            0
        }
    }

    fun resetVariables(){
        startPoint = null
        endPoint = null
        multiRouteCoordinatesList = null
        routeStopsList = null
        totalTravelCost = 0
    }
}
