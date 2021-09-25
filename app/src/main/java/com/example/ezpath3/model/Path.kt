package com.example.ezpath3.model

import com.google.android.gms.maps.model.LatLng

data class Path(
    var geocoded_waypoints : ArrayList<HashMap<String, Any>>,
    var routes : ArrayList<HashMap<String, Any>>,
    var stringPoly : String,
    var decodedPoly : MutableList<LatLng>?,
    var status : String
)