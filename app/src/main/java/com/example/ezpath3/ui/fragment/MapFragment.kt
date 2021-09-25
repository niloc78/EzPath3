package com.example.ezpath3.ui.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.ezpath3.R
import com.example.ezpath3.databinding.MapFragLayoutBinding
import com.example.ezpath3.ui.dialog.DialogTypes
import com.example.ezpath3.ui.dialog.MultipurposeDialog
import com.example.ezpath3.ui.viewmodel.ErrandModel
import com.example.ezpath3.util.DataState
import com.example.ezpath3.util.makeBottomSheetCallback
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import dagger.hilt.android.AndroidEntryPoint
import jp.wasabeef.blurry.Blurry
import javax.inject.Inject

private const val LOCATION_INTERVAL = 5000L
private const val LOCATION_FASTEST_INTERVAL = 5000L
@AndroidEntryPoint
class MapFragment
@Inject
constructor(someString : String) : Fragment(R.layout.map_frag_layout), OnMapReadyCallback, MultipurposeDialog.MultipurposeDialogListener
{

    private lateinit var viewBinding : MapFragLayoutBinding
    private val viewModel : ErrandModel by activityViewModels()
    @Inject
    lateinit var mapFragment : SupportMapFragment
    @Inject
    lateinit var fusedLocationClient : FusedLocationProviderClient
    @Inject
    lateinit var locationCallback: LocationCallback
    private lateinit var bottomSheetBehavior : BottomSheetBehavior<ConstraintLayout>
    private lateinit var map : GoogleMap
    private var sourceMarker : Marker? = null
    private var polyline : Polyline? = null
    private var markers : LinkedHashMap<String, Marker?> = linkedMapOf()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewBinding = MapFragLayoutBinding.inflate(layoutInflater)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Log.d("MapFragment tag: ", "$tag")
        // Log.d("MapFragment parent: ", "${parentFragment?.javaClass?.name}")
        //info: setup includes: attaching listeners, initiating map
        setUp()
        //info: observers subscribed on map ready
    }

    override fun onPause() {
        fusedLocationClient.reset()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        if (map.isMyLocationEnabled)  {
            startLocationUpdates(createLocationRequest(interval = LOCATION_INTERVAL, fastestInterval = LOCATION_FASTEST_INTERVAL, priority = LocationRequest.PRIORITY_HIGH_ACCURACY),
                    locationCallback = locationCallback)
        }
    }

    private fun subscribeObservers() { //todo observe mapDataState, combinedResults, currPlaceInfo DONE
        //component: currPlaceInfo observer
        viewModel.currPlaceInfo.observe(viewLifecycleOwner) { // todo change source marker, animate camera to source DONE
            if (it is DataState.Success) {
                val latLngArr = it.data?.get("latLng") as DoubleArray
                val latLng = LatLng(latLngArr[0], latLngArr[1])
                //info: remove previous marker
                sourceMarker?.remove()
                // info: add marker
                sourceMarker = map.addMarker(MarkerOptions().position(latLng).title("Starting Location")
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)))
                // info: animate camera to source
                map.animateCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.Builder().target(latLng).zoom(10F).build()))
            }
        }
        //component: mapDataState observer
        viewModel.mapDataState.observe(viewLifecycleOwner) { //todo update map polyline DONE
            //info: remove previous polyline if exists
            polyline?.remove()
            //info: update polyline
            polyline = map.addPolyline(
                    PolylineOptions().addAll(it).width(17F).color(Color.DKGRAY)
            )
        }
        //component: combinedResults observer
        viewModel.combinedResults.observe(viewLifecycleOwner) { results -> //todo update marker(s) DONE
            //info: clear markers
            markers.values.forEach {
                it?.remove()
            }
            markers.clear()
            //info: readd all updated ones
            results.entries
                    .forEach {
                        val resultLatLng = LatLng(it.value.geometry["location"]?.get("lat") as Double, it.value.geometry["location"]?.get("lng") as Double)
                        markers["${it.key}"] = map.addMarker(
                                MarkerOptions().position(resultLatLng).title(it.value.name)
                        )
                    }
        }
        //component: checkedErrands observer
        viewModel.checkedErrands.observe(viewLifecycleOwner) {
            //println("checkedErrands observed! ${it.joinToString()}")
            markers.entries.forEach { entries ->
                entries.value?.setIcon(BitmapDescriptorFactory.defaultMarker())
            }
            it.forEach { errand ->
                markers[errand]?.setIcon(BitmapDescriptorFactory.defaultMarker(90F))
            }
        }

    }

    private fun setUp() { //todo setup ui components including listeners DONE
        //info: initialize bottomSheetBehavior
        bottomSheetBehavior = BottomSheetBehavior.from(viewBinding.bottomSheet.root)
        //info: attach listeners
        attachListeners()
        //info: init map
        initMap()
    }

    private fun attachListeners() { // todo attach ui listeners DONE
        //component: bottomsheet listener
        bottomSheetBehavior.makeBottomSheetCallback( //todo create listener functions, scrim visibility DONE
            onStateChanged = {bottomSheet , newState ->
                when(newState) {
                    BottomSheetBehavior.STATE_COLLAPSED -> viewBinding.blurScrim.visibility = View.INVISIBLE
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        map.snapshot {
                            viewBinding.blurScrim.apply {
                                visibility = View.VISIBLE
                                setImageBitmap(it)
                                Blurry.with(requireContext()).radius(10).sampling(8).color(Color.argb(99, 0, 0, 0))
                                        .capture(this).into(this)
                            }
                        }
                    }
                }
            }
        )

        //info: ui click listeners ---------------
        //component: toggleNoteButton click listener
        viewBinding.toggleNoteButton.setOnClickListener { //todo hide/show bottomsheet DONE
            bottomSheetBehavior.state = if(bottomSheetBehavior.state == BottomSheetBehavior.STATE_COLLAPSED) BottomSheetBehavior.STATE_EXPANDED else BottomSheetBehavior.STATE_COLLAPSED
        }
        //component: blurScrim click listener
        viewBinding.blurScrim.setOnClickListener { //todo close bottomsheet DONE
            viewBinding.toggleNoteButton.performClick()
        }
        //component: gearButton click listener
        viewBinding.gearButton.setOnClickListener { //todo show enable/disable location dialog URGENT DONE
            showLocationDialog()
        }
    }



    private fun showLocationDialog() { //todo show location dialog DONE
        val type = DialogTypes.EnableLocationDialog(isLocationCurrentlyEnabled = map.isMyLocationEnabled)
        val dialog = MultipurposeDialog(type)
        dialog.show(childFragmentManager, "locationDialog")
    }

    //todo DONE
    @SuppressLint("MissingPermission")
    override fun toggleLocationEnabled(enable: Boolean) {//info: "enable" is whether or not to enable location. true -> ask for permission if not already granted, false -> disable location
        if (enable) { // info: requestPermission or enable if already granted
            checkLocationPermissionAndLaunchRequest()
        } else { // info: disable location
            map.isMyLocationEnabled = false
            fusedLocationClient.reset()
        }
    }

    //info: check for location permission todo DONE
    private fun checkPermission() : Boolean {
        return ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    private fun checkLocationPermissionAndLaunchRequest() { //todo DONE
        if (!checkPermission()) { //info: permission not yet granted -> request permission
            mPermissionResult.launch(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION))
        } else { //info: permission already granted, enable map location and start updates
            map.isMyLocationEnabled = true
            startLocationUpdates(createLocationRequest(interval = LOCATION_INTERVAL, fastestInterval = LOCATION_FASTEST_INTERVAL, priority = LocationRequest.PRIORITY_HIGH_ACCURACY),
                    locationCallback = locationCallback)
        }
    }

    @SuppressLint("MissingPermission")
    //info: invoke on request finished, launch arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION) todo DONE
    private val mPermissionResult: ActivityResultLauncher<Array<String>> = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->

        map.isMyLocationEnabled = if(!result.containsValue(false)) { //info: permission was granted, start updates and set map.ismylocationenabled to true
            startLocationUpdates(createLocationRequest(interval = LOCATION_INTERVAL, fastestInterval = LOCATION_FASTEST_INTERVAL, priority = LocationRequest.PRIORITY_HIGH_ACCURACY),
                    locationCallback = locationCallback)
            true
        } else { //info: permission denied -> disable map location reset and remove locationclient and callbacks
            fusedLocationClient.reset()
            false
        }

    }

    // info: starting location updates if permission was granted, provide callback and request
    private fun startLocationUpdates(locationRequest : LocationRequest, locationCallback: LocationCallback) {
        if (checkPermission()) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        }
    }

    private fun FusedLocationProviderClient.reset() {
        this.removeLocationUpdates(locationCallback)
    }

    //info: create location request, set params
    private fun createLocationRequest(interval : Long, fastestInterval : Long, priority : Int) : LocationRequest {
        return LocationRequest.create().apply {
            this.interval = interval
            this.fastestInterval = fastestInterval
            this.priority = priority
        }
    }


    private fun initMap() {
        childFragmentManager.beginTransaction().add(R.id.map_container, mapFragment).commit()
        mapFragment.getMapAsync(this)
    }

    @SuppressLint("ResourceType")
    override fun onMapReady(p0: GoogleMap) { //todo map ready functions DONE
        p0.moveCamera(CameraUpdateFactory.newLatLng( LatLng(0.toDouble(), 0.toDouble())))
        val zoomControls = mapFragment.view?.findViewById<View>(0x1)
        if (zoomControls != null && zoomControls.layoutParams is RelativeLayout.LayoutParams) {
            zoomControls.updateLayoutParams {
                val params = this as RelativeLayout.LayoutParams
                params.addRule(RelativeLayout.ALIGN_PARENT_TOP)
                params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
                val margin = TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        10F,
                        resources.displayMetrics
                ).toInt()
                params.setMargins(margin, 6*margin, margin, margin) // left top right bottom
            }
        }
        //component: googleMap
        map = p0
        //info: subscribe observers
        subscribeObservers()

    }

}