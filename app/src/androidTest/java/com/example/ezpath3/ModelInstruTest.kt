package com.example.ezpath3

import android.content.Context

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.ezpath3.repository.PreferencesManagerImpl
import com.example.ezpath3.repository.Repository
import com.example.ezpath3.retrofit.GoogleApiService
import com.example.ezpath3.retrofit.NetworkDirectionsResultsMapper
import com.example.ezpath3.retrofit.NetworkErrandResultsMapper
import com.example.ezpath3.ui.viewmodel.ErrandModel
import com.example.ezpath3.ui.viewmodel.MainStateEvent
import com.example.ezpath3.util.DataState
import com.example.ezpath3.util.PreferencesManager
import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.CancellationException

@RunWith(AndroidJUnit4::class)
class ModelInstruTest {
    @Rule
    @JvmField
    val instantExexcutorRule = InstantTaskExecutorRule()

    var model : ErrandModel? = null
    var repository : Repository? = null
    var retrofit : GoogleApiService? = null

    lateinit var networkErrandResultsMapper: NetworkErrandResultsMapper

    lateinit var networkDirectionsResultsMapper: NetworkDirectionsResultsMapper
    //PreferencesManagerImpl(context.getSharedPreferences(context.packageName + "_preferences", Context.MODE_PRIVATE))
    var prefsManager : PreferencesManager? = null

    @Before
    fun setUp() {
        val appContext = ApplicationProvider.getApplicationContext<EzPathApplication>()
        //MockitoAnnotations.initMocks(this) // lifecycler owner, observer

        //lifecycle owner and lifecycler
        //lifecycle = LifecycleRegistry(lifeCycleOwner)
        //mappers
        networkErrandResultsMapper = NetworkErrandResultsMapper()
        networkDirectionsResultsMapper = NetworkDirectionsResultsMapper()

        // prefs manager
        prefsManager = PreferencesManagerImpl(appContext.getSharedPreferences(appContext.packageName + "_preferences", Context.MODE_PRIVATE))
        // retrofit
        val gson = GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create()
        retrofit = Retrofit.Builder().baseUrl("https://maps.googleapis.com/maps/api/")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(GoogleApiService::class.java)
        //repository
        repository = Repository(retrofit!!, networkErrandResultsMapper, networkDirectionsResultsMapper, prefsManager!!)
        //model
        model = ErrandModel(repository!!)

        //subscribe observers
        //currPlaceInfo observer
        model!!.currPlaceInfo.observeForever { newValue -> // currplaceInfo observer
            println("currPlaceInfo observed! : ${newValue.toString()}")
        }
        //searchPrefs observer
        model!!.searchPreferences.observeForever { newValue ->
            println("searchPreferences observed! : ${newValue.entries.joinToString()}")
        }
        //combinedResult observer
        model!!.combinedResults.observeForever { newValue ->
            println("combinedResults observed! : ${newValue.toString()}")
        }
        //mapdatastate observer
        model!!.mapDataState.observeForever { newValue ->
            println("mapDataState observed! : ${newValue.joinToString()}")
        }
        //main data state observer
        model!!.mainDataState.observeForever { newValue ->
            println("mainDataState observed! : ${newValue.toString()}")
        }

        //set data state observer
        model!!.setData.observeForever { newValue ->
            println("setData change observed! : $newValue")
        }
    }

    @Test
    fun testInsertCurrPlaceInfo_Success() {
        val fakePlaceId = "blah blah blah random place Id"
        val fakePlaceLatLng = doubleArrayOf(42.3675294, -71.186966)
        val fakePlaceAddress = "fake address"
        model!!.setCurrPlaceInfo(fakePlaceId, fakePlaceLatLng, fakePlaceAddress)

    }

    @Test
    fun testClearPrefs() {
        val cleared = prefsManager!!.clear()
        assertTrue(cleared)
    }

    @Test
    fun testPrefsIsEmpty() {
        val isEmpty = prefsManager!!.isEmpty()
        assertTrue(isEmpty)
    }

    @Test
    fun testPrefsGetAll() {
        val all = prefsManager!!.getAll()
        println("all entries: ${all.entries.joinToString()}")
    }

