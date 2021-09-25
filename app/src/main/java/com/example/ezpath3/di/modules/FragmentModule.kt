package com.example.ezpath3.di.modules

import com.example.ezpath3.ui.fragment.ErrandFragment
import com.example.ezpath3.ui.fragment.MapFragment
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn

import dagger.hilt.android.components.FragmentComponent

import dagger.hilt.android.scopes.FragmentScoped


@Module
@InstallIn(FragmentComponent::class)
object FragmentModule {

    @FragmentScoped
    fun provideErrandFragment(someString : String) : ErrandFragment {
        return ErrandFragment(someString)
    }

    @FragmentScoped
    @Provides
    fun provideMapFragment(someString : String) : MapFragment {
        return MapFragment(someString)
    }

//    @FragmentScoped
//    @Provides
//    fun provideViewPagerAdapter(@ActivityContext context : Context, errandFragment: ErrandFragment, mapFragment: MapFragment) : ViewPagerAdapter {
//        return ViewPagerAdapter(context as FragmentActivity, errandFragment, mapFragment)
//    }




}