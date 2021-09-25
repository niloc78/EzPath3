package com.example.ezpath3.ui.activity

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.example.ezpath3.databinding.ErrandActivityLayoutBinding
import com.example.ezpath3.ui.fragment.MainNavHostFragment
import com.example.ezpath3.ui.fragment.ViewPagerFragment

import com.example.ezpath3.ui.viewmodel.ErrandModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class ErrandActivity : AppCompatActivity() {

    lateinit var viewBinding : ErrandActivityLayoutBinding
    private val viewModel : ErrandModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //get currPlaceInfo from intent
        //getIntentCurrPlaceInfo()

        viewBinding = ErrandActivityLayoutBinding.inflate(layoutInflater)
        val view = viewBinding.root
        setContentView(view)
        subscribeObservers()
        getCurrPlaceInfo()
        //Toast.makeText(this, viewModel.currPlaceInfo.value?.entries?.joinToString(), Toast.LENGTH_SHORT).show()
    }

    fun subscribeObservers() {
        viewModel.currPlaceInfo.observe(this) { currPlaceInfo -> //location changed/updated
            //Toast.makeText(this, "ErrandActivity currPlaceInfo observed: ${currPlaceInfo}", Toast.LENGTH_SHORT).show()
        }
    }

    fun getCurrPlaceInfo() {
        viewModel.getCurrPlaceInfo()
    }

    fun updateCurrPlaceInVM(currPlaceId : String, currPlaceLatLng : DoubleArray, currPlaceAddress : String) {
        viewModel.setCurrPlaceInfo(currPlaceId, currPlaceLatLng, currPlaceAddress)
    }

    override fun onBackPressed() {
        (supportFragmentManager.findFragmentByTag("navHostFragment") as MainNavHostFragment).childFragmentManager.primaryNavigationFragment?.let {
            when {
                // info: currently drawer is open -> close drawer
                (it as ViewPagerFragment).viewBinding.drawerLayout.isDrawerOpen(GravityCompat.START) -> it.viewBinding.drawerLayout.closeDrawers()
                // info: currently on mapFragment -> move to errandFragment
                it.viewBinding.viewPager.currentItem == 1 -> it.viewBinding.toggleFragButton.performClick()
                // info: currently on errandFragment and drawers are closed -> move task to back
                else -> moveTaskToBack(true)
            }
        }
    }


}