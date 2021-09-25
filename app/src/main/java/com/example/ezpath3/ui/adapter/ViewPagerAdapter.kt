package com.example.ezpath3.ui.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.ezpath3.ui.fragment.ErrandFragment
import com.example.ezpath3.ui.fragment.MapFragment
import javax.inject.Inject

class ViewPagerAdapter
@Inject
constructor(fa : FragmentActivity, val errandFragment: ErrandFragment, val mapFragment: MapFragment) : FragmentStateAdapter(fa) {

    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        return when(position) {
            0 -> errandFragment
            1 -> mapFragment
            else -> errandFragment
        }
    }
}