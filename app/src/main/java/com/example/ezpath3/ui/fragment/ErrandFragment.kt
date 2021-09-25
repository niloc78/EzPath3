package com.example.ezpath3.ui.fragment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.TransitionInflater
import androidx.transition.TransitionManager
import com.example.ezpath3.R
import com.example.ezpath3.databinding.ErrandFragLayoutBinding
import com.example.ezpath3.ui.activity.MainActivity
import com.example.ezpath3.ui.adapter.ErrandAdapter
import com.example.ezpath3.ui.dialog.DialogTypes
import com.example.ezpath3.ui.dialog.MultipurposeDialog
import com.example.ezpath3.ui.viewmodel.ErrandModel
import com.example.ezpath3.ui.viewmodel.MainStateEvent
import com.example.ezpath3.util.DataState
import com.example.ezpath3.util.makeItemTouchCallback
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList
import kotlin.collections.LinkedHashMap

@AndroidEntryPoint
class ErrandFragment
@Inject
constructor(
    private val someString : String
) : Fragment(), ErrandAdapter.ErrandItemClickListener, MultipurposeDialog.MultipurposeDialogListener {

    lateinit var viewBinding : ErrandFragLayoutBinding
    private lateinit var linearLayoutManager : LinearLayoutManager
    private val viewModel : ErrandModel by activityViewModels()
    lateinit var errandAdapter : ErrandAdapter

    //component: list Item touch callback
    private val itemTouchCallback : ItemTouchHelper.SimpleCallback = makeItemTouchCallback(
            dragDirs = ItemTouchHelper.START or ItemTouchHelper.END or ItemTouchHelper.UP or ItemTouchHelper.DOWN,
            swipeDirs = ItemTouchHelper.LEFT,
            longPressEnabled = true,
            onMove = {recyclerView, viewHolder, target ->  //info: reorder items
                val fromPos = viewHolder.adapterPosition
                val toPosition = target.adapterPosition
                if (toPosition > errandAdapter.data.lastIndex) {
                    false
                } else {
                    Collections.swap(errandAdapter.data, fromPos, toPosition)
                    errandAdapter.notifyItemMoved(fromPos, toPosition)
                    true
                }
            },
            isLongPressEnabled = {enabled -> enabled}, //info: long press to reorder
            getMovementFlags = {recyclerView, viewHolder, dragDirs, swipeDirs ->
                if (viewHolder is ErrandAdapter.FooterViewHolder) ItemTouchHelper.Callback.makeMovementFlags(0, 0)
                else ItemTouchHelper.Callback.makeMovementFlags(dragDirs, swipeDirs)
            },
            doNotAllowSwipeIf = {recyclerView, viewHolder ->  viewHolder is ErrandAdapter.FooterViewHolder},
            onSwiped = {viewHolder, direction -> //info: delete item
                val pos = viewHolder.adapterPosition
                val errandName = errandAdapter.data[pos]["errandName"].toString()
                viewModel.setStateEvent(MainStateEvent.DeleteErrandResultsEvents, errandName)
            }
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewBinding = ErrandFragLayoutBinding.inflate(layoutInflater)
        val view = viewBinding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUp() // set up ui components
        subscribeObservers() // subscribe observers
        // Log.d("ErrandFragment tag: ", "$tag")
        //Log.d("ErrandFragment parent: ", "${parentFragment?.javaClass?.name}")
        //Log.d("ErrandFragment parent activity:", "${activity?.javaClass?.name}")
        //Log.d("ErrandFragment id: ", "$id")
    }

    private fun subscribeObservers() { //observe combinedResult, currPlaceInfo
        //component: combinedResult observer
        viewModel.combinedResults.observe(viewLifecycleOwner) { new ->
            //println("combinedResults observed! : ${new.entries.joinToString()}")
            //todo notify data changed in recyclerview , ArrayList<LinkedHashMap<String, String>> = ArrayList() // errandName, storeName, address DONE
            val newResultsArrayList : ArrayList<LinkedHashMap<String, String>> = ArrayList()
            val newResults = new.map {
                linkedMapOf("errandName" to it.key, "storeName" to it.value.name, "address" to it.value.formatted_address)
            }
            .forEach {
               newResultsArrayList.add(it)
            }
            errandAdapter.setNewData(newResultsArrayList)
        }
        //component: currPlaceInfo observer
        viewModel.currPlaceInfo.observe(viewLifecycleOwner) {
            //info: set banner text to new place
            if (it is DataState.Success) {
                viewBinding.locationText.text = it.data?.get("address").toString()
            }
        }
    }

    fun toggleSideBarButtonAnim() {
        val set = TransitionInflater.from(context).inflateTransition(R.transition.animate)
        set.duration = 150
        viewBinding.sideBarButton.apply {
            TransitionManager.beginDelayedTransition(this.parent as ViewGroup, set)
            rotation = when (isSelected) {
                true -> {
                    0F
                }
                else -> {
                    90F
                }
            }
            isSelected = !isSelected
        }
    }

    private fun setUp() { //setup other things, adapters, etc
        //info: set up layout manager
        linearLayoutManager = LinearLayoutManager(context)
        //info: set up adapter
        errandAdapter = ErrandAdapter(errandItemClickListener = this)
        //info: set up recyclerview
        viewBinding.errandRecyclerView.apply {
            layoutManager = linearLayoutManager
            adapter = errandAdapter
            //info: attach itemtouch callback
            ItemTouchHelper(itemTouchCallback).attachToRecyclerView(this)
        }
        //info: set up other UI components ---------------- click listeners, etc
        attachClickListeners()
    }

    private val locationLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result -> // launch mainactivity for result to change location
        if (result.resultCode == Activity.RESULT_OK) { // received result, location was updated
            //info: update location in the VM
            viewModel.getCurrPlaceInfo(reAdd = true) // retrieves new value from prefs and set it in livedata
            //todo VM should call location changed functions to readd errands on new location, update poly, and markers DONE
        } else { // todo location was not updated
        }
    }

    private fun attachClickListeners() {
        //component: locationCard click listener
        viewBinding.locationCard.setOnClickListener {  // launch main activity for result to change location
            startChangeLocationForResult()
        }
        //component: sideBarButton click listener
        viewBinding.sideBarButton.setOnClickListener { // open drawer
            // Log.d("find", "${(requireActivity().supportFragmentManager.findFragmentByTag("navHostFragment") as MainNavHostFragment).childFragmentManager.primaryNavigationFragment?.javaClass?.name}")
            // Log.d("requireActivity: ", "${requireActivity().supportFragmentManager.findFragmentByTag("navHostFragment")?.javaClass?.name}")
            (requireActivity().supportFragmentManager.findFragmentByTag("navHostFragment") as MainNavHostFragment).childFragmentManager.primaryNavigationFragment?.let {
                (it as ViewPagerFragment).viewBinding.drawerLayout.openDrawer(GravityCompat.START)
            }
        }

    }

    private fun startChangeLocationForResult() {
        locationLauncher.launch(Intent(context, MainActivity::class.java))
    }

    private fun showAddErrandDialog() {
        val dialog = MultipurposeDialog(DialogTypes.AddErrandDialog)
        dialog.show(childFragmentManager, "addErrandDialog")
    }

    override fun check(checked: Boolean, errandName: String) { // todo update map marker based on checked DONE
        viewModel.updateCheckedMarker(checked = checked, errandName = errandName)
    }

    override fun add(maxed: Boolean) { //todo show Toast or Snackbar if maxed, if not open dialog to add DONE
        if (maxed) {
            Toast.makeText(context, "Maximum amount of errands reached", Toast.LENGTH_SHORT).show()
        } else {
            showAddErrandDialog()
        }
    }


    override fun addErrand(errandName: String) { // trying api search for errand result
        viewModel.setStateEvent(MainStateEvent.GetBothResultsEvents, errandName)
    }


}