package com.example.ezpath3.retrofit

import com.example.ezpath3.model.Path
import com.example.ezpath3.util.EntityMapper
import com.google.maps.android.PolyUtil
import javax.inject.Inject

class NetworkDirectionsResultsMapper
@Inject
constructor() : EntityMapper<DirectionsResults, Path> {
    override fun mapFromEntity(entity: DirectionsResults): Path {
        // println("routes response: ${entity.routes}")
//        println("converting routes response to JSONObject...")
//        val routeJson = if (entity.routes.isNullOrEmpty()) null else JSONObject(entity.routes[0].entries.toString())
//        println("converted routes response to JSONObject: ${routeJson.toString()}")
        //println("getting strPoly...")
        //println("this was the overview_polyline: " + entity.routes[0]["overview_polyline"].toString())
        val strPoly = if (entity.routes.isNullOrEmpty() || entity.routes[0]["overview_polyline"].toString().isNullOrBlank()) "" else entity.routes[0]["overview_polyline"].toString()
            .trimStart('{')
            .trimEnd('}')
            .replace("points=", "")
        //routeJson.getJSONObject("overview_polyline").getString("points")
//            val (key,value) = it.split("=")
//            println("key, value: $key, $value")
//            key to value
//        })["points"].toString()
        //println("got strPoly: $strPoly")
        //println("converting strPoly to decodedPoly...")
        val decodedPoly = if (strPoly.isBlank()) null else PolyUtil.decode(strPoly)
        //println("finished converting strPoly to decodedPoly: $decodedPoly")
        return Path(entity.geocoded_waypoints, entity.routes, strPoly, decodedPoly, entity.status)
    }

    override fun mapToEntity(domainModel: Path): DirectionsResults {
        return DirectionsResults(domainModel.geocoded_waypoints, domainModel.routes, domainModel.status)
    }


}