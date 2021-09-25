package com.example.ezpath3.util

import androidx.recyclerview.widget.DiffUtil
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.LocationAvailability
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener

fun AutocompleteSupportFragment.makePlaceListener(onPlaceSelected : (place : Place) -> Unit = {}, onError : (status : Status) -> Unit = {}) : AutocompleteSupportFragment{
    this.setOnPlaceSelectedListener(object : PlaceSelectionListener {
        override fun onPlaceSelected(p0: Place) {
            onPlaceSelected(p0)
        }

        override fun onError(p0: Status) {
            onError(p0)
        }

    })
    return this
}

fun makeDiffUtilListener(getOldListSize : () -> Int = {0},
                         getNewListSize : () -> Int = {0},
                         areItemsTheSame : (oldItemPosition : Int, newItemPosition : Int) -> Boolean = {oldItemPos , newItemPos -> false},
                         areContentsTheSame : (oldItemPosition : Int, newItemPosition : Int) -> Boolean = {oldItemPos, newItemPos -> false}) : DiffUtil.DiffResult {
    return DiffUtil.calculateDiff(object : DiffUtil.Callback() {
        override fun getOldListSize(): Int {
            return getOldListSize()
        }

        override fun getNewListSize(): Int {
            return getNewListSize()
        }

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return areItemsTheSame(oldItemPosition, newItemPosition)
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return areContentsTheSame(oldItemPosition, newItemPosition)
        }

    })

}

 fun makeLocationCallback(onLocationAvailability : (locationAvailability : LocationAvailability?) -> Unit = {},
                          onLocationResult : (locationResult : LocationResult?) -> Unit = {}) : LocationCallback {
     return object : LocationCallback() {
         override fun onLocationAvailability(p0: LocationAvailability?) {
             super.onLocationAvailability(p0)
             onLocationAvailability(p0)
         }

         override fun onLocationResult(p0: LocationResult?) {
             super.onLocationResult(p0)
             onLocationResult(p0)
         }
     }
 }