package com.example.ezpath3.ui.viewmodel

//import androidx.hilt.Assisted
//import androidx.hilt.lifecycle.ViewModelInject
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import com.example.ezpath3.model.Result
import com.example.ezpath3.repository.Repository
import com.example.ezpath3.util.DataState
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.SphericalUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.io.Serializable
import java.lang.Exception
import javax.inject.Inject
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.LinkedHashMap
import kotlin.collections.LinkedHashSet

@HiltViewModel
class ErrandModel
@Inject
constructor(
    private val repository: Repository
) : ViewModel()
{
    //component: errandResultRetrievingState
    private val _mainDataState : MutableLiveData<DataState<String>> = MutableLiveData()
    val mainDataState : LiveData<DataState<String>>
        get() = _mainDataState
    //component: polylineresultRetrievingState
    private val _mapDataState : MutableLiveData<MutableList<LatLng>> = MutableLiveData(mutableListOf())
    val mapDataState : LiveData<MutableList<LatLng>>
        get() = _mapDataState
    //component: currplaceInfo
    private val _currPlaceInfo : MutableLiveData<DataState<HashMap<String, Serializable>?>> = MutableLiveData() //latLng, address, id,
    val currPlaceInfo : LiveData<DataState<HashMap<String, Serializable>?>>
        get() = _currPlaceInfo
    //component: user search preferences
    private val _searchPreferences : MutableLiveData<HashMap<String, Any>> = MutableLiveData(hashMapOf()) // rating (float), price_level(int), radius(float), chip_rating(bool)
    val searchPreferences : LiveData<HashMap<String, Any>>
        get() = _searchPreferences

    //component: errandName : bestResult hashmap
    private val _combinedResults : MutableLiveData<LinkedHashMap<String, Result>> = MutableLiveData(linkedMapOf())
    val combinedResults : LiveData<LinkedHashMap<String, Result>>
        get() = _combinedResults

    //component: setData
    private val _setData : MutableLiveData<ArrayList<String>> = MutableLiveData(ArrayList())
    val setData : LiveData<ArrayList<String>>
        get() = _setData

    //component: checkedErrands
    private val _checkedErrands : MutableLiveData<LinkedHashSet<String>> = MutableLiveData(linkedSetOf())
    val checkedErrands : LiveData<LinkedHashSet<String>>
        get() = _checkedErrands

    //info: default search preferences if none were stored in shared preferences
    private val defaultSearchPrefs : HashMap<String, Any> = hashMapOf("rating" to 1.0f, "price_level" to 0, "radius" to 1500f, "chip_rating" to false)


    //info: either delete an errand or add one includes: DeleteErrandResultsEvents, GetBothResultsEvents
    fun setStateEvent(mainStateEvent: MainStateEvent, errandName: String) : Job {
//        //println("setStateEvent called")
        val query = errandName.replace(" ", "+")
        val currPlaceI = (_currPlaceInfo.value as DataState.Success).data
        val dbArr = currPlaceI?.get("latLng") as DoubleArray
        val latLngStr = "${dbArr[0]},${dbArr[1]}"
        val srcLatLng = LatLng(dbArr[0], dbArr[1])
        val srcIdStr = "place_id:${(_currPlaceInfo.value as DataState.Success<HashMap<String, Serializable>?>).data?.get("id").toString()}"
        val exceptionHandler = CoroutineExceptionHandler { _, exception ->
            _mainDataState.value = DataState.Error(exception as Exception)
        }
//        //println("launching viewmodelscope...")
        val parjob = viewModelScope.launch(exceptionHandler) { //parent job
            when(mainStateEvent) {

                is MainStateEvent.DeleteErrandResultsEvents -> {
//                    //println("attempting to remove an errand result by key : $errandName...")
                    _combinedResults.value?.remove(errandName)
                    _combinedResults.notifyObserver() // notify observer
//                    //println("removed errand result. updated map: ${_combinedResults.value}")
                    if (_combinedResults.value.isNullOrEmpty()) { //info: the last item was removed and list now empty
                        _mapDataState.value?.clear()
                        _mapDataState.notifyObserver()
                        throw CancellationException("All errands have been removed")
                    }
                    //info: update the path after removing
                    var waypoints = "optimize:true"
//                    // println("generating mock results and waypoints string")
                    val mockResults = (_combinedResults.value)!!
                        .filter { it.key != errandName } // dont include existing waypoint if key already exists. plan to replace
                        .map { it.value }
                        .map { it.place_id }
                        .toMutableList()
                        .forEach { waypoints += "|place_id:$it" }
//                    // println("generated updated after delete waypoints string : $waypoints")

                    val tryPolyResults = repository.getDirectionsResults(srcIdStr, waypoints)
                        .filterNotNull()
                        .filter { it is DataState.Success }
                        .map { it as DataState.Success } // info: filter out the datastate errors
                        .map { it.data } // info: to Path
                        .map { it.decodedPoly } // info: to MutableList<LatLng>
                        .filterNotNull() // info: remove nulls
                        .onEmpty { throw CancellationException("Could not find path for this errand") } // network error or no path results
                        .onEach { path ->
                            _mapDataState.value = path //live data poly update
                            _mainDataState.value = DataState.Success("Successfully deleted an errand and updated path") //overall success update
                        }.launchIn(this)

                }

                is MainStateEvent.GetBothResultsEvents -> {
//                    println("setStateEvent job launched")
//                    println("child job 1 launched...")
                        val errResults =  repository.getErrandResults(query, latLngStr) //component: child job 1
                            .filter { it is DataState.Success }
                            .map { it as DataState.Success }
                            .map { it.data.results }
                            .flatMapMerge { it.asFlow() }
                            .onEmpty { //error in DataState which means network error or not results entire job should be cancelled
//                                println("cancellation error was thrown in getErrandResults")
                                throw CancellationException("Could not get result for this errand")
                            }
//                            .onEach { println("result name: " + it.name) }

                        //info: DataState.Success was achieved and atleast one result exist
                        var defaultBest = errResults.first()
//                        println("first DefaultBest is ${defaultBest.name}")
//                        println("DataState.Success was achieved and atleast one result exist")
                        //info: FILTER FUNCTIONS ------------------------------
//                        println("starting filter functions...")
                        val bestResults = errResults
                                .filter {
                                    // info: filter by rating
//                                    println("filtering by rating")
//                                    println("for result ${it.name}, rating was ${it.rating} compared to rating ${_searchPreferences.value?.get("rating")}")
                                    val qualifiedRating = it.rating >= _searchPreferences.value?.get("rating") as Float

                                    //info: filter by price level
//                                    println("filtering by price_level")
//                                    println("for result ${it.name}, price_level was ${it.price_level} compared to price_level ${_searchPreferences.value?.get("price_level")}")
                                    val qualifiedPriceLevel = if(_searchPreferences.value?.get("price_level") as Int != 0) it.price_level == _searchPreferences.value?.get("price_level") as Int else true

                                    //info: filter by radius
//                                    println("filtering by radius")
                                    val thisLatLng = LatLng(it.geometry["location"]!!["lat"]!! as Double, it.geometry["location"]!!["lng"]!! as Double)
                                    val distance = SphericalUtil.computeDistanceBetween(srcLatLng, thisLatLng)
//                                    println("for result ${it.name}, distance was $distance compared to radius ${_searchPreferences.value?.get("radius")}")
                                    val qualifiedRadius = distance <= _searchPreferences.value?.get("radius") as Float
                                    qualifiedRadius && qualifiedPriceLevel && qualifiedRating
                                }

                        //info: filter functions finished besides sorting
//                        println("filter functions done besides chip rating")
                        if (_searchPreferences.value?.get("chip_rating") as Boolean) {
//                            println("starting filter by rating...")
                            bestResults.onEach { curr ->
//                                println("sorting by rating")
                                if (curr.rating > defaultBest.rating) {
                                    defaultBest = curr
//                                    println("default best changed to : ${curr.name}")
                                }
                            }
                            .onEmpty {
//                                println("cancellation error was thrown in getErrandResults")
                                throw CancellationException("Could not get result for this errand")
                            }
                            .launchIn(this).join()

                        } else {
//                            println("chip rating was false, setting defaultBest to first...")
                            defaultBest = bestResults.firstOrNull() ?: throw CancellationException("Could not get result for this errand")
//                            println("defaultBest set to ${defaultBest.name}")
                        }

//                        println("done filtering. best result: ${defaultBest.name}")
                        //info: construct mock bestResults array, add new defaultBest to mock, and construct waypoints
                        var waypoints = "optimize:true"
//                        println("generating mockResults")
                        val mockResults = (_combinedResults.value)!!
                                .filter { it.key != errandName } // info: dont include existing waypoint if key already exists. plan to replace
                                .map { it.value }
                                .map { it.place_id }
                                .toMutableList()
                        mockResults.add(defaultBest.place_id)
//                        println("added defaultBest to mockResults")
                        mockResults.forEach { waypoints += "|place_id:$it" }
//                        println("constructed waypoint string: $waypoints")

                        //info: make path request using defaultbest
                        val tryPolyResults = withContext(viewModelScope.coroutineContext) { //component: child job 2
//                            println("child job 2 launched")
                            repository.getDirectionsResults(srcIdStr, waypoints) // info: to do insert parameters
                        }
                                .filterNotNull()
                                .filter { it is DataState.Success }
                                .map { it as DataState.Success } //info: filter out the datastate errors
                                .map { it.data } //info: to Path
                                .map { it.decodedPoly } //info: to MutableList<LatLng>
                                .filterNotNull() //info: remove nulls
                                .onEmpty { throw CancellationException("Could not find path for this errand") } // network error or no path results
                                .onEach { path ->
                                    _mapDataState.value = path //live data poly update
                                    (_combinedResults.value)!![errandName] = defaultBest //livedata combined result update
                                    _combinedResults.notifyObserver()//notify insert
                                    _mainDataState.value = DataState.Success("Successfully added an errand and updated path") //overall success update
                                }.launchIn(this)
                }
            }
        }
        //info: cancellation exception caught
        parjob.invokeOnCompletion { it?.let {
            _mainDataState.value = if (it is CancellationException) {
                _combinedResults.value?.remove(errandName)
                _combinedResults.notifyObserver()
                DataState.Error(it)
            } else {
                DataState.Error(Exception("Unknown exception"))
            }
        } }
        return parjob

    }

    fun setSearchPreferences(rating : Float, priceLevel : Int, radius : Float, chipRating : Boolean) { // store in prefs "rating", "price_level", "radius", "chip_rating", return a boolean
        val job : CompletableJob = Job()
        val searchPrefs : HashMap<String,Any> = hashMapOf("rating" to rating, "price_level" to priceLevel, "radius" to radius, "chip_rating" to chipRating)
        viewModelScope.launch {
            when(repository.storeSearchPrefs(job, searchPrefs)) {
                is DataState.Success -> {_searchPreferences.value = searchPrefs} // info: search prefs stored
                else -> {throw CancellationException("Could not store search preferences")} // info: search prefs not stored, error
            }
        }.invokeOnCompletion {
            it?.let {
                _mainDataState.value = if(it is CancellationException) DataState.Error(it) else DataState.Error(Exception("Unknown exception storing search preferences"))
            }
        }
    }

    //info: get search preferences and load into live data. if doesnt exist in shared prefs, use default
    fun getSearchPreferences() { // get from prefs, return a hashmap
//        println("starting getSearchPreferences...")
        viewModelScope.launch {
            repository.getSearchPrefs()
                .onEach { ds ->
                    when(ds) {
                        is DataState.Success -> _searchPreferences.value = ds.data!!
                        else -> _searchPreferences.value = defaultSearchPrefs
                    }
                }
                .launchIn(this)
        }
    }

    //info : notify observer extension fun
    fun <T> MutableLiveData<T>.notifyObserver() {
        this.postValue(this.value)
    }

    //info: set new current place info,store into shared prefs, and store into live data
    fun setCurrPlaceInfo(currPlaceId : String, currPlaceLatLng : DoubleArray, currPlaceAddress : String) { // method used when setting new location from main activity
        val info = hashMapOf("id" to currPlaceId, "latLng" to currPlaceLatLng, "address" to currPlaceAddress)
        val job : CompletableJob = Job()
        //info: use repository to store
        repository.storeCurrLocation(job, info).invokeOnCompletion { error ->
            error?.message.let {
                var msg = it
                if (msg.isNullOrBlank()) {
                    msg = "Unknown job error inserting currPlaceInfo into prefs"
                }
//                println("$job was cancelled. Reason: $msg")
            }
            if (error == null) { //info: inserted successfully?
                viewModelScope.launch { //info: retrieve from cache
                    repository.getCurrLocation()
                        .onEach { dataState ->
                            dataState?.let {
                                _currPlaceInfo.value = it //info: change livedata
                            }
                        }
                        .launchIn(this)
                }
            }
        }
    }


    //info: getCurrplace info from prefs and set to livedata
    fun getCurrPlaceInfo(reAdd : Boolean = false) {
        viewModelScope.launch {
            repository.getCurrLocation()
                .filterNotNull()
                .filter { it is DataState.Success }
                .map { it as DataState.Success }
                .onEmpty { throw CancellationException("No current place info was stored") }
                .onEach { dataState ->
                    dataState?.let {
                        _currPlaceInfo.value = if(!it.data.isNullOrEmpty()) it else throw CancellationException("No current place info was stored") //info: currPlaceinfo retrieved or none throw exception
                    }
                }
                .launchIn(this)
                .join()
            if (reAdd) {
                //info: start readding due to location change
                combinedResults.value?.keys
                        ?.onEach { errand -> //info: readd each errand, replacing the old values
                            setStateEvent(MainStateEvent.GetBothResultsEvents, errand).join()
                        }
            }
        }.invokeOnCompletion { //info: cancaellation exception caught
            it?.let {
                _mainDataState.value = if (it is CancellationException) DataState.Error(it) else DataState.Error(Exception("Unknown error getting current place info"))
            }
        }
    }

    //info: check if currplace info exists
    suspend fun currPlaceInfoExists() : Flow<Boolean> {

        val exists = withContext(viewModelScope.coroutineContext) {
            repository.getCurrLocation()
                    .filterNotNull()
                    .filter { it is DataState.Success }
                    .map { it as DataState.Success }
                    .map { !it.data.isNullOrEmpty() }
        }
        return exists
    }

    //info: get set names
    fun getAllSets() {
        viewModelScope.launch {
            repository.getAllSets()
                    .filterNotNull()
                    .filter { it is DataState.Success }
                    .map { it as DataState.Success }
                    .onEmpty { throw CancellationException("Error getting all sets") }
                    .onEach { _setData.value = it.data!! }
                    .launchIn(this)
        }
        .invokeOnCompletion {
            it?.let { // info: caught cancellation exception
                _mainDataState.value = if(it is CancellationException) DataState.Error(it) else DataState.Error(Exception("Unknown error retrieving all sets"))
            }
        }
    }

    //info: get set info only return the set of errands
    suspend fun getSetInfo(setName : String) : Flow<Set<String>> {
        val errs = withContext(viewModelScope.coroutineContext) {
            repository.retrieveSet(setName)
                    .filterNotNull()
                    .filter { it is DataState.Success }
                    .map { it as DataState.Success }
                    .map { it.data }
        }
        return errs
    }

    //info: store a set with a setName
    fun storeSet(setName : String) { //info: store the current livedata set using a user input setName
        val job : CompletableJob = Job()
        viewModelScope.launch {
            //info: get the livedata errand list from here and convert to set
            val stored = repository.storeSet(job, setName, (_combinedResults.value)!!.keys.toHashSet())
            // when statement :
            when(stored) {
                is DataState.Success -> { //info: successfully stored, update ui, notify observer
                    _setData.value?.let {
                        _mainDataState.value = if (it.contains(setName)) {
                           DataState.Success("Updated an existing set")
                        } else {
                            it.add(setName)
                            DataState.Success("Successfully stored the set")
                        }
                        _setData.notifyObserver()
                    }
                }
                else -> {
                    throw CancellationException("Error attempting to store a set")
                } //info: error storing
            }
        }.invokeOnCompletion {
            it?.let { //info: cancellation error caught
                _mainDataState.value = if(it is CancellationException) DataState.Error(it) else DataState.Error(Exception("Unknown error storing set"))
            }
        }
    }

    //info: load a set from shared prefs, including making call to api
    fun loadSet(setName : String) { // retrieve set from prefs and set to livedata if exists, clear any existing errands
        viewModelScope.launch {
            //mocks were created in case of error to restore data
            val mockResults = _combinedResults.value
            val mockPoly = _mapDataState.value
            //clear and notify observers
            _combinedResults.value?.clear()
            _combinedResults.notifyObserver()
            _mapDataState.value?.clear()
            _mapDataState.notifyObserver()

            repository.retrieveSet(setName)
                .filter { it is DataState.Success }
                .map { it as DataState.Success }
                .map { it.data.toMutableList() }
                .flatMapMerge { it.asFlow() }
                .onEmpty {
                    _combinedResults.value = mockResults!!
                    _mapDataState.value = mockPoly!!
                    throw CancellationException("Error loading set: was either null or empty")
                }
                .onEach { errand -> //info: make api request to retrieve errand result for each
                    val addStateEvent = MainStateEvent.GetBothResultsEvents
                    setStateEvent(addStateEvent, errand).join() // info: starting add from set data, calling api here
                }
                .launchIn(this)
        }.invokeOnCompletion {
            it?.let { // info: caught cancellation exception
                _mainDataState.value = if(it is CancellationException) DataState.Error(it) else DataState.Error(Exception("Unknown error loading set"))
            }
        }
    }

    fun deleteSet(setName : String) { // info: delete set from prefs
        val job : CompletableJob = Job()
        viewModelScope.launch {
            when (repository.deleteSet(job, setName)) {
                is DataState.Success -> { // info: successfully deleted, update ui to delete from list. should probably pass in list position to delete
                    val index = _setData.value?.indexOf(setName)
//                    println("index was $index")
                    if(index != null && index != -1)_setData.value?.removeAt(index)
//                    println("new setData is ${_setData.value?.joinToString()}")
                    _setData.notifyObserver()
                    _mainDataState.value = DataState.Success("$setName was removed")
                }
                else -> { // info: error deleting, display error toast
                    throw CancellationException("Error attempting to delete a set")
                }
            }
        }.invokeOnCompletion {
            it?.let {
                _mainDataState.value = if(it is CancellationException) DataState.Error(it) else DataState.Error(Exception("Unknown error deleting set"))// info: cancellation exception caught
            }
        }
    }

    //info: update list of checked errands and update map
    fun updateCheckedMarker(checked : Boolean, errandName : String) {
        if (checked) {
            _checkedErrands.value?.add(errandName)
            _checkedErrands.notifyObserver()
        } else {
            checkedErrands.value?.remove(errandName)
            _checkedErrands.notifyObserver()
        }
    }

}

sealed class MainStateEvent { //info: main state events includes: GetErrandResultsEvents, GetDirectionsResultsEvents, DeleteErrandResultsEvents, GetBothResultsEvents
    object GetErrandResultsEvents : MainStateEvent()

    object GetDirectionsResultsEvents : MainStateEvent()

    object DeleteErrandResultsEvents : MainStateEvent()

    object GetBothResultsEvents : MainStateEvent()

    object None : MainStateEvent()
}