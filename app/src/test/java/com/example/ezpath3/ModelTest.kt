package com.example.ezpath3

import android.content.Context
import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.Observer
import androidx.test.core.app.ApplicationProvider
import com.example.ezpath3.repository.PreferencesManagerImpl
import com.example.ezpath3.repository.Repository
import com.example.ezpath3.retrofit.GoogleApiService
import com.example.ezpath3.retrofit.NetworkDirectionsResultsMapper
import com.example.ezpath3.retrofit.NetworkErrandResultsMapper
import com.example.ezpath3.ui.viewmodel.ErrandModel
import com.example.ezpath3.util.DataState
import com.example.ezpath3.util.PreferencesManager
import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.Mockito.*
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.Serializable

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.O_MR1])
class ModelTest {

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

   // lateinit var lifeCycleOwner : LifecycleOwner

//    @Mock
//    lateinit var fakeCurrPlaceInfoObserver : Observer<DataState<HashMap<String, Serializable>>>

  //  lateinit var lifecycle : Lifecycle

    /*
    @Singleton
    @Provides
    fun provideGsonBuilder() : Gson {
        return GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create()
    }

    @Singleton
    @Provides
    fun provideRetrofit(gson : Gson) : Retrofit.Builder {
        return Retrofit.Builder().baseUrl("https://maps.googleapis.com/maps/api/")
            .addConverterFactory(GsonConverterFactory.create(gson))
    }

    @Singleton
    @Provides
    fun provideGoogleApiService(retrofit: Retrofit.Builder) : GoogleApiService {
        return retrofit
            .build()
            .create(GoogleApiService::class.java)
    }
    */

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
//        model!!.currPlaceInfo.observeForever { newValue -> // currplaceInfo observer
//            println("currPlaceInfo observed! : ${newValue.toString()}")
//            Thread.sleep(3000)
//        }
    }

    @Test
    fun randomTest () {
        println("Hello, this is random Test")
    }

    @Test
    fun testInsertCurrPlaceInfo_Success() {
        val fakePlaceId = "blah blah blah random place Id"
        val fakePlaceLatLng = doubleArrayOf(42.3675294, -71.186966)
        val fakePlaceAddress = "fake address"

        model!!.setCurrPlaceInfo(fakePlaceId, fakePlaceLatLng, fakePlaceAddress)

    }


    @After
    fun tearDown() {
        model = null
        repository = null
        retrofit = null
        prefsManager = null
    }
}