    @Test
    fun testPrefsGetCurrLocation_SuccessNotEmpty() {
        val currLocation = prefsManager!!.getCurrLocation()
        println("curr location: ${currLocation?.entries?.joinToString()}")
        println("currLatLng: lat: ${(currLocation!!["latLng"] as DoubleArray)[0]}, lng: ${(currLocation!!["latLng"] as DoubleArray)[1]}")
        assertTrue(!currLocation.isNullOrEmpty())
    }

    @Test
    fun testModelGetCurrLocation_SuccessChangeObserved() {
        model!!.getCurrPlaceInfo()
        Thread.sleep(1000)
        assertTrue(model!!.currPlaceInfo.value is DataState.Success && !(model!!.currPlaceInfo.value as DataState.Success).data.isNullOrEmpty())
    }

    @Test
    fun testModelStoreSearchPrefs_SuccessChangeObserved() {
       // val fakeSearchPrefs = hashMapOf("rating" to 4.2F, "price_level" to 2, "radius" to 15000, "chip_rating" to true)
        model!!.setSearchPreferences(4.2f, 2, 15000f, true)
        Thread.sleep(1000)
        assertTrue(!model!!.searchPreferences.value.isNullOrEmpty())
    }

    @Test
    fun testModelGetSearchPrefs_SuccessChangeObserved() {
        model!!.getSearchPreferences()
        Thread.sleep(1000)
        assertTrue(!model!!.searchPreferences.value.isNullOrEmpty())
    }

    @Test
    fun testModelGetResults_SuccessChangeObserved() {
        //set currlocation
        val fakePlaceId = "ChIJ____E5F444kRmIjfvqxEjo0"
        val fakePlaceLatLng = doubleArrayOf(42.3675294, -71.186966)
        val fakePlaceAddress = "fake address"
        model!!.setCurrPlaceInfo(fakePlaceId, fakePlaceLatLng, fakePlaceAddress)
        Thread.sleep(1000L) // simulate set and retrieval process
        //set preferences
        model!!.setSearchPreferences(4.2f, 2, 15000f, true)
        Thread.sleep(1000L) //simulate set and retrieval process
        //val currplaceinfo = model!!.currPlaceInfo.value
        //assertTrue(currplaceinfo != null)
        //try to get results
        val mainStateEvent = MainStateEvent.GetBothResultsEvents
        model!!.setStateEvent(mainStateEvent, "buy pencils")
        Thread.sleep(5000L) // simulate api delay
        assertTrue(model!!.mainDataState.value is DataState.Success && model!!.combinedResults.value!!.containsKey("buy pencils"))
        assertTrue(!model!!.mapDataState.value.isNullOrEmpty())

    }

    @Test
    fun testModelGetResults2_SuccessChangeObserved() { //already has stored prefs and location
        //get location
        model!!.getCurrPlaceInfo()
        Thread.sleep(1000L)
        //get prefs
        model!!.getSearchPreferences()
        Thread.sleep(1000L)
        //trying results
        val mainStateEvent = MainStateEvent.GetBothResultsEvents
        model!!.setStateEvent(mainStateEvent, "buy pencils")
        Thread.sleep(5000L)
        assertTrue(model!!.mainDataState.value is DataState.Success && model!!.combinedResults.value!!.containsKey("buy pencils"))
        assertTrue(!model!!.mapDataState.value.isNullOrEmpty())
    }

