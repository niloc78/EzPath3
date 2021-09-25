package com.example.ezpath3.model

data class Result(
    var business_status : String,
    var formatted_address : String,
    var geometry : HashMap<String, HashMap<String, Any>>,
    var icon : String,
    var icon_background_color : String,
    var icon_mask_base_uri : String,
    var name : String,
    var opening_hours : HashMap<String, Boolean>,
    var place_id : String,
    var rating : Double,
    var types : ArrayList<String>,
    var user_ratings_total : Int,
    var price_level : Int
)