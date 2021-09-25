package com.example.ezpath3.repository


import android.content.SharedPreferences
import android.util.Log
import com.example.ezpath3.util.DataState
import com.example.ezpath3.util.PreferencesManager
import java.io.Serializable
import javax.inject.Inject

class PreferencesManagerImpl
@Inject
constructor(private val prefs : SharedPreferences) : PreferencesManager {
    override fun storeSet(setName: String, set: HashSet<String>) : Boolean {
        prefs.edit()
            .putStringSet(setName, set)
            .apply()
        return when (prefs.getStringSet(setName, null) != null) {
            true -> true //successfully added
            else -> false
        }
    }

    override fun loadSet(setName: String): Set<String> {
        return prefs.getStringSet(setName, setOf())!!
    }

    override fun getAllSets(): ArrayList<String> {
        val setArr : ArrayList<String> = ArrayList()
        prefs.all.forEach {
            if(it.value is Set<*>) setArr.add(it.key)
        }
        return setArr
    }

    override fun storeCurrLocation(currPlaceInfo: HashMap<String, Serializable>) {
        prefs.edit().apply {
            //println("storing currPlaceId...")
            putString("currPlaceId", currPlaceInfo["id"].toString())
            //println("storing currPlaceAddress...")
            putString("currPlaceAddress", currPlaceInfo["address"].toString())
            //("storing currPlaceLat...")
            putFloat("currPlaceLat", (currPlaceInfo["latLng"] as DoubleArray)[0].toFloat())
            //println("storing currPlaceLng...")
            putFloat("currPlaceLng", (currPlaceInfo["latLng"] as DoubleArray)[1].toFloat())
        }.apply()
        //println("finished applying storeCurrLocation")
    }

    override fun getCurrLocation(): HashMap<String, Serializable>? {
        return when(currPlaceIsNotNull()) {
            true -> hashMapOf(
                "id" to prefs.getString("currPlaceId", "") as Serializable,
                "latLng" to doubleArrayOf(prefs.getFloat("currPlaceLat", 0F).toDouble(), prefs.getFloat("currPlaceLng", 0F).toDouble()) as Serializable,
                "address" to prefs.getString("currPlaceAddress", "") as Serializable
            )
            else -> {
                //Log.d("getCurrLocation", "was null")
                null
            }
        }
    }

    override fun deleteSet(setName: String): Boolean { // check after if successfully removed
        prefs.edit().apply {
            remove(setName)
        }.apply()
        return when(prefs.getStringSet(setName, null) == null) {  // successfully removed
            true -> true
            else -> false
        }
    }

    override fun storeSearchPreferences(searchPrefs: HashMap<String, Any>): Boolean { // rating, price_level, radius, chip_rating
        prefs.edit().apply {
            putFloat("rating", searchPrefs["rating"] as Float)
            putInt("price_level", searchPrefs["price_level"] as Int)
            putFloat("radius", searchPrefs["radius"] as Float)
            putBoolean("chip_rating", searchPrefs["chip_rating"] as Boolean)
        }.apply()
        return when(searchPrefsExist()) {
            true -> true
            else -> false
        }
    }



    override fun getSearchPreferences(): HashMap<String, Any>? {
        return when(searchPrefsExist()) {
            true -> hashMapOf(
                "rating" to prefs.getFloat("rating", Float.NaN),
                "price_level" to prefs.getInt("price_level", 0),
                "radius" to prefs.getFloat("radius", Float.NaN),
                "chip_rating" to prefs.getBoolean("chip_rating", false)
            )
            else -> null
        }
    }

    override fun clear() : Boolean {
        prefs.edit().clear().apply()
        return prefs.all.isEmpty()
    }

    override fun isEmpty(): Boolean {
        return prefs.all.isEmpty()
    }

    override fun getAll(): Map<String, *> {
        return prefs.all
    }


    private fun searchPrefsExist() : Boolean {
        return !prefs.getFloat("rating", Float.NaN).isNaN()
                && prefs.getInt("price_level", -1) != -1
                && !prefs.getFloat("radius", Float.NaN).isNaN()
    }

    private fun currPlaceIsNotNull() : Boolean {
        return prefs.getString("currPlaceId", null) != null
                && prefs.getString("currPlaceAddress", null) != null
                && !prefs.getFloat("currPlaceLat", Float.NaN).isNaN()
                && !prefs.getFloat("currPlaceLng", Float.NaN).isNaN()
    }
}