    @Test
    fun testModelGetMultipleResults_SuccessChangeObserved() {
        //set currlocation
        val fakePlaceId = "ChIJ____E5F444kRmIjfvqxEjo0"
        val fakePlaceLatLng = doubleArrayOf(42.3675294, -71.186966)
        val fakePlaceAddress = "fake address"
        model!!.setCurrPlaceInfo(fakePlaceId, fakePlaceLatLng, fakePlaceAddress)
        Thread.sleep(1000L) // simulate set and retrieval process
        //set preferences
        model!!.setSearchPreferences(4.2f, 2, 15000f, true)
        Thread.sleep(1000L) //simulate set and retrieval process
        //val currplaceinfo = model!!.currPlaceInfo.value
        //assertTrue(currplaceinfo != null)
        //try to get results
        val mainStateEvent = MainStateEvent.GetBothResultsEvents
        model!!.setStateEvent(mainStateEvent, "buy pencils")
        Thread.sleep(5000L) // simulate api delay
        model!!.setStateEvent(mainStateEvent, "get gas")
        Thread.sleep(5000L)
        assertTrue(model!!.mainDataState.value is DataState.Success)
        assertTrue(model!!.combinedResults.value!!.containsKey("buy pencils"))
        assertTrue(model!!.combinedResults.value!!.containsKey("get gas"))
        assertTrue(!model!!.mapDataState.value!!.isNullOrEmpty())
    }

    @Test
    fun testModelGetMultipleDuplicateResults_SuccessReplaceChangeObserved() {
        //set currlocation
        val fakePlaceId = "ChIJ____E5F444kRmIjfvqxEjo0"
        val fakePlaceLatLng = doubleArrayOf(42.3675294, -71.186966)
        val fakePlaceAddress = "fake address"
        model!!.setCurrPlaceInfo(fakePlaceId, fakePlaceLatLng, fakePlaceAddress)
        Thread.sleep(1000L) // simulate set and retrieval process
        //set preferences
        model!!.setSearchPreferences(4.2f, 2, 15000f, true)
        Thread.sleep(1000L) //simulate set and retrieval process
        //val currplaceinfo = model!!.currPlaceInfo.value
        //assertTrue(currplaceinfo != null)
        //try to get results
        val mainStateEvent = MainStateEvent.GetBothResultsEvents
        model!!.setStateEvent(mainStateEvent, "buy pencils")
        Thread.sleep(5000L) // simulate api delay
        model!!.setStateEvent(mainStateEvent, "buy pencils")
        Thread.sleep(5000L)
        assertTrue(model!!.mainDataState.value is DataState.Success)
        assertTrue(model!!.combinedResults.value!!.containsKey("buy pencils"))
        assertTrue(!model!!.mapDataState.value.isNullOrEmpty())

    }

    @Test
    fun testModelGetResults_FailedNoErrandResultChangeObserved() { //mainstateevent should have an error
        //set currlocation
        val fakePlaceId = "ChIJ____E5F444kRmIjfvqxEjo0"
        val fakePlaceLatLng = doubleArrayOf(42.3675294, -71.186966)
        val fakePlaceAddress = "fake address"
        model!!.setCurrPlaceInfo(fakePlaceId, fakePlaceLatLng, fakePlaceAddress)
        Thread.sleep(1000L) // simulate set and retrieval process
        //set preferences
        model!!.setSearchPreferences(4.2f, 2, 15000f, true)
        Thread.sleep(1000L) //simulate set and retrieval process
        val mainStateEvent = MainStateEvent.GetBothResultsEvents
        model!!.setStateEvent(mainStateEvent, "ahsdjba sadjhbasdhja bdajsd")
        Thread.sleep(5000L) // simulate api delay
        assertTrue(model!!.mainDataState.value is DataState.Error)
        assertTrue((model!!.mainDataState.value as DataState.Error).exception is CancellationException &&
                (model!!.mainDataState.value as DataState.Error).exception.message.equals("Could not get result for this errand"))
    }

    @Test
    fun testModelGetResults_FailedNoPathResultChangeObserved() { //mainstateevent should have an error, make fake placeid like india

    }

