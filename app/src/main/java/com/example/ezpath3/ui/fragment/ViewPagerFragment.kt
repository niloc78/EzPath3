package com.example.ezpath3.ui.fragment


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.Scene
import androidx.transition.Transition
import androidx.transition.TransitionManager
import com.example.ezpath3.R
import com.example.ezpath3.databinding.ViewpagerFragLayoutBinding
import com.example.ezpath3.di.modules.movingBackgroundTransitionSet
import com.example.ezpath3.di.modules.toggleFragTransitionSet
import com.example.ezpath3.ui.adapter.SetAdapter
import com.example.ezpath3.ui.adapter.ViewPagerAdapter
import com.example.ezpath3.ui.dialog.DialogTypes
import com.example.ezpath3.ui.dialog.MultipurposeDialog
import com.example.ezpath3.ui.viewmodel.ErrandModel
import com.example.ezpath3.util.DataState
import com.example.ezpath3.util.hide
import com.example.ezpath3.util.makeListener
import com.example.ezpath3.util.show
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class ViewPagerFragment
constructor(someString : String, val viewPagerAdapter: ViewPagerAdapter) : Fragment(R.layout.viewpager_frag_layout), SetAdapter.SetItemClickListener, MultipurposeDialog.MultipurposeDialogListener {
    lateinit var viewBinding : ViewpagerFragLayoutBinding
    private val viewModel : ErrandModel by activityViewModels()
    lateinit var linearLayoutManager : LinearLayoutManager // info: layout manager for setData recyclerview
    lateinit var setAdapter : SetAdapter // setAdapter
    private var defaultMargin : Int = 0
    @Inject
    @toggleFragTransitionSet
    lateinit var toggleFragTransitionSet : Transition

    @Inject
    @movingBackgroundTransitionSet
    lateinit var movingBackgroundSquareTransitionSet : Transition


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewBinding = ViewpagerFragLayoutBinding.inflate(layoutInflater)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUp()
        subscribeObservers()
        // Log.d("ViewPagerFragment tag: ", "$tag")
        // Log.d("ViewPagerFragment parent: ", "${parentFragment?.javaClass?.name}")
        // Log.d("ViewPagerFragment id: ", "$id")
    }


    private fun subscribeObservers() { //info: observers includes:  setData, searchPreferences, mainDataState
        //component: searchPreferences observer
        viewModel.searchPreferences.observe(viewLifecycleOwner) { searchPrefs -> //info: when loaded in or set, set bars and other values to data.
            // println("searchPrefs observed ! : ${searchPrefs.entries.joinToString()}")
            //info: set rating bar value
            viewBinding.ratingBar.rating = searchPrefs["rating"] as Float
            //info: set price level
            val level = searchPrefs["price_level"] as Int
            viewBinding.priceLevel1.isSelected = level == 1
            viewBinding.priceLevel2.isSelected = level == 2
            viewBinding.priceLevel3.isSelected = level == 3
            //info: set radius
            viewBinding.radiusSlider.value = searchPrefs["radius"] as Float
            //info: set chip_rating
            val prioritizeRating = searchPrefs["chip_rating"] as Boolean
            viewBinding.priorityChipGroup.check(if(prioritizeRating) viewBinding.chipRating.id else viewBinding.chipDistance.id)
        }
        //component: setData observer
        viewModel.setData.observe(viewLifecycleOwner) {
            // println("setData observed! ${it.joinToString()}")
            //info: update adapter and notify changes
            setAdapter.setNewData(it)
        }
        //component: mainDataState observer
        viewModel.mainDataState.observe(viewLifecycleOwner) {
            // println("combinedResults observed! : $it")
            when(it) {
                is DataState.Success -> {
                    showSnackBar(it.data)
                }
                is DataState.Error -> {
                    showToast(it.exception.message ?: "Unknown error")
                }
            }
        }
    }

    private fun showSnackBar(message : String) {
        Snackbar.make(viewBinding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    private fun showToast(message : String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    private fun setUp() { //info: set up other things, includes: click listeners, drawer layout listeners, animation, getting saved stuff
        //info: default margin
        defaultMargin = (viewBinding.toggleFragButton.layoutParams as ConstraintLayout.LayoutParams).bottomMargin
        //info:  set up viewpager
        viewBinding.viewPager.apply {
            adapter = viewPagerAdapter
            offscreenPageLimit = 2
            isNestedScrollingEnabled = false
            isUserInputEnabled = false
        }
        // Log.d("Fragments : ", "${childFragmentManager.fragments.joinToString()} size : ${childFragmentManager.fragments.size}")
        //info: setData
        linearLayoutManager = LinearLayoutManager(context) // layout manager
        setAdapter = SetAdapter(this)
        viewBinding.savedErrandsRecyclerView.apply {
            layoutManager = linearLayoutManager
            adapter = setAdapter
        }
        //info: attach listeners
        attachListeners()
        //info: load search saved search preferences
        viewModel.getSearchPreferences()
        //info: get all sets
        viewModel.getAllSets()
    }

    private fun attachListeners() {
        //info: transition listeners -----------------------------

        toggleFragTransitionSet.makeListener(onStart = {
            when(viewBinding.viewPager.currentItem) {
                0 -> {viewBinding.viewPager.setCurrentItem(1, true)}
                1 -> {viewBinding.viewPager.setCurrentItem(0, true)}
            }
        })

        //component:  drawer listener for push to side animation
        viewBinding.drawerLayout.makeListener(
                onSlide = { _, slideOffset ->
                    val moveFactor = viewBinding.drawerContainer.width * slideOffset
                    viewBinding.contentFrame.translationX = moveFactor
                },
                onOpened = {
                   //info: set sidebar button animation
                    viewPagerAdapter.errandFragment.toggleSideBarButtonAnim()
                },
                onClosed = {
                    //info: //set live data search preferences on close
                    viewModel.setSearchPreferences(
                            rating = viewBinding.ratingBar.rating,
                            priceLevel = when {
                                viewBinding.priceLevel1.isSelected -> {
                                    1
                                }
                                viewBinding.priceLevel2.isSelected -> {
                                    2
                                }
                                viewBinding.priceLevel3.isSelected -> {
                                    3
                                }
                                else -> {
                                    0
                                }
                            },
                            radius = viewBinding.radiusSlider.value,
                            chipRating = viewBinding.chipRating.isChecked
                    )
                    //info: set sidebarbutton animation
                    viewPagerAdapter.errandFragment.toggleSideBarButtonAnim()
                    //todo deselect all recyclerview setData items that are selected DONE
                    for(index in 0 until setAdapter.data.size) {
                        linearLayoutManager.findViewByPosition(index)?.performClick()
                    }
                }
        )

        //component:  preference click listeners
        viewBinding.priceLevel1.setOnClickListener {
            it.isSelected = !it.isSelected
            viewBinding.priceLevel2.isSelected = false
            viewBinding.priceLevel3.isSelected = false

        }
        viewBinding.priceLevel2.setOnClickListener {
            it.isSelected = !it.isSelected
            viewBinding.priceLevel1.isSelected = false
            viewBinding.priceLevel3.isSelected = false

        }
        viewBinding.priceLevel3.setOnClickListener {
            it.isSelected = !it.isSelected
            viewBinding.priceLevel1.isSelected = false
            viewBinding.priceLevel2.isSelected = false
        }

        //info:  within drawer button listeners ------------------
        //component: filterButton listener todo show edit preferences layout and animation DONE
        viewBinding.filterButton.apply {
            isSelected = true
            setOnClickListener {
                toggleSelect()
            }
        }

        //component: folderButton listener todo show recyclerview layout containing all savedsets and animation DONE
        viewBinding.folderButton.apply {
            setOnClickListener {
                toggleSelect()
            }
        }

        //component: saveSetButtonListener todo open save set dialog DONE
        viewBinding.saveButton.setOnClickListener { showSaveSetDialog() }

        //info:  other click listeners ------------------
        //component:  toggleFrag button listener todo toggle frags and animation DONE
        viewBinding.toggleFragButton.apply { setOnClickListener { toggleFrag() } }

    }

    private fun FloatingActionButton.toggleFrag() {
        val par = this.parent
        TransitionManager.go(Scene(par as ViewGroup), toggleFragTransitionSet)
        val constraintLayout = par as ConstraintLayout
        val constraintSet = ConstraintSet()
        constraintSet.clone(constraintLayout)
        val firstConstraint = if (viewBinding.viewPager.currentItem == 0)  ConstraintSet.START else ConstraintSet.END
        val secondConstraint = if (viewBinding.viewPager.currentItem == 0) ConstraintSet.END else ConstraintSet.START
        constraintSet.apply {
            clear(this@toggleFrag.id, firstConstraint)
            connect(this@toggleFrag.id, secondConstraint, ConstraintSet.PARENT_ID, secondConstraint)
            applyTo(constraintLayout)
        }
        (this.layoutParams as ConstraintLayout.LayoutParams).setMargins(defaultMargin, 0, defaultMargin, defaultMargin)
        this.setImageResource(if (viewBinding.viewPager.currentItem == 0) R.drawable.ic_check_list_icon else R.drawable.ic_map_icon)
    }

    private fun ImageButton.toggleSelect() {
        if (!isSelected) {
            //info: set selected to true
            isSelected = true
            //info: animate background square
            moveIconBackground(buttonId = id)
            //info: deselect button
            when (id) {
                R.id.filter_button -> {
                    viewBinding.folderButton.isSelected = false
                    //info: hide layout of savedSets
                    viewBinding.savedContainer.hide()
                    //info: show preferences layout
                    viewBinding.preferencesContainer.show()
                }
                R.id.folder_button -> {
                    viewBinding.filterButton.isSelected = false
                    //info: hide preferences layout
                    viewBinding.preferencesContainer.hide()
                    //info: show layout of saved sets
                    viewBinding.savedContainer.show()
                }
            }
        }
    }


    private fun moveIconBackground(buttonId : Int) {
        val par = viewBinding.movingShapeBackground.parent
        val constraintLayout = par as ConstraintLayout
        viewBinding.movingShapeBackground.apply {
            TransitionManager.go(Scene(par as ViewGroup), movingBackgroundSquareTransitionSet)
            val constraintSet1 = ConstraintSet() // first part of animation
            constraintSet1.clone(constraintLayout)
            val firstConstraint = if (buttonId == R.id.filter_button) ConstraintSet.TOP else ConstraintSet.BOTTOM
            val secondConstraint = if(buttonId == R.id.filter_button) ConstraintSet.BOTTOM else ConstraintSet.TOP
            constraintSet1.clear(this.id, firstConstraint)
            constraintSet1.connect(this.id, firstConstraint, buttonId, firstConstraint)
            constraintSet1.applyTo(constraintLayout)
            CoroutineScope(Main).launch {
                delay(150L)
                TransitionManager.go(Scene(par as ViewGroup), movingBackgroundSquareTransitionSet)
                val constraintSet2 = ConstraintSet()
                constraintSet2.clone(constraintLayout)
                constraintSet2.clear(this@apply.id, secondConstraint)
                constraintSet2.connect(this@apply.id, secondConstraint, buttonId, secondConstraint)
                constraintSet2.applyTo(constraintLayout)
            }
        }
    }

    private fun showSaveSetDialog() { //DONE
        val dialog = MultipurposeDialog(type = DialogTypes.SaveSetDialog)
        dialog.show(childFragmentManager, "saveSetDialog")
    }

    override fun saveSet(setName: String) { // todo save set if 0 errands show toast DONE
        val numErrs = viewModel.combinedResults.value?.size ?: 0
        when {
            !setName.isNullOrBlank() && numErrs >  0 -> viewModel.storeSet(setName = setName) //info: setName not null or blank and have atleast 1 errand in list
            numErrs == 0 -> showToast(message = "You must add at least one errand") //info: 0 errands in list
            else -> showToast(message = "Set name cannot be blank") //info: user tried to enter blank name
        }
    }

    override fun confirmLoadSet(setName: String) { // todo load set close drawer first DONE
        viewBinding.drawerLayout.closeDrawers()
        viewModel.loadSet(setName = setName)
    }


    override fun deleteSet(setName: String) { //todo delete set DONE
        viewModel.deleteSet(setName = setName)
    }

    override fun showLoadSetDialog(setName: String) { //info: show load set dialog by first retrieving set info to display in dialog DONE
        CoroutineScope(Main).launch {
            var info : Set<String> = setOf()
            viewModel.getSetInfo(setName) //info: suspending function
                    .onEach { info = it }
                    .launchIn(this).join()
            withContext(Main) {
                val dialog = MultipurposeDialog(DialogTypes.ConfirmLoadSetDialog(setName = setName, set = info))
                dialog.show(childFragmentManager, "loadSetDialog")
            }
        }

    }


}