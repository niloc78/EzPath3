package com.example.ezpath3.ui.fragment

import android.content.Context
import android.util.Log
import androidx.navigation.fragment.NavHostFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainNavHostFragment : NavHostFragment() {

    @Inject
    lateinit var fragmentFactory: MainFragmentFactory

    override fun onAttach(context: Context) {
        super.onAttach(context)
        childFragmentManager.fragmentFactory = fragmentFactory
        // Log.d("NavHostFrag tag: ", "$tag")
        // Log.d("NavHostFrag parent: ", "${activity?.javaClass?.name}")
        // Log.d("NavHostFrag id: ", "$id")
    }
}