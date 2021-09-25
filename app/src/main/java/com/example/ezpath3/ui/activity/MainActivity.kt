package com.example.ezpath3.ui.activity

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.viewModels
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.updateLayoutParams
import androidx.transition.Scene
import androidx.transition.Transition
import androidx.transition.TransitionManager
import com.example.ezpath3.BuildConfig.PLACES_API_KEY
import com.example.ezpath3.R
import com.example.ezpath3.databinding.ActivityMainBinding
import com.example.ezpath3.di.modules.searchBarTransitionSet
import com.example.ezpath3.ui.viewmodel.ErrandModel
import com.example.ezpath3.util.DataState
import com.example.ezpath3.util.makeListener
import com.example.ezpath3.util.makePlaceListener
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.material.card.MaterialCardView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val viewModel : ErrandModel by viewModels()
    private lateinit var viewBinding : ActivityMainBinding

    @Inject
    @searchBarTransitionSet
    lateinit var set : Transition

    lateinit var autoCompleteSupportFrag : AutocompleteSupportFragment

    private var expanded = false
    private var placeSelected = false

    private var defaultButtonWidth : Int = 0
    private var defaultButtonRadius : Float = 0F

    override fun onCreate(savedInstanceState: Bundle?) { //todo skip if currplace already exists URGENT DONE
        super.onCreate(savedInstanceState)
        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        val view = viewBinding.root
        setContentView(view)
        //info: initiate places stuff
        initSupportFrag()
        //info: attach click listeners
        attachUiListeners()
        //info: subscribe observers
        subscribeObservers()
        //info: check if currPlace already exists in shared prefs and skip to ErrandActivity if it does
        if (!isForResult()) {
            CoroutineScope(Main).launch {
                var exists = false
                viewModel.currPlaceInfoExists()
                        .onEach { exists = it }
                        .launchIn(this).join()
                if(exists) {
                    withContext(Main) {
                        launchErrandActivity()
                    }
                }
            }
        }
    }

    private fun initSupportFrag() {
        if (!Places.isInitialized()) {
            Places.initialize(this, PLACES_API_KEY)
        }
        Places.createClient(this)
        if (!::autoCompleteSupportFrag.isInitialized) {
            autoCompleteSupportFrag = supportFragmentManager.findFragmentById(R.id.place_search_autocomplete) as AutocompleteSupportFragment
            autoCompleteSupportFrag.apply {
                setPlaceFields(listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG))
                setHint("")
                view?.findViewById<View>(R.id.places_autocomplete_search_button)?.visibility = View.GONE
                view?.findViewById<View>(R.id.places_autocomplete_content)
                view?.findViewById<View>(R.id.places_autocomplete_search_input)?.isEnabled = false
                //placeselectedlistener
                makePlaceListener(
                    onPlaceSelected = { place ->
                        CoroutineScope(Main).launch {
                            delay(500L)
                            viewModel.setCurrPlaceInfo(place.id!!, doubleArrayOf(place.latLng!!.latitude, place.latLng!!.longitude), place.name!!)
                        }
                    },
                    onError = {
                        viewBinding.searchLocationButton.performClick()
                    }
                )
            }
        }
    }

    private fun attachUiListeners() {
        initSupportFrag()
        set.makeListener(onEnd = {
            if (expanded) {
                CoroutineScope(Main).launch {
                    delay(150L)
                    autoCompleteSupportFrag.view?.findViewById<View>(R.id.places_autocomplete_search_input)?.performClick()
                }
            }
            else if (placeSelected) {
                launchErrandActivity()
            }
        })
        viewBinding.searchLocationButton.apply {
            setOnClickListener {
                this.toggleExpand()
            }
            defaultButtonWidth = this.layoutParams.width
            defaultButtonRadius = this.radius
        }

    }

    private fun launchErrandActivity() {
        Intent(this, ErrandActivity::class.java).also { intent ->
            if (isForResult()) {
                setResult(Activity.RESULT_OK, intent)
                onBackPressed()
            } else {
                finish()
                startActivity(intent)
            }
        }
    }

    private fun isForResult() : Boolean {
        return callingActivity != null
    }

    private fun subscribeObservers() {
        //info: observe currPlaceChanges
        viewModel.currPlaceInfo.observe(this) { dataState ->
            when (dataState) {
                is DataState.Success -> {
                    //Log.d("ex hashmap", "${hashMapOf("awawa" to "", "mwjfn" to 1234).entries.joinToString()}")
                    //Log.d("dataState success", "success: ${dataState.data?.entries?.joinToString()}")
                    // Toast.makeText(this, "currPlaceInfoChanged observed! ${dataState.data?.entries?.joinToString()}", Toast.LENGTH_SHORT).show()
                    viewBinding.searchLocationButton.performClick()
                    placeSelected = true
                }
                else -> {Toast.makeText(this, "Error storing curr place info into shared prefs", Toast.LENGTH_SHORT).show()}
            }
        }
    }

    private fun MaterialCardView.toggleExpand () {
        val par = this.parent
        TransitionManager.go(Scene(par as ViewGroup), set)
        val constraintLayout = par as ConstraintLayout
        val constraintSet = ConstraintSet()
        constraintSet.clone(constraintLayout)
        constraintSet.clear(id, ConstraintSet.START)
        constraintSet.clear(id, ConstraintSet.END)
        val viewId = if(!isChecked) R.id.logo else R.id.myde
        constraintSet.connect(id, ConstraintSet.START, viewId, ConstraintSet.START)
        constraintSet.connect(id, ConstraintSet.END, viewId, ConstraintSet.END)
        val bias = if(!isChecked) 0.1F else 0.5F
        constraintSet.setHorizontalBias(id, bias)
        constraintSet.applyTo(constraintLayout)
        when(isChecked) {
            false -> {
                CoroutineScope(Main).launch {
                    delay(200L)
                    TransitionManager.go(Scene(par), set)
                    updateLayoutParams {
                        width = 0
                    }
                    radius = 10F
                    val cL = getChildAt(0) as ConstraintLayout
                    val searchIcon = cL.getChildAt(0)
                    val cS = ConstraintSet()
                    cS.clone(cL)
                    cS.clear(searchIcon.id, ConstraintSet.END)
                    cS.connect(searchIcon.id, ConstraintSet.END, R.id.place_search_autocomplete, ConstraintSet.START)
                    cS.applyTo(cL)
                    expanded = true
                    isChecked = true
                }
            }
            true -> {
                updateLayoutParams {
                    width = defaultButtonWidth
                }
                radius = defaultButtonRadius
                val cL = this.getChildAt(0) as ConstraintLayout
                val searchIcon = cL.getChildAt(0)
                val cS = ConstraintSet()
                cS.clone(cL)
                cS.clear(searchIcon.id, ConstraintSet.END)
                cS.connect(searchIcon.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
                cS.applyTo(cL)
                expanded = false
                isChecked = false
            }
        }

    }

    override fun onDestroy() {
        autoCompleteSupportFrag.onDestroy()
        super.onDestroy()
    }


    override fun onBackPressed() {
        if (isForResult()) {
            finish()
        } else {
            moveTaskToBack(true)
        }
    }


}