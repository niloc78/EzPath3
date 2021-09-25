package com.example.ezpath3.ui.fragment

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import com.example.ezpath3.ui.adapter.ViewPagerAdapter
import javax.inject.Inject

class MainFragmentFactory
@Inject
constructor(
     private val someString : String,
     private val viewPagerAdapter: ViewPagerAdapter
): FragmentFactory() {

    override fun instantiate(classLoader: ClassLoader, className: String): Fragment {
        return when(className) {
//            ErrandFragment::class.java.name -> {
//                ErrandFragment(someString)
//            }
//            MapFragment::class.java.name -> {
//                MapFragment(someString)
//            }
            ViewPagerFragment::class.java.name -> {
                ViewPagerFragment(someString, viewPagerAdapter)
            }
            else -> super.instantiate(classLoader, className)
        }
    }

}