    @Test
    fun testModelDeleteResultAndUpdatePath_SuccessChangeObserved() {
        //insert multiple fake results ----------
        //set currlocation
        val fakePlaceId = "ChIJ____E5F444kRmIjfvqxEjo0"
        val fakePlaceLatLng = doubleArrayOf(42.3675294, -71.186966)
        val fakePlaceAddress = "fake address"
        model!!.setCurrPlaceInfo(fakePlaceId, fakePlaceLatLng, fakePlaceAddress)
        Thread.sleep(1000L) // simulate set and retrieval process
        //set preferences
        model!!.setSearchPreferences(4.2f, 2, 15000f, true)
        Thread.sleep(1000L) //simulate set and retrieval process
        //val currplaceinfo = model!!.currPlaceInfo.value
        //assertTrue(currplaceinfo != null)
        //try to get results
        val mainStateEvent = MainStateEvent.GetBothResultsEvents
        model!!.setStateEvent(mainStateEvent, "buy pencils")
        Thread.sleep(5000L) // simulate api delay
        model!!.setStateEvent(mainStateEvent, "get gas")
        Thread.sleep(5000L)
        assertTrue(model!!.mainDataState.value is DataState.Success)
        assertTrue(model!!.combinedResults.value!!.containsKey("buy pencils"))
        assertTrue(model!!.combinedResults.value!!.containsKey("get gas"))
        assertTrue(!model!!.mapDataState.value!!.isNullOrEmpty())
        val oldPath = model!!.mapDataState.value
        //deleting a result and updating path ------------
        val deleteStateEvent = MainStateEvent.DeleteErrandResultsEvents
        model!!.setStateEvent(deleteStateEvent, "buy pencils")
        Thread.sleep(5000) //simulate api process
        assertTrue(model!!.mainDataState.value is DataState.Success && (model!!.mainDataState.value as DataState.Success).data == "Successfully deleted an errandResult and updated path")
        assertTrue(!model!!.combinedResults.value!!.containsKey("buy pencils"))
        assertTrue(model!!.combinedResults.value!!.containsKey("get gas"))
        assertTrue(!model!!.mapDataState.value!!.isNullOrEmpty())
        assertTrue(model!!.mapDataState.value!! != oldPath)

    }

    @Test
    fun testModelDeleteResultEmptyAfter_SuccessChangeObserved() {
        // insert single fake result -------\
        //set currlocation
        val fakePlaceId = "ChIJ____E5F444kRmIjfvqxEjo0"
        val fakePlaceLatLng = doubleArrayOf(42.3675294, -71.186966)
        val fakePlaceAddress = "fake address"
        model!!.setCurrPlaceInfo(fakePlaceId, fakePlaceLatLng, fakePlaceAddress)
        Thread.sleep(1000L) // simulate set and retrieval process
        //set preferences
        model!!.setSearchPreferences(4.2f, 2, 15000f, true)
        Thread.sleep(1000L) //simulate set and retrieval process
        //val currplaceinfo = model!!.currPlaceInfo.value
        //assertTrue(currplaceinfo != null)
        //try to get results
        val mainStateEvent = MainStateEvent.GetBothResultsEvents
        model!!.setStateEvent(mainStateEvent, "buy pencils")
        Thread.sleep(5000L) // simulate api delay
        assertTrue(model!!.mainDataState.value is DataState.Success && model!!.combinedResults.value!!.containsKey("buy pencils"))
        assertTrue(!model!!.mapDataState.value.isNullOrEmpty())
        //deleting single result and expecting error and maindatastate cancellation message
        val deleteStateEvent = MainStateEvent.DeleteErrandResultsEvents
        model!!.setStateEvent(deleteStateEvent, "buy pencils")
        Thread.sleep(2000)
        assertTrue(model!!.mainDataState.value is DataState.Error)
        assertTrue((model!!.mainDataState.value as DataState.Error).exception is CancellationException && (model!!.mainDataState.value as DataState.Error).exception.message == "All errands have been removed")
        assertTrue(model!!.mapDataState.value!!.isEmpty()) //the polyline should have been cleared
        assertTrue(model!!.combinedResults.value!!.isEmpty()) // errand results list should now be empty


    }



    @Test
    fun testModelDeleteResultNoUpdatedPath_FailChangeObserved() { // rare case?

    }


