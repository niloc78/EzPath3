package com.example.ezpath3.di.modules

import android.content.Context
import com.example.ezpath3.util.makeLocationCallback
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.SupportMapFragment
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.scopes.FragmentScoped

@Module
@InstallIn(FragmentComponent::class)
object MapModule {

    @FragmentScoped
    @Provides
    fun provideSupportMapFragment() : SupportMapFragment {
        val options = GoogleMapOptions()
        options.apply {
            compassEnabled(true)
            zoomControlsEnabled(true)
        }
        return SupportMapFragment.newInstance(options)
    }

    @FragmentScoped
    @Provides
    fun provideFusedLocationClient(@ActivityContext context: Context) : FusedLocationProviderClient {
        return LocationServices.getFusedLocationProviderClient(context)
    }

    @FragmentScoped
    @Provides
    fun provideLocationCallback() : LocationCallback {
        return makeLocationCallback()
    }

}