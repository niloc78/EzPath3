package com.example.ezpath3.retrofit

import retrofit2.http.GET
import retrofit2.http.Query

interface GoogleApiService {

    @GET("place/textsearch/json?&rankby=distance")
    suspend fun getErrandResults(@Query("query") query : String, @Query("location") location : String, @Query("key") key : String) : NetworkErrandResults

    @GET("directions/json")
    suspend fun getDirectionsResults(@Query("origin") originId : String, @Query("destination") destinationId : String, @Query("waypoints") waypointsString : String,
                                     @Query("key") key : String) : DirectionsResults


//    @GET("place/textsearch/json?")
//    fun getErrandResults(@Query("query") query : String, @Query("location") location : String, @Query("key") key : String) : Observable<ErrandResults2>
//
//    @GET("directions/json?origin=place_id:{sourceId}&destination=place_id:{sourceId}&waypoints=optimize:true|place_id:{waypoints}&key={key}")
//    fun getPolyResults(@Path("sourceId") sourceId : String, @Path("waypoints") waypoints : String, @Path("key") key : String) : Observable<JsonElement>

}
