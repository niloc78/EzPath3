package com.example.ezpath3.retrofit

data class DirectionsResults(
    var geocoded_waypoints : ArrayList<HashMap<String, Any>>,
    var routes : ArrayList<HashMap<String, Any>>,
    var status : String
)