    @Test
    fun testModelStoreSet_SuccessChangeObserved() {
        // insert multiple fake results ---------

        //set currlocation
        val fakePlaceId = "ChIJ____E5F444kRmIjfvqxEjo0"
        val fakePlaceLatLng = doubleArrayOf(42.3675294, -71.186966)
        val fakePlaceAddress = "fake address"
        model!!.setCurrPlaceInfo(fakePlaceId, fakePlaceLatLng, fakePlaceAddress)
        Thread.sleep(1000L) // simulate set and retrieval process
        //set preferences
        model!!.setSearchPreferences(4.2f, 2, 15000f, true)
        Thread.sleep(1000L) //simulate set and retrieval process
        //val currplaceinfo = model!!.currPlaceInfo.value
        //assertTrue(currplaceinfo != null)
        //try to get results
        val mainStateEvent = MainStateEvent.GetBothResultsEvents
        model!!.setStateEvent(mainStateEvent, "buy pencils")
        Thread.sleep(5000L) // simulate api delay
        model!!.setStateEvent(mainStateEvent, "get gas")
        Thread.sleep(5000L)
        assertTrue(model!!.mainDataState.value is DataState.Success)
        assertTrue(model!!.combinedResults.value!!.containsKey("buy pencils"))
        assertTrue(model!!.combinedResults.value!!.containsKey("get gas"))
        assertTrue(!model!!.mapDataState.value!!.isNullOrEmpty())
        //set into shared preferences ---------
        model!!.storeSet("testSet1")
        Thread.sleep(1000) // simulate store and retrieve process
        assertTrue(!model!!.setData.value.isNullOrEmpty()) // should not be empty after storing
        assertTrue(model!!.setData.value!!.contains("testSet1")) // contains the stored set name

    }

    @Test
    fun testRetrieveSetWithPrefsManager_SuccessExists() {
        val set = prefsManager!!.loadSet("testSet1")
        assertTrue(set.isNotEmpty() && set.contains("get gas") && set.contains("buy pencils")) // contains the errands that were saved
    }

    @Test
    fun testModelDeleteSet_SuccessChangeObserved() {
        model!!.deleteSet(0, "testSet1")
        Thread.sleep(1000) // simulate delete process
        assertTrue(!model!!.setData.value!!.contains("testSet1")) // should no longer exist in livedata
        val emptySet = prefsManager!!.loadSet("testSet1")
        println(emptySet.toString())
        assertTrue(emptySet.isEmpty()) // should no longer exist in prefs
    }

    @Test
    fun testModelLoadSet_SuccessChangeObserved() {
        //set currlocation
        val fakePlaceId = "ChIJ____E5F444kRmIjfvqxEjo0"
        val fakePlaceLatLng = doubleArrayOf(42.3675294, -71.186966)
        val fakePlaceAddress = "fake address"
        model!!.setCurrPlaceInfo(fakePlaceId, fakePlaceLatLng, fakePlaceAddress)
        Thread.sleep(1000L) // simulate set and retrieval process
        //set preferences
        model!!.setSearchPreferences(4.2f, 2, 15000f, true)
        Thread.sleep(1000L) //simulate set and retrieval process

        // attempting to reload data from set ------------
        model!!.loadSet("testSet1")
        Thread.sleep(10000) // simulate retrieve set and both api requests

        assertTrue(model!!.mainDataState.value is DataState.Success)
        assertTrue(model!!.combinedResults.value!!.containsKey("buy pencils"))
        assertTrue(model!!.combinedResults.value!!.containsKey("get gas"))
        assertTrue(!model!!.mapDataState.value.isNullOrEmpty())
    }

    @Test
    fun testModelGetPreferencesDefault() { //private val defaultSearchPrefs : HashMap<String, Any> = hashMapOf("rating" to 1.0f, "price_level" to 0, "radius" to 1500f, "chip_rating" to false)
        model!!.getSearchPreferences() // should be set to default,
        Thread.sleep(1000)
        assertTrue(model!!.searchPreferences.value!!.isNotEmpty())
        assertTrue(model!!.searchPreferences.value?.get("rating") == 1.0f)
        assertTrue(model!!.searchPreferences.value?.get("price_level") == 0)
        assertTrue(model!!.searchPreferences.value?.get("radius") == 1500f)
        assertTrue(model!!.searchPreferences.value?.get("chip_rating") == false)
    }



    @After
    fun tearDown() {
        model = null
        repository = null
        retrofit = null
        prefsManager = null
    }

}