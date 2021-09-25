package com.example.ezpath3.util

import java.io.Serializable

interface PreferencesManager {

    fun storeSet(setName : String, set : HashSet<String>) : Boolean

    fun loadSet(setName : String) : Set<String>

    fun storeCurrLocation(currPlaceInfo : HashMap<String, Serializable>)

    fun getCurrLocation() : HashMap<String, Serializable>?

    fun deleteSet(setName: String) : Boolean

    fun storeSearchPreferences(searchPrefs : HashMap<String, Any>) : Boolean

    fun getSearchPreferences() : HashMap<String, Any>?

    fun clear() : Boolean

    fun getAllSets() : ArrayList<String>

    fun isEmpty() : Boolean

    fun getAll() : Map<String, *>

}