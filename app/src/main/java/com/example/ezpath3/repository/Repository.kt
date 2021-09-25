package com.example.ezpath3.repository

import android.util.Log
import com.example.ezpath3.BuildConfig.PLACES_API_KEY
import com.example.ezpath3.model.ErrandResults
import com.example.ezpath3.model.Path
import com.example.ezpath3.retrofit.GoogleApiService
import com.example.ezpath3.retrofit.NetworkDirectionsResultsMapper
import com.example.ezpath3.retrofit.NetworkErrandResultsMapper
import com.example.ezpath3.util.DataState
import com.example.ezpath3.util.PreferencesManager
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.Serializable
import java.lang.Exception
import javax.inject.Inject

class Repository
@Inject
constructor(
    private val retrofit : GoogleApiService,
    private val networkErrandResultsMapper: NetworkErrandResultsMapper,
    private val networkDirectionsResultsMapper: NetworkDirectionsResultsMapper,
    private val prefsManager : PreferencesManager
)
{

    suspend fun getErrandResults(query : String, location : String) : Flow<DataState<ErrandResults>> = flow { //include parameters
        //emit(DataState.Loading)
        //delay(1000)
        //println("starting getErrandResults")
        try {
            //println("getting errand results from retrofit... with query $query with location $location")
            val networkErrandResults = retrofit.getErrandResults(query, location, PLACES_API_KEY) /*insert parameters*/
            //println("got retrofit errand result : ${networkErrandResults.results.joinToString()}")
            //println("mapping to errand result to domain model...")
            val errandResults = networkErrandResultsMapper.mapFromEntity(networkErrandResults)
            //println("mapped errand result to domainmodel")
            //println("emitting datastate errand result...")
            emit(DataState.Success(errandResults))
            //println("emitted datastate errand result.")
        } catch (e : Exception) {
            //println("get errand result exception thrown, probably network error")
            emit(DataState.Error(e))
        }
    }

    fun storeCurrLocation(job: Job, currPlaceInfo: HashMap<String, Serializable>) = CoroutineScope(IO + job).launch {
        //store location into into prefs using prefs manager
        //println("storing currPlaceInfo into prefs...")
        prefsManager.storeCurrLocation(currPlaceInfo)
        //println("finished storing currPlaceInfo into prefs.")
    }

    suspend fun storeSet(job : Job, setName : String, setData : HashSet<String>) : DataState<Boolean> = withContext(IO + job) {
        val stored = prefsManager.storeSet(setName, setData)
        if (stored) DataState.Success(stored) else DataState.Error(Exception())
    }

    suspend fun getCurrLocation(): Flow<DataState<HashMap<String, Serializable>?>> = flow {
       // emit(DataState.Loading)
        try {
            val currPlaceInfo = prefsManager.getCurrLocation()
            emit(DataState.Success(currPlaceInfo))
        } catch (e : Exception) {
            emit(DataState.Error(e))
        }
    }

    suspend fun retrieveSet(setName: String) : Flow<DataState<Set<String>>> = flow {
        try {
            val set = prefsManager.loadSet(setName)
            emit(DataState.Success(set))
        } catch (e : Exception) {
            emit(DataState.Error(e))
        }
    }

    suspend fun getAllSets() : Flow<DataState<ArrayList<String>>> = flow {
        try {
            val allSets = prefsManager.getAllSets()
            emit(DataState.Success(allSets))
        } catch (e : Exception) {
            emit(DataState.Error(e))
        }
    }

    suspend fun deleteSet(job : Job, setName: String) : DataState<Boolean> = withContext(IO + job) {
        val deleted = prefsManager.deleteSet(setName)
        if (deleted) DataState.Success(deleted) else DataState.Error(Exception())
    }

    suspend fun storeSearchPrefs(job: Job, searchPrefs: HashMap<String, Any>) : DataState<Boolean> = withContext(IO + job) {
        val stored = prefsManager.storeSearchPreferences(searchPrefs)
        if (stored) DataState.Success(stored) else DataState.Error(Exception())
    }

    suspend fun getSearchPrefs() : Flow<DataState<HashMap<String, Any>>> = flow {

        //println("starting getSearchPrefs...")
            val searchPrefs = prefsManager.getSearchPreferences()
            if (searchPrefs != null) {
                // println("getSearchPrefs success")
                emit(DataState.Success(searchPrefs))
            } else {
                //println("getSearchPrefs exception thrown")
                emit(DataState.Error(Exception("No search prefs in prefs")))
            }

    }


    suspend fun getDirectionsResults(srcId : String, waypointsStr : String) : Flow<DataState<Path>> = flow {
        //emit(DataState.Loading)
       // delay(1000)
        //println("launching getDirectionResults")
        try {
            //println("making call to retrofit api for directions...")
            val directionsResults = retrofit.getDirectionsResults(srcId, srcId, waypointsStr, PLACES_API_KEY) /*insert parameters*/
            //println("got directionResult: ${directionsResults.status}")
            //println("mapping to domain model...")
            val path = networkDirectionsResultsMapper.mapFromEntity(directionsResults)
            //println("finished mapping to domain model: str poly : ${path.stringPoly}")
            //println("emitting path result...")
            emit(DataState.Success(path))

        } catch (e : Exception) {
            emit(DataState.Error(e))
        }
    }

}